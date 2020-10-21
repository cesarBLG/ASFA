#define SDL_MAIN_HANDLED
#include <SDL2/SDL.h>
#include <SDL2/SDL_mixer.h>
#include <stdio.h>
#include <mutex>
#include <orts/client.h>
#include <orts/common.h>
#ifdef WIN32
#include <winsock2.h>
#include <windows.h>
#else
#include <unistd.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#endif
#include <map>
using namespace ORserver;
using namespace std;
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
std::map<soundid, Mix_Chunk*> sndbuf;
#define NCHANNELS 8
Uint32 updateVolume(Uint32 interval, void *param);
int numactivo[NCHANNELS];

string fabricante = "INDRA";

ParameterManager manager;
client *s_client;
string path = "src/content/Sonido/";
void cargar_sonidos()
{
    sndbuf.clear();
    for(int i=0; i<15; i++)
    {
        {
            string s = path+fabricante+"/"+sonidos[i]+".wav";
            Mix_Chunk *m = Mix_LoadWAV(s.c_str());
            if (m == nullptr)
            {
                string s = path+"INDRA/";
                s+=sonidos[i];
                s+=".wav";
                m = Mix_LoadWAV(s.c_str());
            }
            sndbuf[soundid({sonidos[i],false})] = m;
            
        }
        {
            string s = path+fabricante+"/Basico/"+sonidos[i]+".wav";
            Mix_Chunk *m = Mix_LoadWAV(s.c_str());
            if (m == nullptr)
            {
                string s = path+"INDRA/Basico/";
                s+=sonidos[i];
                s+=".wav";
                m = Mix_LoadWAV(s.c_str());
            }
            sndbuf[soundid({sonidos[i],true})] = m;
        }
    }
}

void handle_sound(int num, bool basic, bool trig);

int main(int argc, char** argv)
{
#ifdef WIN32
    WSADATA wsa;
    WSAStartup(MAKEWORD(2,2), &wsa);
#endif
    SDL_Init(SDL_INIT_AUDIO | SDL_INIT_TIMER);
    Mix_Init(0);
    Mix_AllocateChannels(NCHANNELS);
    
    threadwait *poller = new threadwait();
    s_client = TCPclient::connect_to_server(poller);
    s_client->WriteLine("register(asfa::fabricante)");
    s_client->WriteLine("register(asfa::sonido::*)");

    Mix_OpenAudio(MIX_DEFAULT_FREQUENCY,MIX_DEFAULT_FORMAT,2,1024);
    
    cargar_sonidos();
    
    Parameter trig("asfa::sonido::iniciar");
    Parameter stop("asfa::sonido::detener");
    Parameter fabr("asfa::fabricante");
    
    trig.SetValue = [](string val) {
        string::size_type com = val.find_first_of(',');
        bool bas = false;
        if (com != string::npos)
            bas = val.substr(com + 1) == "1";
        string snd = val.substr(0, com);
        for (int i=0; i<15; i++) {
            if (sonidos[i] == snd)
                handle_sound(i, bas, true);
        }
    };
    stop.SetValue = [](string val) {
        string::size_type com = val.find_first_of(',');
        bool bas = false;
        if (com != string::npos)
            bas = val.substr(com + 1) == "1";
        string snd = val.substr(0, com);
        for (int i=0; i<15; i++) {
            if (sonidos[i] == snd)
                handle_sound(i, bas, false);
        }
    };
    fabr.SetValue = [](string val) {
        fabricante = val;
        cargar_sonidos();
    };
    
    manager.AddParameter(&trig);
    manager.AddParameter(&stop);
    manager.AddParameter(&fabr);
    
    SDL_AddTimer(100, updateVolume, nullptr);
    while(s_client->connected) {
        int nfds = poller->poll(1000);
        if (nfds == 0)
            continue;
        s_client->handle();
        string s = s_client->ReadLine();
        while(s!="") {
            if (s == "unregister(asfa::frecuencia)")
                return 0;
            manager.ParseLine(s_client, s);
            s = s_client->ReadLine();
        }
    }
}
void handle_sound(int num, bool basic, bool trig)
{
    if (basic)
    {
        return;
        if ((num > 1 && num <7) || num == 8) return;
    }
    int i=0;
    switch(num)
    {
        case 13:
            i = 0;
            break;
        case 1:
        case 14:
            i = 1;
            break;
        case 0:
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
        Mix_Chunk *m = sndbuf[soundid({sonidos[num],basic})];
        bool loop = (num == 7 || num == 8 || num == 10 || num == 11 || num == 13);
        if (!loop || numactivo[i] != num) Mix_PlayChannel(i, m, loop ? -1 : 0);
        numactivo[i] = num;
        updateVolume(0,0);
    }
    else if(numactivo[i] == num)
    {
        Mix_HaltChannel(i);
        numactivo[i] = -1;
    }
}
Uint32 updateVolume(Uint32 interval, void *param)
{
    bool mute = false;
    for (int i=0; i<NCHANNELS; i++)
    {
        Mix_Volume(i, mute ? 0 : 64);
        if (Mix_Playing(i)!=0) mute = true;
    }
    return interval;
}
