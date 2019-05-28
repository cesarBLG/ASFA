#include <SDL2/SDL.h>
#include <unistd.h>
#include <stdio.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#define PORT 5000
char *sonidos[] = {
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
void play(const char* name);
void playLoop(const char* name);
void stop();
int main(int argc, char** argv)
{
    SDL_Init(SDL_INIT_AUDIO | SDL_INIT_TIMER);
    int sock;
    struct sockaddr_in server;
    sock = socket(AF_INET, SOCK_STREAM, 0);
    server.sin_addr.s_addr = inet_addr("127.0.0.1");
    server.sin_family = AF_INET;
    server.sin_port = htons(PORT);
    connect(sock, (struct sockaddr *)&server, sizeof(server));
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
            char s[50];
            s[0] = 0;
            strcat(s, "src/content/Sonido/");
            if(basic) strcat(s, "Basico/");
            strcat(s, sonidos[num]);
            strcat(s, ".wav");
            if(trig)
            {
                if(num == 7 || num == 8 || num == 10 || num == 11 || num == 13) playLoop(s);
                else play(s);
            }
            else stop();
        }
    }
}
int open = 0;
SDL_AudioDeviceID deviceId;
Uint32 warnLength;
Uint8 *warnBuffer;
Uint32 refill(Uint32 interval, void *param)
{
    if(warnBuffer != NULL && SDL_GetQueuedAudioSize(deviceId) < 100000) SDL_QueueAudio(deviceId, warnBuffer, warnLength);
    if(warnBuffer == NULL) return 0;
    return interval;
}
void playLoop(const char* name)
{
    if(warnBuffer != NULL) stop();
    SDL_AudioSpec wavSpec;
    
    SDL_LoadWAV(name, &wavSpec, &warnBuffer, &warnLength);
    if(open) SDL_CloseAudioDevice(deviceId);
    deviceId = SDL_OpenAudioDevice(NULL, 0, &wavSpec, NULL, 0);
    int success = SDL_QueueAudio(deviceId, warnBuffer, warnLength);
    SDL_PauseAudioDevice(deviceId, 0);
    SDL_AddTimer(50, refill, NULL);
    open = 1;
}
void play(const char* name)
{
    SDL_AudioSpec wavSpec;
    Uint32 wavLength;
    Uint8 *wavBuffer;
    
    SDL_LoadWAV(name, &wavSpec, &wavBuffer, &wavLength);
    if(warnBuffer != NULL)
    {
        stop();
    }
    if(open) SDL_CloseAudioDevice(deviceId);
    deviceId = SDL_OpenAudioDevice(NULL, 0, &wavSpec, NULL, 0);
    int success = SDL_QueueAudio(deviceId, wavBuffer, wavLength);
    SDL_PauseAudioDevice(deviceId, 0);
    SDL_FreeWAV(wavBuffer);
    open = 1;
}
void stop()
{
    SDL_FreeWAV(warnBuffer);
    warnBuffer = NULL;
    if(open) SDL_CloseAudioDevice(deviceId);
    open = 0;
}