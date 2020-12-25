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
const int LuzBasico = 10; //Luz modo ASFA basico
const int LEDVerde = A5; //LED verde ASFA basico
const int LEDRojo = A4; //LED rojo ASFA basico
const int LEDFrenar = A3; //LED amarillo frenar ASFA basico
const int LEDEficacia = A2; //LED azul eficacia ASFA basico
const int AlimentacionPantalla = -1; //Alimentacion pantalla ASFA digital
const int AvisadorBasico = 11; //Avisador acustico ASFA básico

const bool invertirLuzPulsadores = true; //Algunos pulsadores adicionales tienen positivo común, por lo que se encienden cuando la entrada está a LOW

//Estado LEDs ASFA basico: 0 apagado, 1 encendido, 2 parpadeante
int estLEDVerde = 0;
int estLEDRojo = 0;
int estLEDFrenar = 0;
int estLEDEficacia = 0;

enum sonidos
{
  S11,
  S21,
  S31,
  S33,
  S34,
  S35,
  S5,
  S6
};

unsigned long sonidos[8];
bool sonando;

void setup() {
  // put your setup code here, to run once:
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
  pinMode(PAnPar, INPUT_PULLUP);
  pinMode(PAnPre, INPUT_PULLUP);
  pinMode(PPrePar, INPUT_PULLUP);
  pinMode(PRearme, INPUT_PULLUP);
  pinMode(PAlarma, INPUT_PULLUP);
  pinMode(PModo, INPUT_PULLUP);
  pinMode(POcultacion, INPUT_PULLUP);
  pinMode(PAumento, INPUT_PULLUP);
  pinMode(PRebase, INPUT_PULLUP);
  pinMode(PLVI, INPUT_PULLUP);
  pinMode(PPN, INPUT_PULLUP);
  pinMode(PConex, INPUT_PULLUP);
  pinMode(PBasico, INPUT_PULLUP);
  pinMode(LEDVerde, OUTPUT);
  pinMode(LEDRojo, OUTPUT);
  pinMode(LEDFrenar, OUTPUT);
  pinMode(LEDEficacia, OUTPUT);
  pinMode(AlimentacionPantalla, OUTPUT);
  pinMode(AvisadorBasico, OUTPUT);
  digitalWrite(LuzAnPar, HIGH);
  digitalWrite(LuzAnPre, HIGH);
  digitalWrite(LuzVL, HIGH);
  digitalWrite(LuzPrePar, HIGH);
  digitalWrite(LuzRearme, LOW);
  digitalWrite(LuzAlarma, LOW);
  digitalWrite(LuzConex, LOW);
  digitalWrite(LEDEficacia, LOW);
  digitalWrite(LEDFrenar, LOW);
  digitalWrite(LEDRojo, HIGH);
  digitalWrite(LEDVerde, HIGH);
  Serial.begin(115200);
  while (!Serial) {}
}
byte data[60]; //Buffer de datos
int windex = 0; //Indice de escritura en buffer
int reindex = 0; //Indice de lectura en buffer
bool con = 0; //Conectado al servidor de datos
void readSerial()
{
  if (reindex >= windex) //Si no hay datos sin leer, volver al inicio del buffer
  {
    windex = reindex = 0;
  }
  if (reindex > 20) //Si hay mas de 20 bytes libres a la izquierda, mover todo para aumentar espacio por la derecha
  {
    for (int i = reindex; i < windex; i++)
    {
      data[i - reindex] = data[i];
    }
    windex = windex - reindex;
    reindex = 0;
  }
  while (Serial.available() && windex < 60)
  {
    data[windex] = Serial.read();
    windex++;
  }
  for (int i = reindex; i < windex; i++)
  {
    if (data[i] == '\r' || data[i] == '\n') //Buscamos el final de linea
    {
      char line[i - reindex + 1];                // Creamos un null-terminated array que guarda la linea.
      memcpy(line, data + reindex, i - reindex); // Se podria evitar el memcpy y la reserva del array reutilizando el propio buffer data[]
      line[i - reindex] = 0;                     // (en ese caso no estaria null-terminated). Como es para un Arduino Mega, vamos sobrados y da igual.
      const char *value = strchr(line, '=') + 1; // Los mensajes llegan en el formato: asfa::pulsador::ilum::anpar=1, por lo que separamos comando y valor
      if (!strncmp(line, "asfa::pulsador::ilum::", 22))
      {
        const char *boton = line + 22;
        int pin = -1;
        bool inv = invertirLuzPulsadores;
        if (!strncmp(boton, "prepar", 6)) pin = LuzPrePar;
        else if (!strncmp(boton, "anpre", 5)) pin = LuzAnPre;
        else if (!strncmp(boton, "vlcond", 6)) pin = LuzVL;
        else if (!strncmp(boton, "anpar", 5)) pin = LuzAnPar;
        else
        {
          inv = 0;
          if (!strncmp(boton, "rearme", 6)) pin = LuzRearme;
          else if (!strncmp(boton, "alarma", 6)) pin = LuzAlarma;
          else if (!strncmp(boton, "modo", 4)) pin = LuzModo;
          else if (!strncmp(boton, "aumento", 7)) pin = LuzAumento;
          else if (!strncmp(boton, "rebase", 6)) pin = LuzRebase;
          else if (!strncmp(boton, "lvi", 3)) pin = LuzLVI;
          else if (!strncmp(boton, "pn", 2)) pin = LuzPN;
          else if (!strncmp(boton, "conex", 5)) pin = LuzConex;
        }
        int state = (value[0] == '1')^inv;
        digitalWrite(pin, state);
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
      else if (!strncmp(line, "asfa::sonido::iniciar", 21))
      {
        const char *val = line + 22;
        if (!strncmp(val, "S1-1,1", 6)) sonidos[S11] = millis();
        else if (!strncmp(val, "S2-1,1", 6)) sonidos[S21] = millis();
        else if (!strncmp(val, "S3-1,1", 6)) sonidos[S31] = millis();
        else if (!strncmp(val, "S3-3,1", 6)) sonidos[S33] = millis();
        else if (!strncmp(val, "S3-4,1", 6)) sonidos[S34] = millis();
        else if (!strncmp(val, "S3-5,1", 6)) sonidos[S35] = millis();
        else if (!strncmp(val, "S5,1", 4)) sonidos[S5] = millis();
        else if (!strncmp(val, "S6,1", 4)) sonidos[S6] = millis();
        con = 1;
      }
      else if (!strncmp(line, "asfa::sonido::detener", 21))
      {
        const char *val = line + 22;
        if (!strncmp(val, "S1-1", 4)) sonidos[S11] = 0;
        else if (!strncmp(val, "S2-1", 4)) sonidos[S21] = 0;
        else if (!strncmp(val, "S3-1", 4)) sonidos[S31] = 0;
        else if (!strncmp(val, "S3-3", 4)) sonidos[S33] = 0;
        else if (!strncmp(val, "S3-4", 4)) sonidos[S34] = 0;
        else if (!strncmp(val, "S3-5", 4)) sonidos[S35] = 0;
        else if (!strncmp(val, "S5", 2)) sonidos[S5] = 0;
        else if (!strncmp(val, "S6", 2)) sonidos[S6] = 0;
        con = 1;
      }
      else if (!strncmp(line, "connected", 9))
      {
        Serial.println("register(asfa::pulsador::ilum::*)");
        Serial.println("register(asfa::leds::*)");
        Serial.println("register(asfa::pantalla::iniciar)");
        Serial.println("register(asfa::pantalla::apagar)");
        Serial.println("register(asfa::sonido::iniciar)");
        Serial.println("register(asfa::sonido::detener)");
        con = 1;
      }
      reindex = i + 1;
      break;
    }
    else if (i + 1 == 60) windex = reindex = 0; // El buffer no permite lineas de mas de 60 caracteres, si es el caso borramos todo
  }
}
// Estado pulsadores, para enviar los datos solo cuando cambian
int anpar_state = 1;
int anpre_state = 1;
int prepar_state = 1;
int rearme_state = 1;
int alarma_state = 1;
int modo_state = 1;
int ocultacion_state = 1;
int lvi_state = 1;
int pn_state = 1;
int rebase_state = 1;
int aumento_state = 1;
int conex_state = 1;
int basico_state = 1;

void avisador()
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
  if (on && !sonando) tone(AvisadorBasico, 2000);
  else if (!on && sonando) noTone(AvisadorBasico);
  sonando = on;
}

unsigned long last = 0;
void loop() {
  if (!con && last + 500 < millis()) //Pedimos periodicamente los parametros que queremos leer, hasta que recibamos alguno
  {
    Serial.println("register(asfa::pulsador::ilum::*)");
    Serial.println("register(asfa::leds::*)");
    Serial.println("register(asfa::pantalla::iniciar)");
    Serial.println("register(asfa::pantalla::apagar)");
    Serial.println("register(asfa::sonido::iniciar)");
    Serial.println("register(asfa::sonido::detener)");
    last = millis();
  }
  readSerial();
  avisador();
  if (anpar_state != digitalRead(PAnPar))
  {
    anpar_state = digitalRead(PAnPar);
    Serial.print("asfa::pulsador::anpar=");
    Serial.println(anpar_state ? "0" : "1");
  }
  if (anpre_state != digitalRead(PAnPre))
  {
    anpre_state = digitalRead(PAnPre);
    Serial.print("asfa::pulsador::anpre=");
    Serial.println(anpre_state ? "0" : "1");
  }
  if (prepar_state != digitalRead(PPrePar))
  {
    prepar_state = digitalRead(PPrePar);
    Serial.print("asfa::pulsador::prepar=");
    Serial.println(prepar_state ? "0" : "1");
  }
  if (rearme_state != digitalRead(PRearme))
  {
    rearme_state = digitalRead(PRearme);
    Serial.print("asfa::pulsador::rearme=");
    Serial.println(rearme_state ? "0" : "1");
  }
  if (modo_state != digitalRead(PModo))
  {
    modo_state = digitalRead(PModo);
    Serial.print("asfa::pulsador::modo=");
    Serial.println(modo_state ? "0" : "1");
  }
  if (alarma_state != digitalRead(PAlarma))
  {
    alarma_state = digitalRead(PAlarma);
    Serial.print("asfa::pulsador::alarma=");
    Serial.println(alarma_state ? "0" : "1");
  }
  if (ocultacion_state != digitalRead(POcultacion))
  {
    ocultacion_state = digitalRead(POcultacion);
    Serial.print("asfa::pulsador::ocultacion=");
    Serial.println(ocultacion_state ? "0" : "1");
  }
  if (rebase_state != digitalRead(PRebase))
  {
    rebase_state = digitalRead(PRebase);
    Serial.print("asfa::pulsador::rebase=");
    Serial.println(rebase_state ? "0" : "1");
  }
  if (aumento_state != digitalRead(PAumento))
  {
    aumento_state = digitalRead(PAumento);
    Serial.print("asfa::pulsador::aumento=");
    Serial.println(aumento_state ? "0" : "1");
  }
  if (lvi_state != digitalRead(PLVI))
  {
    lvi_state = digitalRead(PLVI);
    Serial.print("asfa::pulsador::lvi=");
    Serial.println(lvi_state ? "0" : "1");
  }
  if (pn_state != digitalRead(PPN))
  {
    pn_state = digitalRead(PPN);
    Serial.print("asfa::pulsador::pn=");
    Serial.println(pn_state ? "0" : "1");
  }
  if (conex_state != digitalRead(PConex))
  {
    conex_state = digitalRead(PConex);
    Serial.print("asfa::pulsador::conex=");
    Serial.println(conex_state ? "0" : "1");
    digitalWrite(AlimentacionPantalla, !conex_state);
  }
  if (basico_state != digitalRead(PBasico))
  {
    basico_state = digitalRead(PBasico);
    Serial.print("asfa::pulsador::basico=");
    Serial.println(basico_state ? "0" : "1");
    digitalWrite(LuzBasico, !basico_state);
  }
  digitalWrite(LEDEficacia, estLEDEficacia == 1 ? HIGH : (estLEDEficacia == 2 ? (millis() / 500) % 2 : LOW));
  digitalWrite(LEDFrenar, estLEDFrenar == 1 ? HIGH : (estLEDFrenar == 2 ? (millis() / 500) % 2 : LOW));
  digitalWrite(LEDRojo, estLEDRojo == 1 ? HIGH : (estLEDRojo == 2 ? (millis() / 500) % 2 : LOW));
  digitalWrite(LEDVerde, estLEDVerde == 1 ? HIGH : (estLEDVerde == 2 ? (millis() / 500) % 2 : LOW));
}
