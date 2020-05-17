package dmi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import com.COM;

import dmi.Botones.Botón;
import dmi.Botones.Botón.TipoBotón;
import dmi.Pantalla.ÚltimaInfo.Info;
import ecp.Clock;
import ecp.FrecASFA;
import ecp.Main;
import ecp.Odometer;
import ecp.ASFA.Modo;

public class ECPinterface {
	Socket s;
	OutputStream out;
	BufferedReader in;
	DMI dmi;
	public ECPinterface(DMI dmi)
	{
		this.dmi = dmi;
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
			subscribe("asfa::pulsador::conex");
			subscribe("asfa::pulsador::basico");
			subscribe("asfa::pulsador::ilum::*");
			subscribe("asfa::leds::*");
			subscribe("asfa::fabricante");
			subscribe("asfa::pantalla::iniciar");
			subscribe("asfa::pantalla::apagar");
			while(true)
			{
				String s = readData();
				if(s==null) return;
				int index = s.indexOf('=');
				if (index < 0) continue;
				String[] topics = s.substring(0, index).split("::");
				String val = s.substring(index+1);
				if(s.startsWith("asfa::indicador::velocidad="))
				{
					int Val = Integer.parseInt(val);
					dmi.pantalla.vreal.setValue(Val);
				}
				else if(s.startsWith("asfa::pulsador::ilum::"))
				{
					String pul = topics[3];
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
					if (tb == null) continue;
					int Ilum = Integer.parseInt(val);
	                if (tb == Botón.TipoBotón.VLCond) Ilum += 2;
	                if (Botón.ListaBotones[tb.ordinal()] != null) Botón.ListaBotones[tb.ordinal()].iluminar(Ilum);
				}
				else if (s.startsWith("asfa::indicador::v_control="))
				{
	            	dmi.pantalla.vtarget.val = Integer.parseInt(val);
	            	dmi.pantalla.vtarget.update();
				}
				else if (s.startsWith("asfa::indicador::estado_vcontrol="))
				{
	            	dmi.pantalla.vtarget.figureVisible = Integer.parseInt(val);
	            	dmi.pantalla.vtarget.update();
				}
				else if (s.startsWith("asfa::indicador::frenado="))
				{
	            	dmi.pantalla.intervención.frenado = Integer.parseInt(val);
	            	dmi.pantalla.intervención.update();
				}
				else if (s.startsWith("asfa::indicador::urgencia="))
				{
	            	dmi.pantalla.intervención.urgencia = val.equals("1");
	            	dmi.pantalla.intervención.update();
				}
				else if (s.startsWith("asfa::indicador::eficacia="))
				{
	            	dmi.pantalla.eficacia.set(val.equals("1"));
				}
				else if (s.startsWith("asfa::indicador::control_desvio="))
				{
	            	dmi.pantalla.controles.Desv = Integer.parseInt(val);
	            	dmi.pantalla.controles.update();
				}
				else if (s.startsWith("asfa::indicador::secuencia_aa="))
				{
	            	dmi.pantalla.controles.SecAA = Integer.parseInt(val);
	            	dmi.pantalla.controles.update();
				}
				else if (s.startsWith("asfa::indicador::lvi="))
				{
	            	dmi.pantalla.controles.LVI = Integer.parseInt(val);
	            	dmi.pantalla.controles.update();
				}
				else if (s.startsWith("asfa::indicador::pndesp="))
				{
	            	dmi.pantalla.controles.PNdesp = Integer.parseInt(val);
	            	dmi.pantalla.controles.update();
				}
				else if (s.startsWith("asfa::indicador::pnprot="))
				{
	            	dmi.pantalla.controles.PNprot = Integer.parseInt(val);
	            	dmi.pantalla.controles.update();
				}
				else if (s.startsWith("asfa::indicador::urgencia="))
				{
	            	dmi.pantalla.intervención.urgencia = val.equals("1");
	            	dmi.pantalla.intervención.update();
				}
				else if (s.startsWith("asfa::indicador::modo="))
				{
	            	dmi.pantalla.ModoASFA.update(Modo.values()[Integer.parseInt(val)]);
				}
				else if (s.startsWith("asfa::indicador::tipo_tren="))
				{
					dmi.pantalla.tipoTren.set(Integer.parseInt(val));
				}
				else if (s.startsWith("asfa::leds::"))
				{
					dmi.repetidor.luces_basico.update(Integer.parseInt(topics[2]), Integer.parseInt(val));
				}
				else if (s.startsWith("asfa::indicador::velo"))
				{
					dmi.pantalla.velo.setActivo(val.equals("1"));
				}
				else if(s.startsWith("asfa::indicador::ultima_info="))
				{
					int Val = Integer.parseInt(val);
					dmi.pantalla.info.setInfo(Info.values()[Val>>1], (Val & 1) != 0);
				}
				else if(s.startsWith("asfa::pulsador::basico="))
				{
					boolean bas = val.equals("1");
					if (bas) 
		        		dmi.pantalla.poweroff();
					dmi.repetidor.basico.setSelected(bas);
				}
				else if(s.startsWith("asfa::pantalla::iniciar="))
				{
					dmi.pantalla.start();
				}
				else if(s.startsWith("asfa::pantalla::apagar="))
				{
	        		dmi.pantalla.poweroff();
				}
				else if(s.startsWith("asfa::pulsador::conex="))
				{
					if (!val.equals("1")) 
		        		dmi.pantalla.poweroff();
				}
				else if (s.startsWith("asfa::fabricante="))
				{
					dmi.fabricante = val;
				}
			}
		}).start();
	}
	public void subscribe(String topic)
	{
		sendData("register("+topic+")");
	}
	public void unsubscribe(String topic)
	{
		sendData("unregister("+topic+")");
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
	public void enviarPulsacion(TipoBotón botón, boolean state)
	{
        String name = botón.name().toLowerCase();
		if(name.equals("aumvel")) name = "aumento";
		else if(name.equals("ocultación")) name = "ocultacion";
		else if(name.equals("asfa_básico")) name = "basico";
        sendData("asfa::pulsador::" + name + "=" + (state ? "1" : "0"));
	}
}
