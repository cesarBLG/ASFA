#!/usr/bin/python
import os, socket, threading, time
import RPi.GPIO as GPIO

c = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
while True:
    try:
        c.connect(('localhost', 5092))
        break
    except:
        continue
    
GPIO.setmode(GPIO.BCM) #Inicializamos el GPIO de la raspberry
GPIO.setup(17, GPIO.IN, pull_up_down=GPIO.PUD_UP) #Usamos el puerto 17 del GPIO como entrada para modo Noche y dia
GPIO.setup(27, GPIO.IN, pull_up_down=GPIO.PUD_UP) #Usamos el puerto 27 del GPIO como entrada para Subir Brillo/Contraste
GPIO.setup(22, GPIO.IN, pull_up_down=GPIO.PUD_UP) #Usamos el puerto 22 del GPIO como entrada para Bajar Brillo/Contraste

status = False
Brightness = 0

def setBrightness(value):
    global Brightness
    if value > 255:
        value = 255
    if value < 150:
        value = 150
    print(str(Brightness))
    os.system("sudo bash -c 'echo" +" "+ str(Brightness) +" "+ "> /sys/class/backlight/rpi_backlight/brightness'")
    Brightness = value

def setModo(modo):
    if modo:
        print("Noche")
        c.send("n".encode())
    else:
        print("Dia")
        c.send("d".encode())

def cambiarModo(pin):
    print("Cambio")
    c.send("s".encode())
def aumentarBrillo(pin):
    global Brightness
    setBrightness(Brightness + 5)
def disminuirBrillo(pin):
    global Brightness
    setBrightness(Brightness - 5)
    
setBrightness(150)

GPIO.add_event_detect(17, GPIO.FALLING, callback=cambiarModo, bouncetime=500)
GPIO.add_event_detect(27, GPIO.FALLING, callback=aumentarBrillo, bouncetime=500)
GPIO.add_event_detect(22, GPIO.FALLING, callback=disminuirBrillo, bouncetime=500)

while True:
    time.sleep(1)
