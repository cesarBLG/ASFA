#define SDL_MAIN_HANDLED
#include <SDL2/SDL.h>
#include <stdio.h>
#include <mutex>
#ifdef WIN32
#include <winsock2.h>
#include <windows.h>
#else
#include <unistd.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#endif
#include <map>
#define PORT 5000
const char *sonidos[] = {
	"S1-1",
	"S2-1",
	"S2-2",
	"S2-3",
	"S2-4",
	"S2-5",
	"S2-6",
	"S3-1",
	"S3-2",
	"S3-3",
	"S3-4",
	"S3-5",
	"S4",
	"S5",
	"S6"
};
struct soundid
{
    const char *snd;
    bool basic;
    bool operator<(const soundid ot) const
    {
        if(ot.snd==snd) return basic&&!ot.basic;
        return ot.snd<snd;
    }
    bool operator==(const soundid ot) const
    {
        return ot.snd==snd && basic==ot.basic;
    }
};
struct sdlsounddata
{
    Uint8 *wavBuffer;
    SDL_AudioSpec wavSpec;
    Uint32 wavLength;
};
std::map<soundid, sdlsounddata> sndbuf;
void play(sdlsounddata d, int channel, bool loop=false);
void stop(int i);
#define NCHANNELS 8
SDL_AudioDeviceID deviceId[NCHANNELS];
Uint32 refill(Uint32 interval, void *param);
Uint32 updateVolume(Uint32 interval, void *param);
int numactivo[NCHANNELS];
int main(int argc, char** argv)
{
#ifdef WIN32
    WSADATA wsa;
    WSAStartup(MAKEWORD(2,2), &wsa);
#endif
    SDL_Init(SDL_INIT_AUDIO | SDL_INIT_TIMER);
    int sock;
    struct sockaddr_in server;
    sock = socket(AF_INET, SOCK_STREAM, 0);
    server.sin_addr.s_addr = inet_addr("127.0.0.1");
    server.sin_family = AF_INET;
    server.sin_port = htons(PORT);
    connect(sock, (struct sockaddr *)&server, sizeof(server));
    for(int i=0; i<15; i++)
    {
        {
            char s[50];
            s[0] = 0;
            strcat(s, "src/content/Sonido/");
            strcat(s, sonidos[i]);
            strcat(s, ".wav");
            sdlsounddata d;
            SDL_LoadWAV(s, &d.wavSpec, &d.wavBuffer, &d.wavLength);
            if(i<NCHANNELS) deviceId[i] = SDL_OpenAudioDevice(NULL, 0, &d.wavSpec, NULL, 0);
            sndbuf[soundid({sonidos[i],false})] = d;
            
        }
        {
            char s[50];
            s[0] = 0;
            strcat(s, "src/content/Sonido/");
            strcat(s, "Basico/");
            strcat(s, sonidos[i]);
            strcat(s, ".wav");
            sdlsounddata d;
            SDL_LoadWAV(s, &d.wavSpec, &d.wavBuffer, &d.wavLength);
            sndbuf[soundid({sonidos[i],true})] = d;
        }
    }
    //SDL_AddTimer(100, updateVolume, nullptr);
    while(1)
    {
        char buff[3];
        int res = recv(sock, buff, 3, MSG_WAITALL);
        if(res<=0) return 0;
        int functn = buff[0];
        int val = buff[1];
        if(functn==15)
        {
            int basic = (val & 1) != 0;
            int trig = (val & 2) != 0;
            int num = val >> 2;
            if (basic)
            {
                if ((num > 1 && num <7) || num == 8) continue;
            }
            sdlsounddata d = sndbuf[soundid({sonidos[num],basic!=0})];
            int i=0;
            switch(num)
            {
                case 13:
                    i = 0;
                    break;
                case 0:
                case 1:
                case 14:
                    i = 1;
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    i = 2;
                    break;
                case 9:
                    i = 3;
                    break;
                case 11:
                    i = 4;
                    break;
                case 10:
                    i = 5;
                    break;
                case 7:
                case 8:
                    i = 6;
                    break;
                case 12:
                    i = 7;
                    break;
            }
            if(trig)
            {
                bool loop = (num == 7 || num == 8 || num == 10 || num == 11 || num == 13);
                if (!loop || numactivo[i] != num) play(d,i,loop);
                numactivo[i] = num;
            }
            else if(numactivo[i] == num)
            {
                stop(i);
                numactivo[i] = -1;
            }
        }
    }
}
Uint32 updateVolume(Uint32 interval, void *param)
{
    bool mute = false;
    for (int i=0; i<NCHANNELS; i++)
    {
        if (SDL_GetAudioDeviceStatus(deviceId[i]) == SDL_AUDIO_PLAYING) mute = true;
    }
    return interval;
}
std::mutex mtx[NCHANNELS];
Uint32 warnLength[NCHANNELS];
Uint8 *warnBuffer[NCHANNELS];
Uint32 refill(Uint32 interval, void *param)
{
    int i = *((int*)(&param));
    std::unique_lock<std::mutex> lck(mtx[i]);
    if(warnBuffer[i] != nullptr && SDL_GetQueuedAudioSize(deviceId[i]) < 3*warnLength[i]) SDL_QueueAudio(deviceId[i], warnBuffer[i], warnLength[i]);
    if(warnBuffer[i] == nullptr) return 0;
    return interval;
}
void play(sdlsounddata d, int i, bool loop)
{
    std::unique_lock<std::mutex> lck(mtx[i]);
    if(warnBuffer[i] != nullptr)
    {
        lck.unlock();
        stop(i);
        lck.lock();
    }
    else SDL_ClearQueuedAudio(deviceId[i]);
    int success = SDL_QueueAudio(deviceId[i], d.wavBuffer, d.wavLength);
    SDL_PauseAudioDevice(deviceId[i], 0);
    if(loop)
    {
        warnLength[i] = d.wavLength;
        warnBuffer[i] = d.wavBuffer;
        SDL_AddTimer(50, refill, (void*)i);
    }
}
void stop(int i)
{
    std::unique_lock<std::mutex> lck(mtx[i]);
    SDL_ClearQueuedAudio(deviceId[i]);
    warnBuffer[i] = nullptr;
    SDL_PauseAudioDevice(deviceId[i], 1);
}
