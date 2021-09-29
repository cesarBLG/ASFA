package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.swing.Timer;

import dmi.Sonidos;
import dmi.Botones.Botón;
import dmi.Botones.Botón.TipoBotón;
import dmi.Pantalla.ÚltimaInfo.Info;
import ecp.Clock;
import ecp.FrecASFA;
import ecp.Main;
import ecp.Odometer;
import ecp.ASFA;

public class OR_Client {
	ASFA ASFA;
	Socket s;
	OutputStream out;
	BufferedReader in;
	static boolean startServer=true;
	public static Socket getSocket()
	{
		Socket s = null;
		while(s==null)
		{
			String ip = "localhost";
			try
			{
				DatagramSocket ds = new DatagramSocket(null);
				ds.setBroadcast(true);
				ds.bind(new InetSocketAddress("0.0.0.0", 5091));
				ds.setSoTimeout(1000);
				byte[] buff = new byte[50];
				DatagramPacket packet = new DatagramPacket(buff, buff.length);
				ds.receive(packet);
				ip = packet.getAddress().getCanonicalHostName();
				ds.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			try
			{
				s = new Socket(ip, 5090);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if(s == null || !s.isConnected())
			{
				s = null;
				if (startServer)
				{
			    	try {
						Runtime.getRuntime().exec("./server");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    	startServer = false; 
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return s;
	}
	void setup()
	{
		s = getSocket();
		try {
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			out = s.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		subscribe("asfa::frecuencia");
		subscribe("asfa::selector_tipo");
		subscribe("asfa::cg::conectado");
		subscribe("asfa::cg::anulado");
		subscribe("asfa::akt");
		subscribe("asfa::con");
		subscribe("asfa::pulsador::*");
		subscribe("speed");
		subscribe("simulator_time");
		subscribe("asfa::pantalla::conectada");
		subscribe("asfa::pantalla::activa");
		while(true)
		{
			String s = readData();
			if (s == null) return;
			parse(s);
		}
	}
	public OR_Client(ASFA asfa)
	{
		ASFA = asfa;
		new Thread(() -> {
			while (true)
			{
				setup();
				in = null;
				out = null;
				s = null;
			}
		}).start();
	}
	void parse(String s)
	{
		if(s==null) return;
		if (matches(s, "asfa::conectado")) sendData("asfa::conectado="+(ASFA.ASFAconectado&&!ASFA.ASFAanulado?"1":"0"));
		if (matches(s, "asfa::fase")) sendData("asfa::fase=" + (ASFA.Fase2 ? "2" : "1"));
		if (matches(s, "asfa::ecp::estado") && !ASFA.display.estadoecp.isEmpty()) sendData("asfa::ecp::estado=" + ASFA.display.estadoecp);
		if (matches(s, "asfa::pantalla::habilitada")) sendData("asfa::pantalla::habilitada=" + (ASFA.display.pantallahabilitada ? 1 : 0));
		if (matches(s, "asfa::dmi::activo")) sendData("asfa::dmi::activo=" + (ASFA.Connected ? 1 : 0));
		int index = s.indexOf('=');
		if (index < 0) return;
		String[] topics = s.substring(0, index).split("::");
		String val = s.substring(index+1);
		if(s.startsWith("asfa::frecuencia="))
		{
			FrecASFA f = FrecASFA.AL;
			try
			{
				int freqHz = (int) Double.parseDouble(val);
				f = ASFA.captador.procesarFrecuencia(freqHz);
			}
			catch (NumberFormatException e1)
			{
				try
				{
					 f = FrecASFA.valueOf(val);
				}
				catch(IllegalArgumentException e)
				{
					e.printStackTrace(); 
				}
			}
            ASFA.captador.nuevaFrecuencia(f);
		}
		else if(s.startsWith("speed="))
		{
			Odometer.speed = (float) Float.parseFloat(val.replace(',', '.')) / 3.6;
		}
		else if(s.startsWith("asfa::pulsador::"))
		{
			String pul = topics[2];
			TipoBotón tb = null;
			if(pul.equals("aumento")) tb = TipoBotón.AumVel;
			else if(pul.equals("ocultacion")) tb = TipoBotón.Ocultación;
			else if(pul.equals("basico")) tb = TipoBotón.ASFA_básico;
			else for(TipoBotón t : TipoBotón.values())
			{
				if(t.name().toLowerCase().equals(pul))
				{
					tb = t;
					break;
				}
			}
			if (tb == null) return;
            ASFA.display.pulsar(tb, val.equals("1"));
            if(tb==TipoBotón.Conex)
            {
            	synchronized(ASFA)
            	{
            		ASFA.notify();
            	}
            }
            if(tb==TipoBotón.PrePar) ASFA.display.pulsar(TipoBotón.VLCond, val.equals("1"));
		}
		else if(s.startsWith("simulator_time="))
		{
			Clock.set_external_time(Double.parseDouble(val.replace(',','.')));
		}
		else if(s.startsWith("asfa::div="))
		{
			byte[] DIV = new byte[64];
			for(int i=0; i<64; i++)
			{
				DIV[i] = Integer.decode("0x"+val.substring(2*i, 2*i+2)).byteValue();
			}
			ASFA.div.setData(DIV, 0);
		}
		else if(s.startsWith("asfa::akt="))
		{
			ASFA.AKT = val.equals("1");
		}
		else if(s.startsWith("asfa::con="))
		{
			ASFA.CON = !val.equals("0");
		}
		else if(s.startsWith("asfa::cg::conectado="))
		{
			ASFA.ASFAconectado = val.equals("1");
			synchronized(ASFA)
        	{
        		ASFA.notify();
        	}
		}
		else if(s.startsWith("asfa::cg::anulado="))
		{
			ASFA.ASFAanulado = val.equals("1");
			synchronized(ASFA)
        	{
        		ASFA.notify();
        	}
		}
		else if(s.startsWith("asfa::selector_tipo="))
		{
			int speed = Integer.parseInt(val);
			int selectorT=0;
			if (speed > 180) selectorT = 8;
			else if (speed > 160) selectorT = 7;
			else if (speed > 140) selectorT = 6;
			else if (speed > 120) selectorT = 5;
			else if (speed > 100) selectorT = 4;
			else if (speed > 90) selectorT = 3;
			else if (speed > 80) selectorT = 2;
			else if (speed > 10) selectorT = 1;
			else if (speed > 0) selectorT = speed;
			if (selectorT > 0) ASFA.selectorT = selectorT;
		}
		else if(s.startsWith("asfa::pantalla::conectada="))
		{
			ASFA.display.pantallaconectada = val.equals("1");
		}
		else if(s.startsWith("asfa::pantalla::activa="))
		{
			ASFA.display.pantallaactiva = val.equals("1");
		}
	}
	static boolean matches(String topic, String var)
	{
		if (topic.startsWith("register(")) topic = topic.substring(9, topic.length()-1);
		if (topic.startsWith("get(")) topic = topic.substring(4, topic.length()-1);
		String[] t1 = topic.split("::");
		String[] t2 = var.split("::");
		for (int i=0; i<t1.length && i<t2.length; i++)
		{
			if (t1[i].equals("*")) return true;
			if (!t1[i].equals("+") && !t1[i].equals(t2[i])) break;
			if (i+1 == t1.length && t1.length == t2.length) return true;
		}
		return false;
	}
	void subscribe(String topic)
	{
		sendData("register("+topic+")");
	}
	public void sendData(String s)
	{
		if(out==null) return;
		if (!ASFA.ASFA_Maestro && !s.startsWith("register(")) return;
		s = s+'\n';
		char[] c = s.toCharArray();
		try {
			for(int i=0; i<c.length; i++) {
				out.write(c[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	String readData()
	{
		try {
			return in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public boolean connected()
	{
		return out!=null;
	}
}
