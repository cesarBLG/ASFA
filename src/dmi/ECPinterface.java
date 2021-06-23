package dmi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.Timer;

import com.COM;
import com.OR_Client;

import dmi.Botones.Botón;
import dmi.Botones.Botón.TipoBotón;
import dmi.Pantalla.Pantalla;
import dmi.Pantalla.Pantalla.ModoDisplay;
import dmi.Pantalla.ÚltimaInfo.Info;
import ecp.Clock;
import ecp.Config;
import ecp.FrecASFA;
import ecp.Main;
import ecp.Odometer;
import ecp.ASFA.Modo;

public class ECPinterface {
	Socket s;
	OutputStream out;
	BufferedReader in;
	DMI dmi;
	boolean connected;
	void setup()
	{
		s = OR_Client.getSocket();
		try {
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			out = s.getOutputStream();
			connected = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		subscribe("asfa::pulsador::conex");
		subscribe("asfa::pulsador::basico");
		subscribe("asfa::pulsador::ilum::*");
		subscribe("asfa::dmi::activo");
		subscribe("asfa::leds::*");
		subscribe("asfa::fabricante");
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
            	dmi.pantalla.vtarget.veloc.value = Integer.parseInt(val);
            	dmi.pantalla.vtarget.veloc_lvi.value = Integer.parseInt(val);
            	dmi.pantalla.vtarget.update();
			}
			else if (s.startsWith("asfa::indicador::estado_vcontrol="))
			{
            	dmi.pantalla.vtarget.figureVisible = Integer.parseInt(val);
            	dmi.pantalla.vtarget.update();
			}
			else if (s.startsWith("asfa::indicador::vcontrol_degradada="))
			{
            	dmi.pantalla.vtarget.lvi = val.equals("1") || val.equals("true");
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
            	dmi.pantalla.eficacia.set(val.equals("1"), !val.equals("0"));
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
				int num = Integer.parseInt(val);
				if (num < 0)
				{
					dmi.pantalla.ModoASFA.setValue("");
				}
				else
				{
					Modo m = Modo.values()[num];
					if (m == Modo.EXT)
					{
						dmi.pantalla.linea.setVisible(false);
						dmi.pantalla.vreal.setVisible(false);
						dmi.pantalla.vtarget.setVisible(false);
						dmi.pantalla.controles.setVisible(false);
						dmi.pantalla.info.setVisible(false);
						dmi.pantalla.velo.setVisible(false);
						dmi.pantalla.intervención.setVisible(false);
						dmi.pantalla.set(ModoDisplay.Noche);
					}
					else if (dmi.modo == Modo.EXT)
					{
						dmi.pantalla.linea.setVisible(true);
						dmi.pantalla.vreal.setVisible(true);
						dmi.pantalla.vtarget.setVisible(true);
						dmi.pantalla.controles.setVisible(true);
						dmi.pantalla.info.setVisible(true);
						dmi.pantalla.velo.setVisible(true);
						dmi.pantalla.intervención.setVisible(true);
						dmi.pantalla.set(ModoDisplay.Día);
					}
					dmi.modo = m;
					dmi.pantalla.ModoASFA.update(m);
				}
			}
			else if (s.startsWith("asfa::indicador::tipo_tren="))
			{
				try {
					dmi.pantalla.tipoTren.set(Integer.parseInt(val));
				} catch (Exception e) {
					dmi.pantalla.tipoTren.set(val);
				}
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
				dmi.repetidor.basico.setSelected(val.equals("1"));
			}
			else if(s.startsWith("asfa::ecp::estado="))
			{
				String[] spl = val.split(",");
				dmi.pantalla.setup(Integer.parseInt(spl[0]), spl.length>1 ? spl[1] : "");
			}
			else if(s.startsWith("asfa::pantalla::activa="))
			{
				boolean act = val.equals("1");
				if (act != dmi.pantalla.activa && dmi.pantalla.conectada)
				{
					dmi.pantalla.activa = act;
					if (act) dmi.pantalla.start();
					else dmi.pantalla.stop();
				}
			}
			else if(s.startsWith("asfa::conectado="))
			{
				if (!val.equals("1"))
				{
					dmi.pantalla.apagar();
	        		if (Config.ApagarOrdenador)
	        		{
	        			try {
							Runtime.getRuntime().exec("shutdown -h now");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	        		}
				}
				else if (dmi.activo)
				{
					dmi.pantalla.encender();
				}
			}
			else if(s.startsWith("asfa::dmi::activo="))
			{
				if (!val.equals("1"))
				{
					dmi.activo = false;
					dmi.pantalla.apagar();
	        		if (Config.ApagarOrdenador)
	        		{
	        			try {
							Runtime.getRuntime().exec("shutdown -h now");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	        		}
				}
				else
				{
					dmi.activo = true;
					dmi.pantalla.encender();
				}
			}
			else if(s.startsWith("asfa::fase="))
			{
				boolean Fase2 = val.equals("2");
				dmi.pantalla.eficacia.fase(Fase2);
			}
		}
	}
	public ECPinterface(DMI dmi)
	{
		this.dmi = dmi;
		new Thread(() -> {
			while(true) {
				setup();
				in = null;
				out = null;
				s = null;
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
		if (botón == TipoBotón.Conex && !connected)
		{
			if (!state || dmi.pantalla.conectada) dmi.pantalla.apagar();
			else dmi.pantalla.setup(3, "Fallo de comunicaciones con ECP");
			return;
		}
        String name = botón.name().toLowerCase();
		if(name.equals("aumvel")) name = "aumento";
		else if(name.equals("ocultación")) name = "ocultacion";
		else if(name.equals("asfa_básico")) name = "basico";
        sendData("asfa::pulsador::" + name + "=" + (state ? "1" : "0"));
	}
}
