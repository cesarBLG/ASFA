package com;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.swing.JOptionPane;

import dmi.DMI;
import dmi.Botones.Botón;
import dmi.Pantalla.ÚltimaInfo.Info;
import ecp.ASFA;
import ecp.Main;
import gnu.io.*;

public class Serial implements COM {

    private SerialPort sp;
    OutputStream Output;
    InputStream Input;
    public boolean Connected = false;

    @Override
    public void start() {
        //begin(115000);
    }

    @Override
    public void write(byte[] b) {
        if (Connected) {
            try {
                Output.write(b);
            } catch (IOException e) {
            }
        }
    }

    @Override
    public String read(int count) {
        byte[] data = new byte[count];
        try {
            Input.read(data, 0, count);
        } catch (IOException e) {
        }
        return data.toString();
    }

    @Override
    public int available() {
        return 0;
    }

    void begin(int BaudRate) {
        CommPortIdentifier portId = null;
        Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            if ("/dev/ttyACM0".equals(currPortId.getName()) || "COM5".equals(currPortId.getName())) {
                portId = currPortId;
                break;
            }
        }
        if (portId == null) {
            return;
        }
        try {
            sp = (SerialPort) portId.open("CTC", 2000);
            sp.setSerialPortParams(
                    BaudRate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            sp.setDTR(true);
            Output = sp.getOutputStream();
            Input = sp.getInputStream();
            sp.addEventListener(new SerialPortEventListener() {
                public void serialEvent(SerialPortEvent e) {
                    Receive();
                }
            });
            sp.notifyOnDataAvailable(true);
        } catch (Exception e) {
            return;
        }
        Connected = true;
    }
    boolean Missed = false;
    void Receive() {
        try {
        	if(Missed)
        	{
        		while(Input.read() != 0xFF) {}
        		Missed = false;
        	}
            if (Input.available() >= 3) {
                byte[] data = new byte[3];
                Input.read(data, 0, 3);
                if ((int) (data[2] & 0xFF) != 0xFF) {
                    Missed = true;
                    return;
                }
                COM.parse(data);
            }
        } catch (IOException e) {
        }
    }
}
