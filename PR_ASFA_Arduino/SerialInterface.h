#define BUFFER_SIZE_SERIAL 128
class SerialInterface
{
  byte data[BUFFER_SIZE_SERIAL]; //Buffer de datos
  int windex = 0; //Indice de escritura en buffer
  int reindex = 0; //Indice de lectura en buffer
  void (*lineHandler)(const char *, const char *);
  Stream &serial;
  bool newData;
  public:
  SerialInterface(Stream &stream, void (*lineHandler)(const char *, const char *)) : lineHandler(lineHandler), serial(stream)
  {
    newData = false;
  }
  void update()
  {
    if (reindex >= windex) //Si no hay datos sin leer, volver al inicio del buffer
    {
      windex = reindex = 0;
    }
    if (reindex > 60) //Si hay mas de 60 bytes libres a la izquierda, mover todo para aumentar espacio por la derecha
    {
      memmove(data, data + reindex, windex-reindex);
      windex = windex - reindex;
      reindex = 0;
    }
    if (serial.available() && windex < BUFFER_SIZE_SERIAL)
    {
      windex += serial.readBytes(data + windex, min(serial.available(), BUFFER_SIZE_SERIAL-windex));
      newData = true;
    }
    if (newData)
    {
      newData = false;
      for (int i = reindex; i < windex; i++)
      {
        if (data[i] == '\r' || data[i] == '\n') //Buscamos el final de linea
        {
          if (i == reindex)
          {
            reindex = i + 1;
            continue;
          }
          char *line = data + reindex;
          line[i - reindex] = 0;
          const char *value = strchr(line, '=') + 1; // Los mensajes llegan en el formato: asfa::pulsador::ilum::anpar=1, por lo que separamos comando y valor
          lineHandler(line, value);
          reindex = i + 1;
        }
        else if (i + 1 >= BUFFER_SIZE_SERIAL) windex = reindex = 0; // El buffer no permite lineas de mas de BUFFER_SIZE_SERIAL caracteres, si es el caso borramos todo
        //if (serial.available() > windex-reindex) break;
      }
    }
  } 
};

class BinaryInterface
{
  Stream &serial;
  byte buffer[64];
  int reindex;
  int windex;
  void (*parse)(int, const byte *, int);
  void moveBuffer()
  {
    memmove(buffer, buffer + reindex, windex-reindex);
    windex = windex - reindex;
    reindex = 0;
  }
  public:
  BinaryInterface(Stream &stream, void (*parse)(int, const byte *, int)) : serial(stream), parse(parse)
  {
    reindex = windex = 0;
  }
  void write(int num, byte* data, int length)
  {
    byte control = num+length;
    for (int i=0; i<length; i++)
    {
      control += data[i];
    }
    serial.write(0xAD);
    serial.write(num);
    serial.write(length);
    serial.write(data, length);
    serial.write(control);
  }
  void update()
  {
    if (reindex >= windex) //Si no hay datos sin leer, volver al inicio del buffer
    {
      windex = reindex = 0;
    }
    if (serial.available() && windex < 64) windex += serial.readBytes(buffer + windex, min(serial.available(), 64-windex));
    while(reindex < windex)
    {
      int available = windex-reindex;
      if (buffer[reindex] == 0xAD)
      {
        if (reindex + 3 >= 64) moveBuffer();
        if (available < 3) break;
        byte *data = buffer+reindex;
        int length = data[2];
        if (reindex + length + 4 >= 64)
        {
          if (reindex == 0) windex = 0;
          else moveBuffer();
        }
        if (available < length + 4) break;
        byte control = data[length+3];
        byte control_expected = 0;
        for (int i=1; i<length+3; i++)
        {
          control_expected += data[i];
        }
        if (control_expected == control) parse(data[1], data+3, length);
        reindex += length + 4;
      }
      else reindex++;
    }
  }
};
