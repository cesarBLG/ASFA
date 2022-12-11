// ASFA Digital
// Copyright (C) César Benito e Iván Izquierdo

// Código para el arduino dedicado al subsistema de actuación y presentación de informaciones en cabina
// Controla directamente los pulsadores y LEDs del panel repetidor y pulsadores adicionales, así como
// el avisador acústico utilizado en el modo ASFA básico.
// También envía los comandos necesarios para reproducir los sonidos del avisador de la pantalla de visualización
// También reenvía los datos de control del display procedentes del ECP (Serial0) a la Rasperry que controla
// la pantalla a través del Serial2
//#define TEXT_SERIAL
#include "Avisador_ASFAD.h"
#include "SerialInterface.h"
byte datosDIV[64];
bool DIVconectado = false;
const int PAnPar = 31; //Pulsador anuncio parada
const int PAnPre = 33; //Pulsador anuncio precaucion
const int PPrePar = 37; //Pulsador preanuncio parada/via libre condicional
const int PRearme = 3; //Pulsador rearme freno
const int PAlarma = 4; //Pulsador alarma
const int PModo = 2; //Pulsador modo
const int POcultacion = 5; //Pulsador ocultacion
const int PRebase = 6; //Pulsador rebase
const int PLVI = 8; //Pulsador limitacion velocidad por infraestructura
const int PPN = 9; //Pulsador paso a nivel
const int PAumento = 7; //Pulsador aumento de velocidad
const int PConex = A1;
const int PBasico = 12; //Selector ASFA basico
const int AlimentacionPantalla = 50; //Alimentacion pantalla ASFA digital
const int HabilitacionPantalla = 52; //Habilitacion pantalla ASFA digital
const int PinAvisadorBasico = 11; //Avisador acustico ASFA básico
const int LuzBasico = 10; //Luz modo ASFA basico, no controlada

#ifdef TCL_DRIVER
// Utilizar estos valores si los LEDs se controlan con un registro de desplazamiento
const int LuzAnPar = 1; //Luz pulsador anuncio parada
const int LuzAnPre = 2; //Luz pulsador anuncio precaucion
const int LuzVL = 3; //Luz verde pulsador preanuncio/VL
const int LuzPrePar = 4; //Luz amarilla pulsador preanuncio/VL
const int LuzRearme = 5; //Luz pulsador rearme (azul)
const int LuzAlarma = 6; //Luz pulsador alarma (rojo)
const int LuzModo = 7; //Luz pulsador modo (blanco)
const int LuzRebase = 8; //Luz pulsador rebase
const int LuzLVI = 9; //Luz pulsador limitacion velocidad por infraestructura
const int LuzPN = 10; //Luz pulsador paso a nivel
const int LuzAumento = 11; //Luz pulsador aumento velocidad
const int LEDVerde = 14; //LED verde ASFA basico
const int LEDRojo = 15; //LED rojo ASFA basico
const int LEDFrenar = 16; //LED amarillo frenar ASFA basico
const int LEDEficacia = 17; //LED azul eficacia ASFA basico
const int LuzConex = 18; //Luz pulsador conexion
#else
// Pines del arduino, modificar según las conexiones. Válido para conexion directa.
const int LuzAnPar = 51; //Luz pulsador anuncio parada
const int LuzAnPre = 47; //Luz pulsador anuncio precaucion
const int LuzVL = 49; //Luz verde pulsador preanuncio/VL
const int LuzPrePar = 45; //Luz amarilla pulsador preanuncio/VL
const int LuzRearme = 23; //Luz pulsador rearme (azul)
const int LuzAlarma = 27; //Luz pulsador alarma (rojo)
const int LuzModo = 25; //Luz pulsador rearme (azul)
const int LuzRebase = 24; //Luz pulsador rebase
const int LuzLVI = 22; //Luz pulsador limitacion velocidad por infraestructura
const int LuzPN = 26; //Luz pulsador paso a nivel
const int LuzAumento = 28; //Luz pulsador aumento velocidad
const int LuzConex = A0; //Luz pulsador conexion
const int LEDVerde = A5; //LED verde ASFA basico
const int LEDRojo = A4; //LED rojo ASFA basico
const int LEDFrenar = A3; //LED amarillo frenar ASFA basico
const int LEDEficacia = A2; //LED azul eficacia ASFA basico
const bool invertirLuzPulsadores = true; //LEDs con ánodo común, se encienden en LOW
#endif
#include "LED_Driver.h"
#ifdef TCL_DRIVER
TCL5928LEDDriver leds(1, 30, 32, 34, 36);
#else
LEDDriver leds;
#endif
#ifdef TEXT_SERIAL
class Pulsador
{
  int pin;
  const char *nombre;
  public:
  bool pulsado;
  Pulsador(int pin, const char *nombre) : pin(pin), nombre(nombre)
  {
  }
  void setup()
  {
    pulsado = false;
    pinMode(pin, INPUT_PULLUP);
  }
  void update()
  {
    bool nuevoEstado = !digitalRead(pin);
    if (pulsado != nuevoEstado)
    {
      pulsado = nuevoEstado;
      Serial.print("asfa::pulsador::");
      Serial.print(nombre);
      Serial.print('=');
      Serial.println(pulsado ? '1' : '0');
    }
  }
};

Pulsador pulsadores[] = {Pulsador(PAnPar, "anpar"),
Pulsador(PAnPre, "anpre"),
Pulsador(PPrePar, "prepar"),
Pulsador(PConex, "conex"),
Pulsador(PBasico, "basico"),
Pulsador(PModo, "modo"),
Pulsador(PRearme, "rearme"),
Pulsador(PAlarma, "alarma"),
Pulsador(POcultacion, "ocultacion"),
Pulsador(PRebase, "rebase"),
Pulsador(PAumento, "aumento"),
Pulsador(PLVI, "lvi"),
Pulsador(PPN, "pn")};
#else
short PinesPulsadores[] = {PModo, PRearme, PRebase, PAumento, PAlarma, POcultacion, PLVI, PPN, PAnPar,PAnPre, PPrePar, PConex, PBasico};
bool EstadoPulsadores[13];
#endif

//Estado LEDs ASFA basico: 0 apagado, 1 encendido, 2 parpadeante
int estLEDVerde = 0;
int estLEDRojo = 0;
int estLEDFrenar = 0;
int estLEDEficacia = 0;

// Zumbador ASFA basico
// AVISADOR_ASFAB_PIEZO genera una onda de 2000Hz (usar con zumbadores piezoelectricos)
// AVISADOR_ASFAB_MONOTONO pone a HIGH o LOW el pin del avisador (usar con zumbadores calibrados a 2000Hz, como el original de EAO)
AvisadorBasico avisadorBasico(PinAvisadorBasico, AVISADOR_ASFAB_PIEZO);
                                                                        
void setup() {
  leds.begin();
  #ifdef TEXT_SERIAL
  for (int i=0; i<sizeof(pulsadores)/sizeof(Pulsador); i++)
  {
    pulsadores[i].setup();
  }
  #else
  for (int i=0; i<sizeof(PinesPulsadores)/sizeof(short); i++)
  {
    pinMode(PinesPulsadores[i], INPUT_PULLUP);
  }
  Serial2.begin(115200);
  #endif
  digitalWrite(AlimentacionPantalla, HIGH);
  pinMode(AlimentacionPantalla, OUTPUT);
  pinMode(HabilitacionPantalla, OUTPUT);
  Serial.begin(115200);
  Serial3.begin(115200);
  digitalWrite(HabilitacionPantalla, LOW);
  Serial1.begin(9600, SERIAL_8E1);
  while (!Serial) {}
}
#ifdef TEXT_SERIAL
bool con = 0; //Conectado al servidor de datos
void serialDataHandler(const char *line, const char *value)
{
  if (!strncmp(line, "asfa::pulsador::ilum::", 22))
  {
    const char *boton = line + 22;
    int pin = -1;
    if (!strncmp(boton, "prepar", 6)) pin = LuzPrePar;
    else if (!strncmp(boton, "anpre", 5)) pin = LuzAnPre;
    else if (!strncmp(boton, "vlcond", 6)) pin = LuzVL;
    else if (!strncmp(boton, "anpar", 5)) pin = LuzAnPar;
    else if (!strncmp(boton, "rearme", 6)) pin = LuzRearme;
    else if (!strncmp(boton, "alarma", 6)) pin = LuzAlarma;
    else if (!strncmp(boton, "modo", 4)) pin = LuzModo;
    else if (!strncmp(boton, "aumento", 7)) pin = LuzAumento;
    else if (!strncmp(boton, "rebase", 6)) pin = LuzRebase;
    else if (!strncmp(boton, "lvi", 3)) pin = LuzLVI;
    else if (!strncmp(boton, "pn", 2)) pin = LuzPN;
    else if (!strncmp(boton, "conex", 5)) pin = LuzConex;
    int state = (value[0] == '1');
    leds.setLed(pin, state);
    con = 1;
  }
  else if (!strncmp(line, "asfa::leds::", 12))
  {
    int num = line[12] - '0';
    int state = line[14] - '0';
    if (num == 0) estLEDEficacia = state;
    else if (num == 1) estLEDFrenar = state;
    else if (num == 2)
    {
      int val = 0;
      if (state != 0) val = 1;
      if ((state & 2) != 0) val = 2;
      if (state > 2)
      {
        estLEDVerde = 0;
        estLEDRojo = val;
      }
      else
      {
        estLEDRojo = 0;
        estLEDVerde = val;
      }
    }    
    else if (num == 3)
    {
      estLEDRojo = 0;
      estLEDVerde = state;
    }
    con = 1;
  }
  else if (!strncmp(line, "asfa::pantalla::iniciar", 24))
  {
    digitalWrite(AlimentacionPantalla, HIGH);
    con = 1;
  }
  else if (!strncmp(line, "asfa::pantalla::apagar", 24))
  {
    digitalWrite(AlimentacionPantalla, LOW);
    con = 1;
  }
  else if (!strncmp(line, "asfa::pantalla::activa", 24))
  {
    digitalWrite(HabilitacionPantalla, *value == '0' ? LOW : HIGH);
    con = 1;
  }
  else if (!strncmp(line, "asfa::sonido::iniciar", 21))
  {
    int son = 0;
    const char *c = strchr(value, ',') + 1;
    bool bas = *c == '1';
    if (!strncmp(value, "S1-1", 4)) son = S11;
    else if (!strncmp(value, "S2-1", 4)) son = S21;
    else if (!strncmp(value, "S2-2", 4)) son = S22;
    else if (!strncmp(value, "S2-3", 4)) son = S23;
    else if (!strncmp(value, "S2-4", 4)) son = S24;
    else if (!strncmp(value, "S2-5", 4)) son = S25;
    else if (!strncmp(value, "S2-6", 4)) son = S26;
    else if (!strncmp(value, "S3-1", 4)) son = S31;
    else if (!strncmp(value, "S3-2", 4)) son = S32;
    else if (!strncmp(value, "S3-3", 4)) son = S33;
    else if (!strncmp(value, "S3-4", 4)) son = S34;
    else if (!strncmp(value, "S3-5", 4)) son = S35;
    else if (!strncmp(value, "S4", 2)) son = S4;
    else if (!strncmp(value, "S5", 2)) son = S5;
    else if (!strncmp(value, "S6", 2)) son = S6;
    if (bas) avisadorBasico.iniciar(son);
    else Serial3.println(line);
    con = 1;
  }
  else if (!strncmp(line, "asfa::sonido::detener", 21))
  {
    int son = 0;
    if (!strncmp(value, "S1-1", 4)) son = S11;
    else if (!strncmp(value, "S2-1", 4)) son = S21;
    else if (!strncmp(value, "S2-2", 4)) son = S22;
    else if (!strncmp(value, "S2-3", 4)) son = S23;
    else if (!strncmp(value, "S2-4", 4)) son = S24;
    else if (!strncmp(value, "S2-5", 4)) son = S25;
    else if (!strncmp(value, "S2-6", 4)) son = S26;
    else if (!strncmp(value, "S3-1", 4)) son = S31;
    else if (!strncmp(value, "S3-2", 4)) son = S32;
    else if (!strncmp(value, "S3-3", 4)) son = S33;
    else if (!strncmp(value, "S3-4", 4)) son = S34;
    else if (!strncmp(value, "S3-5", 4)) son = S35;
    else if (!strncmp(value, "S4", 2)) son = S4;
    else if (!strncmp(value, "S5", 2)) son = S5;
    else if (!strncmp(value, "S6", 2)) son = S6;
    Serial3.println(line);
    avisadorBasico.detener(son);
    con = 1;
  }
  else if (!strncmp(line, "connected", 9))
  {
    Serial.println("register(asfa::pulsador::ilum::*)");
    Serial.println("register(asfa::leds::*)");
    Serial.println("register(asfa::pantalla::iniciar)");
    Serial.println("register(asfa::pantalla::apagar)");
    Serial.println("register(asfa::pantalla::activa)");
    Serial.println("register(asfa::sonido::iniciar)");
    Serial.println("register(asfa::sonido::detener)");
    con = 1;
  }
}
SerialInterface serialInterface(Serial, serialDataHandler);

unsigned long last = 0;
void loop_text()
{
  if (!con && last + 500 < millis()) //Pedimos periodicamente los parametros que queremos leer, hasta que recibamos alguno
  {
    Serial.println("register(asfa::pulsador::ilum::*)");
    Serial.println("register(asfa::leds::*)");
    Serial.println("register(asfa::pantalla::iniciar)");
    Serial.println("register(asfa::pantalla::apagar)");
    Serial.println("register(asfa::pantalla::activa)");
    Serial.println("register(asfa::sonido::iniciar)");
    Serial.println("register(asfa::sonido::detener)");
    last = millis();
  }
  for (int i=0; i<sizeof(pulsadores)/sizeof(Pulsador); i++)
  {
    pulsadores[i].update();
  }
  serialInterface.update();
  avisadorBasico.update();
}
#else
void parseLeds(byte *data)
{
  short ids[] = {LuzModo, LuzRearme, LuzRebase, LuzAumento, LuzAlarma, -1, LuzLVI, LuzPN, LuzAnPar, LuzAnPre, LuzPrePar, LuzVL, LuzConex};
  int count = sizeof(ids)/sizeof(short);
  for (int i=0; i<count; i++)
  {
    if (ids[i] >= 0) leds.setLed(ids[i], data[i/8] & 1);
    data[i/8] >>= 1;
  }
  estLEDEficacia = data[2] & 3;
  estLEDFrenar = (data[2]>>2) & 3;
  estLEDRojo = (data[2]>>4) & 3;
  estLEDVerde = (data[2]>>6) & 3;
}
void parseSound(byte *data)
{
  bool bas = (data[0]>>5) & 1;
  bool start = (data[0]>>6) & 1;
  int son = data[0] & 31;
  if (start)
  {
    if (bas) avisadorBasico.iniciar(son);
  }
  else
  {
    avisadorBasico.detener(son);
  }
}
BinaryInterface pantallaInterface(Serial2, bridgeParser);
BinaryInterface avisadorInterface(Serial3, bridgeParser);
void dataParser(int num, byte *data, int length)
{
  switch(num)
  {
    case 0:
      parseLeds(data);
      break;
    case 2:
    case 4:
      pantallaInterface.write(num, data, length);
      break;
    case 3:
      parseSound(data);
      avisadorInterface.write(num, data, length);
      pantallaInterface.write(num, data, length);
      break;
    case 5:
      digitalWrite(AlimentacionPantalla, !(data[0]&1));
      digitalWrite(HabilitacionPantalla, !((data[0]>>1)&1));
      pantallaInterface.write(num, data, length);
      /*if (data[0] & 1) Serial3.begin(115200);
      else Serial3.end();*/
      break;
    default:
      break;
  }
}
BinaryInterface ecpInterface(Serial, dataParser);
void bridgeParser(int num, byte *data, int length)
{
  ecpInterface.write(num, data, length);
}

void sendPulsadores()
{
  byte data[2];
  int count = sizeof(PinesPulsadores)/sizeof(short);
  data[0] = data[1] = 0;
  for (int i=0; i<count; i++)
  {
    data[i/8] |= !digitalRead(PinesPulsadores[i])<<(i%8);
  }
  ecpInterface.write(1, data, 2);
}
unsigned long lastSentPulsadores = 0;
void loop_binary()
{
  if (Serial1.available())
  {
    int num = Serial1.readBytes(datosDIV, 64);
    if (num == 64)
    {
      DIVconectado = true;
      ecpInterface.write(8, datosDIV, 64);
    }
  }
  bool send = false;
  for (int i=0; i<sizeof(PinesPulsadores)/sizeof(short); i++)
  {
    if (EstadoPulsadores[i] != !digitalRead(PinesPulsadores[i]))
    {
      send = true;
      EstadoPulsadores[i] = !digitalRead(PinesPulsadores[i]);
    }
  }
  if (lastSentPulsadores + 50 < millis() || send)
  {
    sendPulsadores();
    lastSentPulsadores = millis();
  }
  ecpInterface.update();
  pantallaInterface.update();
}
#endif
void loop() {
  #ifdef TEXT_SERIAL
  loop_text();
  #else
  loop_binary();
  #endif
  avisadorBasico.update();
  leds.update();
  
  leds.setLed(LEDEficacia, estLEDEficacia == 1 ? HIGH : (estLEDEficacia == 2 ? (millis() / 500) % 2 : LOW));
  leds.setLed(LEDFrenar, estLEDFrenar == 1 ? HIGH : (estLEDFrenar == 2 ? (millis() / 250) % 2 : LOW));
  leds.setLed(LEDRojo, estLEDRojo == 1 ? HIGH : (estLEDRojo == 2 ? (millis() / 250) % 2 : LOW));
  leds.setLed(LEDVerde, estLEDVerde == 1 ? HIGH : (estLEDVerde == 2 ? (millis() / 250) % 2 : LOW));
  
  digitalWrite(LuzBasico, !digitalRead(PBasico));
}
