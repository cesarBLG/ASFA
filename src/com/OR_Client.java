package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import dmi.Sonidos;
import dmi.Botones.Botón;
import dmi.Botones.Botón.TipoBotón;
import dmi.Pantalla.ÚltimaInfo.Info;
import ecp.Clock;
import ecp.FrecASFA;
import ecp.Main;
import ecp.Odometer;

public class OR_Client {
	Socket s;
	OutputStream out;
	BufferedReader in;
	public OR_Client()
	{
		new Thread(() -> {
			while(s==null)
			{
				try
				{
					s = new Socket("localhost", 5090);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			while(!s.isConnected()) 
			{
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				out = s.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			subscribe("asfa::frecuencia");
			subscribe("asfa::div");
			subscribe("asfa::selector_tipo");
			subscribe("asfa::akt");
			subscribe("asfa::con");
			subscribe("asfa::pulsador::*");
			subscribe("speed");
			subscribe("simulator_time");
			sendData("asfa::cg=1");
			while(true)
			{
				String s = readData();
				if(s==null) return;
				if (s.equals("register(asfa::cg)") || s.equals("register(asfa::*")) sendData("asfa::cg=1");
				int index = s.indexOf('=');
				if (index < 0) continue;
				String[] topics = s.substring(0, index).split("::");
				String val = s.substring(index+1);
				if(s.startsWith("asfa::frecuencia="))
				{
					FrecASFA f = FrecASFA.AL;
					try
					{
						 f = FrecASFA.valueOf(val);
					}
					catch(IllegalArgumentException e)
					{
					}
					COM.parse(8, f.ordinal());
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
					if (tb == null) continue;
		            Main.ASFA.display.pulsar(tb, val.equals("1"));
		            if(tb==TipoBotón.PrePar) Main.ASFA.display.pulsar(TipoBotón.VLCond, val.equals("1"));
				}
				else if(s.startsWith("simulator_time="))
				{
					Clock.set_external_time(Double.parseDouble(val.replace(',','.')));
				}
				else if(s.startsWith("asfa::div="))
				{
					for(int i=0; i<64; i++)
					{
						byte b = Integer.decode("0x"+val.substring(2*i, 2*i+2)).byteValue();
						Main.ASFA.div.add(b);
					}
				}
				else if(s.startsWith("asfa::akt="))
				{
					Main.ASFA.AKT = val.equals("1");
				}
				else if(s.startsWith("asfa::con="))
				{
					Main.ASFA.CON = !val.equals("0");
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
					else if (speed > 0) selectorT = 1;
					if (Main.ASFA.selectorT == 0 && selectorT != 0) Main.ASFA.selectorT = selectorT;
				}
			}
		}).start();
	}
	void subscribe(String topic)
	{
		sendData("register("+topic+")");
	}
	public void sendData(String s)
	{
		if(out==null) return;
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
}
