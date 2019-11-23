#define SDL_MAIN_HANDLED
#include <SDL2/SDL.h>
#include <stdio.h>
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
void play(sdlsounddata d, bool loop=false);
void stop();
SDL_AudioDeviceID deviceId;
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
            if(i==0) deviceId = SDL_OpenAudioDevice(NULL, 0, &d.wavSpec, NULL, 0);
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
            sdlsounddata d = sndbuf[soundid({sonidos[num],basic!=0})];
            if(trig)
            {
                if(num == 7 || num == 8 || num == 10 || num == 11 || num == 13) play(d,true);
                else play(d);
            }
            else stop();
        }
    }
}
Uint32 warnLength;
Uint8 *warnBuffer;
Uint32 refill(Uint32 interval, void *param)
{
    if(warnBuffer != NULL && SDL_GetQueuedAudioSize(deviceId) < 3*warnLength) SDL_QueueAudio(deviceId, warnBuffer, warnLength);
    if(warnBuffer == NULL) return 0;
    return interval;
}
void play(sdlsounddata d, bool loop)
{
    if(warnBuffer != NULL) stop();
    
    SDL_ClearQueuedAudio(deviceId);
    int success = SDL_QueueAudio(deviceId, d.wavBuffer, d.wavLength);
    SDL_PauseAudioDevice(deviceId, 0);
    if(loop)
    {
        warnBuffer = d.wavBuffer;
        warnLength = d.wavLength;
        SDL_AddTimer(50, refill, NULL);
    }
}
void stop()
{
    SDL_ClearQueuedAudio(deviceId);
    warnBuffer = nullptr;
    SDL_PauseAudioDevice(deviceId, 1);
}
