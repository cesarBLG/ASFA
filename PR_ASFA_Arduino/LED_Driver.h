class TCL5928LEDDriver
{
  bool *buffer;
  bool *errors;
  int NDrivers;
  int SDI;
  int SDO;
  int SCLK;
  int LAT;
  bool refresh;
  unsigned long lastRead = false;
  public:
  TCL5928LEDDriver(int nDrivers, int sdi, int sdo, int sclk, int lat)
  {
    SDI = sdi;
    SDO = sdo;
    SCLK = sclk;
    LAT = lat;
    NDrivers = nDrivers;
    refresh = false;
  }
  ~TCL5928LEDDriver()
  {
    delete[] buffer;
    delete[] errors;
  }
  void begin()
  {
    buffer = new bool[NDrivers*16];
    errors = new bool[NDrivers*16];
    for (int i=0; i<NDrivers*16; i++)
    {
      buffer[i] = errors[i] = 0;
    }
    pinMode(SDI, OUTPUT);
    pinMode(SCLK, OUTPUT);
    pinMode(LAT, OUTPUT);
    pinMode(SDO, INPUT);
    digitalWrite(SDI, LOW);
    digitalWrite(SCLK, LOW);
    digitalWrite(LAT, LOW);
    send();
  }
  void setLed(int num, bool state)
  {
    refresh = buffer[num] != state;
    buffer[num] = state;
  }
  void push(int value)
  {
      digitalWrite(SDI, value);
      delayMicroseconds(1);
      digitalWrite(SCLK, HIGH);
      delayMicroseconds(1);
      digitalWrite(SCLK, LOW);
      delayMicroseconds(1);
  }
  void send()
  {
    for (int i=0; i<16*NDrivers; i++)
    {
      push(buffer[i]);
    }
    digitalWrite(LAT, HIGH);
    delayMicroseconds(1);
    digitalWrite(LAT, LOW);
    delayMicroseconds(1);
  }
  void readStatus()
  {
    digitalWrite(LAT, HIGH);
    delayMicroseconds(1);
    digitalWrite(LAT, LOW);
    delayMicroseconds(1);
    for (int i=0; i<16*NDrivers; i++)
    {
      digitalWrite(SDI, buffer[i]);
      errors[i] = digitalRead(SDO);
    }
  }
  void update()
  {
    if (refresh) send();
    else if (lastRead + 500 < millis())
    {
      lastRead = millis();
      readStatus();
    }
  }
};
class LEDDriver
{
  public:
  void begin()
  {
    //Si tienen +12V comun, activar las salidas del L298N (para GND comun activado siempre con jumper)
    if (invertirLuzPulsadores)
    {
      pinMode(46,OUTPUT);
      pinMode(48,OUTPUT);
      digitalWrite(46,HIGH);
      digitalWrite(48,HIGH);
    }
    pinMode(LuzAnPar, OUTPUT);
    pinMode(LuzAnPre, OUTPUT);
    pinMode(LuzVL, OUTPUT);
    pinMode(LuzPrePar, OUTPUT);
    pinMode(LuzRearme, OUTPUT);
    pinMode(LuzAlarma, OUTPUT);
    pinMode(LuzModo, OUTPUT);
    pinMode(LuzRebase, OUTPUT);
    pinMode(LuzLVI, OUTPUT);
    pinMode(LuzPN, OUTPUT);
    pinMode(LuzAumento, OUTPUT);
    pinMode(LuzConex, OUTPUT);
    pinMode(LuzBasico, OUTPUT);
    pinMode(LEDVerde, OUTPUT);
    pinMode(LEDRojo, OUTPUT);
    pinMode(LEDFrenar, OUTPUT);
    pinMode(LEDEficacia, OUTPUT);
    if (invertirLuzPulsadores)
    {
      digitalWrite(LuzAnPar, HIGH);
      digitalWrite(LuzAnPre, HIGH);
      digitalWrite(LuzVL, HIGH);
      digitalWrite(LuzPrePar, HIGH);
    }
    else
    {
      digitalWrite(LuzAnPar, LOW);
      digitalWrite(LuzAnPre, LOW);
      digitalWrite(LuzVL, LOW);
      digitalWrite(LuzPrePar, LOW);
    }
    digitalWrite(LuzRearme, LOW);
    digitalWrite(LuzAlarma, LOW);
    digitalWrite(LuzConex, LOW);
    digitalWrite(LEDEficacia, LOW);
    digitalWrite(LEDFrenar, LOW);
    digitalWrite(LEDRojo, HIGH);
    digitalWrite(LEDVerde, HIGH);
  }
  void update()
  {
    
  }
  void setLed(int pin, bool state)
  {
    if (invertirLuzPulsadores && (pin == LuzAnPar || pin == LuzAnPre || pin == LuzVL || pin == LuzPrePar)) state = !state;
    digitalWrite(pin, state);
  }
};
