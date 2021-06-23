#include <AltSoftSerial.h>
enum Sonidos
{
  SinSonido,
  S11,
  S21,
  S22,
  S23,
  S24,
  S25,
  S26,
  S31,
  S32,
  S33,
  S34,
  S35,
  S4,
  S5,
  S6
};
class AvisadorASFAD
{
  unsigned long sonidos[16];
  bool enviado[16];
  int sonando;
  Stream *serial;
  bool soft;
  public:
  int volumen = 15;
  void begin(HardwareSerial &s)
  {
    soft = false;
    s.begin(9600);
    serial = &s;
  }
  void begin(AltSoftSerial &s)
  {
    soft = true;
    s.begin(9600);
    serial = &s;
  }
  void iniciar(int son)
  {
     sonidos[son] = millis();
     enviado[son] = false;
  }
  void detener(int son)
  {
     sonidos[son] = 0; 
  }
  void update()
  {
    int nuevo = 0;
    if (sonidos[S5]) {
      nuevo = S5;
    } else if (sonidos[S6]) {
      if (sonidos[S6] + 3000UL < millis()) detener(S6);
      nuevo = S6;
    } else if (sonidos[S11]) {
      if (sonidos[S11] + 300UL < millis()) detener(S11);
      nuevo = S11;
    } else if (sonidos[S21]) {
      if (sonidos[S21] + 3000UL < millis()) detener(S21);
      nuevo = S21;
    } else if (sonidos[S22]) {
      if (sonidos[S22] + 300UL < millis()) detener(S22);
      nuevo = S22;
    } else if (sonidos[S23]) {
      if (sonidos[S23] + 500UL < millis()) detener(S23);
      nuevo = S23;
    } else if (sonidos[S24]) {
      if (sonidos[S24] + 1100UL < millis()) detener(S24);
      nuevo = S24;
    } else if (sonidos[S25]) {
      if (sonidos[S25] + 1900UL < millis()) detener(S25);
      nuevo = S25;
    } else if (sonidos[S26]) {
      if (sonidos[S26] + 2900UL < millis()) detener(S26);
      nuevo = S26;
    } else if (sonidos[S33]) {
      if (sonidos[S33] + 6000UL < millis()) detener(S33);
      nuevo = S33;
    } else if (sonidos[S35]) {
      nuevo = S35;
    } else if (sonidos[S34]) {
      nuevo = S34;
    } else if (sonidos[S31]) {
      nuevo = S31;
    } else if (sonidos[S32]) {
      nuevo = S32;
    } else if (sonidos[S4]) {
      if (sonidos[S4] + 10000 < millis()) detener(S4);
      nuevo = S4;
    }
    if (nuevo != 0 && !sonidos[nuevo])
    {
      update();
      return;
    }
    if (sonando != nuevo)
    {
      if (sonando != 0 && nuevo != 0) stop();
      int son = nuevo;
      bool loop = (son == S31 || son == S32 || son == S34 || son == S35 || son == S5);
      if (son == 0) stop();
      else if (loop) playLoop(son);
      else play(son);
    }
  }
  void write(byte data)
  {
    if (soft) ((AltSoftSerial*)serial)->write(data);
    else ((HardwareSerial*)serial)->write(data);
  }
  unsigned long ultimoEnvio = 0;
  void play(int sonido) {
    if (ultimoEnvio + 80 > millis())
    {
      if (!enviado[sonido])
      {
        sonidos[sonido] += ultimoEnvio + 80 - millis();
        enviado[sonido] = true;
      }
      return;
    }
    enviado[sonido] = true;
    sonando = sonido;
    write(0x7E);
    write(0x04);
    write(0x41);
    write(0x00);
    write(sonido);
    write(0xEF);
    ultimoEnvio = millis();
  }
  void playLoop(int sonido) {
    if (ultimoEnvio + 80 > millis()) return;
    sonando = sonido;
    write(0x7E);
    write(0x04);
    write(0x33);
    write(0x00);
    write(sonido);
    write(0xEF);
    ultimoEnvio = millis();
  }
  void stop()
  {
    if (ultimoEnvio + 80 > millis()) return;
    sonando = 0;
    write(0x7E);
    write(0x02);
    write(0x0E);
    write(0xEF);
    ultimoEnvio = millis();
  }
  void setVolume(int vol)
  {
    if (ultimoEnvio + 80 > millis()) return;
    volumen = vol;
    write(0x7E);
    write(0x03);
    write(0x31);
    write(volumen);
    write(0xEF);
    ultimoEnvio = millis();
  }
};
#define AVISADOR_ASFAB_PIEZO 0
#define AVISADOR_ASFAB_MONOTONO 1
class AvisadorBasico
{
  int PinAvisador;
  int Modo;
  unsigned long sonidos[16];
  bool sonando;
  public:
  AvisadorBasico(int pin, int modo)
  {
    PinAvisador = pin;
    Modo = modo;
  }
  void begin()
  {
    pinMode(PinAvisador, OUTPUT);
  }
  void iniciar(int son)
  {
     sonidos[son] = millis();  
  }
  void detener(int son)
  {  
     sonidos[son] = 0; 
  }
  void update()
  {
    bool on = false;
    if (sonidos[S5])
    {
      on = true;
    }
    else if (sonidos[S6])
    {
      on = millis() - sonidos[S6] < 3000;
      if (!on) sonidos[S6] = 0;
    }
    else if (sonidos[S21])
    {
      on = millis() - sonidos[S21] < 3000;
      if (!on) sonidos[S11] = 0;
    }
    else if (sonidos[S11])
    {
      on = millis() - sonidos[S11] < 300;
      if (!on) sonidos[S11] = 0;
    }
    else if (sonidos[S33])
    {
      on = millis() - sonidos[S33] < 6000;
      if (!on) sonidos[S33] = 0;
    }
    else if (sonidos[S35])
    {
      on = (millis() - sonidos[S35]) % 333 < 200;
    }
    else if (sonidos[S34])
    {
      on = (millis() - sonidos[S34]) % 1100 < 300;
    }
    else if (sonidos[S31])
    {
      on = (millis() - sonidos[S31]) % 850 < 250;
    }
    if (on && !sonando) play();
    else if (!on && sonando) stop();
    sonando = on;
  }
  void play()
  {
    if (Modo == AVISADOR_ASFAB_MONOTONO) digitalWrite(PinAvisador, HIGH);
    else tone(PinAvisador, 2100);
  }
  void stop()
  {
    if (Modo == AVISADOR_ASFAB_MONOTONO) digitalWrite(PinAvisador, LOW);
    else noTone(PinAvisador);
  }
};
