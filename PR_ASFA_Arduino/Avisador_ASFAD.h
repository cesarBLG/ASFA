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
enum ErrorAvisador
{
  SUCCESS,
  ERR_MALFUNCTION,
  ERR_DISCONNECTED
};
class ControladorAvisador
{
  public:
  int volumen = 15;
  int status = SUCCESS;
  virtual void begin() = 0;
  virtual void play(int sonido) = 0;
  virtual void playLoop(int sonido) = 0;
  virtual void stop() = 0;
  virtual void setVolume(int vol) = 0;
  virtual bool canSend() = 0;
  virtual void update() = 0;
};
template<class T>
class ControladorOpenSmart : public ControladorAvisador
{
  T &serial;
  bool waitingAck=false;
  unsigned long ultimoEnvio = 0;
  void send(int num)
  {
    serial.write(0x7E);
    serial.write(2);
    serial.write(num);
    serial.write(0xEF);
    ultimoEnvio = millis();
    waitingAck=true;
    waitingPacketLength = 0;
  }
  void send(int num, int d1)
  {
    serial.write(0x7E);
    serial.write(3);
    serial.write(num);
    serial.write(d1);
    serial.write(0xEF);
    ultimoEnvio = millis();
    waitingAck=true;
    waitingPacketLength = 0;
  }
  void send(int num, int d1, int d2)
  {
    serial.write(0x7E);
    serial.write(4);
    serial.write(num);
    serial.write(d1);
    serial.write(d2);
    serial.write(0xEF);
    ultimoEnvio = millis();
    waitingAck=true;
    waitingPacketLength = 0;
  }
  public:
  ControladorOpenSmart(T &serial) : serial(serial) {}
  void begin()
  {
    serial.begin(9600);
    send(0x35, 0x01);
  }
  void play(int sonido)
  {
    send(0x31, volumen, sonido);
  }
  void playLoop(int sonido)
  {
    send(0x33,0,sonido);
  }
  void stop()
  {
    send(0x0E);
  }
  void setVolume(int vol)
  {
    volumen = vol;
    send(0x31, volumen);
  }
  bool canSend()
  {
    unsigned long diff = millis()-ultimoEnvio;
    return (!waitingAck || diff > 100) && diff > 50;
  }
  int waitingPacketLength = 0;
  unsigned long statusRequestTime = 0;
  unsigned long nextStateCheck = 0;
  void update()
  {
    if (canSend() && nextStateCheck < millis())
    {
      send(0x10);
      nextStateCheck = millis() + 10000;
      statusRequestTime = millis();
    }
    if (statusRequestTime > 0 && statusRequestTime + 1000 < millis())
    {
      status = ERR_DISCONNECTED;
    }
    if (waitingPacketLength > 0)
    {
      if (serial.available() >= waitingPacketLength)
      {
        byte data[waitingPacketLength-1];
        serial.readBytes(data, waitingPacketLength-1);
        for (int i=0; i<waitingPacketLength-1; i++)
        {
          /*Serial.print(data[i], HEX);
          Serial.print(" ");*/
        }
        int tail = serial.read();
        //Serial.println(tail, HEX);
        if (tail == 0xEF)
        {
          if (waitingPacketLength == 2 && data[0] == 0) waitingAck = false;
          if (waitingPacketLength == 3 && data[0] == 0x10)
          {
            statusRequestTime = 0;
            status = (data[1] == 0 || data[1] == 1) ? SUCCESS : ERR_MALFUNCTION;
          }
        }
        waitingPacketLength = 0;
      }
    }
    else if (serial.available() >= 2)
    {
      int head = serial.read();
      if (head != 0x7E) return;
      waitingPacketLength = serial.read();
      /*Serial.print(head, HEX);
      Serial.print(" ");
      Serial.print(waitingPacketLength, HEX);
      Serial.print(" ");*/
    }
  }
};
template<class T>
class ControladorDF3Mini : public ControladorAvisador
{
  T &serial;
  bool waitingAck=false;
  unsigned long ultimoEnvio = 0;
  void send(uint8_t command, uint16_t value = 0)
  {
     uint8_t out[] = {0x7E, 0xFF, 6, command, 1, value>>8, value&0xFF, 0, 0, 0xEF};
     uint16_t sum = 0;
     for (int i = 1; i<7; i++)
     {
      sum -= out[i];
     }
     out[7] = sum>>8;
     out[8] = sum & 0xFF;
     serial.write(out, 10);
     ultimoEnvio = millis();
     waitingAck = true;
  }
  public:
  ControladorDF3Mini(T &serial) : serial(serial) {}
  void begin()
  {
    serial.begin(9600);
    send(0x09, 1);
  }
  void play(int sonido)
  {
    send(0x03, sonido);
  }
  void playLoop(int sonido)
  {
    send(0x08, sonido);
  }
  void stop()
  {
    send(0x16);
  }
  void setVolume(int vol)
  {
    volumen = vol;
    send(0x06, volumen);
  }
  bool canSend()
  {
    unsigned long diff = millis()-ultimoEnvio;
    return (!waitingAck || diff > 100) && diff > 50;
  }
  unsigned long statusRequestTime = 0;
  unsigned long nextStateCheck = 0;
  void update()
  {
    if (canSend() && nextStateCheck < millis())
    {
      send(0x42);
      nextStateCheck = millis() + 10000;
      statusRequestTime = millis();
    }
    if (statusRequestTime > 0 && statusRequestTime + 1000 < millis())
    {
      status = ERR_DISCONNECTED;
    }
    if (serial.available() >= 10)
    {
      int head = serial.read();
      if (head != 0x7E) return;
      byte data[10];
      data[0] = head;
      serial.readBytes(data + 1, 9);
      if (data[1] != 0xFF || data[2] != 6 || data[9] != 0xEF) return;
      uint16_t sum = 0;
      for (int i = 1; i<7; i++)
      {
       sum -= data[i];
      }
      if ((sum>>8) != data[7] || (sum & 0xFF) != data[8]) return;
      switch(data[3])
      {
        case 0x40:
          status = ERR_MALFUNCTION;
        case 0x41:
          waitingAck = false;
          status = SUCCESS;
          break;
        case 0x42:
          statusRequestTime = 0;
          status = SUCCESS;
          break;
        default:
          break;
      }
    }
  }
};
class AvisadorASFAD
{
  unsigned long sonidos[16];
  bool enviado[16];
  int sonando;
  ControladorAvisador &controlador;
  public:
  int volumen = 15;
  bool eficacia = false;
  AvisadorASFAD(ControladorAvisador &c) : controlador(c)
  {
  }
  void begin()
  {
    for (int i=0; i<16; i++)
    {
      sonidos[i] = 0;
      enviado[i] = false;
    }
    sonando = 0;
    controlador.begin();
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
  unsigned long recordatorioPausa=0;
  void update()
  {
    controlador.update();
    eficacia = controlador.status == SUCCESS;
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
      if (son == 0)
      {
        stop();
        recordatorioPausa = millis();
      }
      else if (loop) playLoop(son);
      else play(son);
    }
    else if (controlador.volumen != volumen)
    {
      setVolume(volumen);
    }
    else if (recordatorioPausa + 500 < millis() && controlador.canSend() && sonando == 0)
    {
      stop();
      recordatorioPausa = millis(); 
    }
  }
  void play(int sonido) {
    if (!controlador.canSend())
    {
      if (!enviado[sonido])
      {
         sonidos[sonido] += 100;
         enviado[sonido] = true;
      }
      return;
    }
    enviado[sonido] = true;
    sonando = sonido;
    controlador.play(sonido);
  }
  void playLoop(int sonido) {
    if (!controlador.canSend()) return;
    sonando = sonido;
    controlador.playLoop(sonido);
  }
  void stop()
  {
    if (!controlador.canSend()) return;
    sonando = 0;
    controlador.stop();
  }
  void setVolume(int vol)
  {
    if (!controlador.canSend()) return;
    volumen = vol;
    controlador.setVolume(vol);
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
