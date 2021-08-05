package com;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;

import dmi.Pantalla.PantallaSerializer;
import ecp.Main;

public abstract class StateSerializer {
	protected enum TipoPaquete
	{
		LucesPR,
		PulsadoresPR,
		IconosDisplay,
		Sonido,
		EstadoECP,
		ConexionDisplay,
	}
	protected class Paquete
	{
		public TipoPaquete tipo;
		public byte[] data;
		public Paquete(TipoPaquete t, byte[] d)
		{
			tipo = t;
			data = d;
		}
	}
	boolean ready = false;
	String puerto;
	boolean setupSerial()
	{
		ready = false;
		try {
			SerialPort sp = SerialPort.getCommPort(puerto);
			sp.setBaudRate(115200);
			sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
			sp.setDTR();
			sp.openPort();
			in = sp.getInputStream();
			out = sp.getOutputStream();
			in.skip(in.available());
			return true;
		} catch (Exception e) {
			if (e instanceof SerialPortInvalidPortException) return false;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	public StateSerializer(String puerto)
	{
		this.puerto = puerto;
		if (puerto == null || puerto.isEmpty()) return;
		new Thread(() -> {
			while(!setupSerial())
			{
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			/*try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ready = true;*/
			read();
			}).start();
	}
	InputStream in;
	OutputStream out;
	protected void write(Paquete p)
	{
		byte[] packet = p.data;
		//try{Main.dmi.pantalla.serialClient.parse(p);}catch(Exception e) {}
		if (!ready) return;
		byte[] data = new byte[packet.length + 4];
		data[0] = (byte) 0xAD;
		data[1] = (byte) p.tipo.ordinal();
		data[2] = (byte) packet.length;
		System.arraycopy(packet, 0, data, 3, packet.length);

		byte control = 0;
		for (int i=1; i<data.length-1; i++)
		{
			control += data[i];
		}
		data[data.length-1] = control;
		try {
			out.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	void read()
	{
		while(true)
		{
			try {
				if ((in.read()&255) == 0xAD)
				{
					int pacno = in.read();
					int length = in.read();
					byte[] data = new byte[length];
					int offset = 0;
					while (offset < length) offset += in.read(data, offset, length-offset);
					byte control = (byte)in.read();
					byte control_expected = (byte)(pacno + length);
					for (int i=0; i<data.length; i++)
					{
						control_expected += data[i];
					}
					if (control == control_expected)
					{
						parse(new Paquete(TipoPaquete.values()[pacno], data));
						ready = true;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setupSerial();
				try {
					Thread.sleep(250);
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
		}
	}
	protected abstract void parse(Paquete paquete);
}
