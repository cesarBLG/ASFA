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
			sendData("register(asfa_baliza)");
			sendData("register(speed)");
			sendData("register(asfa_pulsador_anpar)");
			sendData("register(asfa_pulsador_anpre)");
			sendData("register(asfa_pulsador_prepar)");
			sendData("register(asfa_pulsador_modo)");
			sendData("register(asfa_pulsador_rearme)");
			sendData("register(asfa_pulsador_rebase)");
			sendData("register(asfa_pulsador_aumento)");
			sendData("register(asfa_pulsador_alarma)");
			sendData("register(asfa_pulsador_ocultacion)");
			sendData("register(asfa_pulsador_lvi)");
			sendData("register(asfa_pulsador_pn)");
			sendData("register(simulator_time)");
			while(true)
			{
				String s = readData();
				if(s==null) return;
				String val = s.substring(s.indexOf('=')+1);
				if(s.startsWith("asfa_baliza="))
				{
					FrecASFA f = FrecASFA.valueOf(val);
					COM.parse(8, f.ordinal());
				}
				else if(s.startsWith("speed="))
				{
					Odometer.speed = (float) Float.parseFloat(val.replace(',', '.')) / 3.6;
				}
				else if(s.startsWith("asfa_pulsador_"))
				{
					String pul = s.substring(14, s.indexOf('='));
					TipoBotón tb = null;
					if(pul.equals("aumento")) tb = TipoBotón.AumVel;
					else if(pul.equals("ocultacion")) tb = TipoBotón.Ocultación;
					else for(TipoBotón t : TipoBotón.values())
					{
						if(t.name().toLowerCase().equals(pul))
						{
							tb = t;
							break;
						}
					}
					if (tb == null) break;
	                Main.ASFA.display.pulsar(tb, Integer.parseInt(val)==1);
	                if(tb==TipoBotón.PrePar) Main.ASFA.display.pulsar(TipoBotón.VLCond, Integer.parseInt(val)==1);
				}
				else if(s.startsWith("simulator_time="))
				{
					Clock.set_external_time(Double.parseDouble(val.replace(',','.')));
				}
			}
		}).start();
	}
	void sendData(String s)
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
	public void send(int funct, int val)
	{
		if (funct == 0) {
	        int BotNum = val >> 2;
	        int Ilum = val & 1;
	        if ((val & 2) != 0) return;
	        String name = Botón.TipoBotón.values()[BotNum].name().toLowerCase();
			if(name.equals("aumvel")) name = "aumento";
			else if(name.equals("ocultación")) name = "ocultacion";
	        sendData("asfa_ilumpuls_"+name+"="+(Ilum==1 ? "1" : "0"));
	    }
		if (funct == 1) sendData("asfa_last_info="+Info.values()[val>>1].ordinal());
		if (funct == 4) 
        {
            if (val < 2) sendData("asfa_emergency=" + (val==1 ? '1' : '0'));
            if ((val & 4) != 0) sendData("asfa_indicador_frenado="+(int)(val & 3));
        }
        if (funct == 3)
        {
        	if((val & 1) != 1) sendData("asfa_target_speed="+(((int) (val >> 1) & 0xFF) * 5));
        	else sendData("asfa_target_state="+(int)((val >> 1) & 0xFF));
        }
        if (funct == 5) {
            int control = val >> 2;
            
            if (control == 0) sendData("asfa_control_desvio="+(int)(val&3));
            if (control == 1) sendData("asfa_secuencia_aa="+(int)(val&3));
            if (control == 2) sendData("asfa_indicador_lvi="+(int)(val&3));
            if (control == 3) sendData("asfa_indicador_pndesp="+(int)(val&3));
            if(control == 4) sendData("asfa_indicador_pnprot="+(int)(val&3));
        }
        /*if (funct == 15)
        {
        	boolean basic = (val & 1) != 0;
        	boolean trig = (val & 2) != 0;
        	int num = val >> 2;
        	String name = Sonidos.values()[num].toString().replace('_', '-');
        	if(trig) sendData("asfa_sound_trigger="+name+","+(basic ? "1" : "0"));
        	else sendData("asfa_sound_stop="+name+","+(basic ? "1" : "0"));
        }*/
	}
}
