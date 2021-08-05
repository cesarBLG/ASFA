package com;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import dmi.Botones.Botón.TipoBotón;
import ecp.Config;
import ecp.DisplayInterface;
import ecp.Main;
import ecp.ASFA.Modo;

public class ECPStateSerializer extends StateSerializer {
	DisplayInterface display;
	public ECPStateSerializer(DisplayInterface display) 
	{
		super(Config.SerialECP);
		this.display = display;
		if (Config.SerialECP == null || Config.SerialECP == "") return;
		new Thread(() -> {
			while(true) update();
			}).start();
	}
	public ECPStateSerializer(DisplayInterface display, String port)
	{
		super(port);
		this.display = display;
		new Thread(() -> {
			while(true) update();
			}).start();
	}
	long lastPR;
	long lastDisplay;

    
    public void cambioRepetidor()
    {
    	lastPR += - 510 + Math.max(0, lastPR + 50 - System.currentTimeMillis());
    	synchronized(this) { notify(); }
    }
    public void cambioDisplay()
    {
    	lastDisplay += - 510 + Math.max(0, lastDisplay + 50 - System.currentTimeMillis());
    	synchronized(this) { notify(); }
    }
    public void cambio()
    {
    	lastPR += - 510 + Math.max(0, lastPR + 50 - System.currentTimeMillis());
    	lastDisplay += - 510 + Math.max(0, lastDisplay + 50 - System.currentTimeMillis());
    	synchronized(this) { notify(); }
    }
    ConcurrentLinkedQueue<Paquete> pendientes = new ConcurrentLinkedQueue<>();
	void update()
	{
		long time = System.currentTimeMillis();
		if (lastPR + 500 <= time)
		{
			sendEstadoPR();
			lastPR = time;
		}
		if (lastDisplay + 500 <= time)
		{
			write(new Paquete(TipoPaquete.ConexionDisplay, new byte[] {(byte)((display.pantallaactiva?2:0)+(display.pressed(TipoBotón.Conex)?1:0))}));
			if (display.pantallaactiva) sendEstadoDisplay();
			lastDisplay = time;
		}
		while(!pendientes.isEmpty())
		{
			write(pendientes.poll());
		}
		try {
			synchronized(this)
			{
				long sleep = Math.min(lastDisplay + 500, lastPR + 500) - time;
				if (sleep > 0) wait(sleep);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void sendEstadoPR()
	{
		byte[] data = new byte[3];
		TipoBotón[] botones = {
				TipoBotón.Modo, TipoBotón.Rearme, TipoBotón.Rebase, 
				TipoBotón.AumVel, TipoBotón.Alarma, TipoBotón.Ocultación,
				TipoBotón.LVI, TipoBotón.PN,
				TipoBotón.AnPar, TipoBotón.AnPre,TipoBotón.PrePar, TipoBotón.VLCond,
				TipoBotón.Conex
		};
		for (int i=0; i<botones.length; i++)
		{
			data[i/8] |= (display.iluminado(botones[i]) ? 1 : 0)<<(i%8);
		}
		data[2] |= (display.leds[0]) | (display.leds[1] << 2) | 
				((display.leds[2] == 4 ? 1 : (display.leds[2] == 3 ? 2 : 0)) << 4) | ((display.leds[2] < 3 ? display.leds[2] : 0) << 6); 
		write(new Paquete(TipoPaquete.LucesPR, data));
	}
	byte[] prevEstadoPR;
	public void setEstadoPR(byte[] data)
	{
		if (Arrays.equals(prevEstadoPR, data)) return;
		prevEstadoPR = data.clone();
		TipoBotón[] botones = {
				TipoBotón.Modo, TipoBotón.Rearme, TipoBotón.Rebase, 
				TipoBotón.AumVel, TipoBotón.Alarma, TipoBotón.Ocultación,
				TipoBotón.LVI, TipoBotón.PN,
				TipoBotón.AnPar, TipoBotón.AnPre,TipoBotón.PrePar,
				TipoBotón.Conex, TipoBotón.ASFA_básico
		};
		boolean different = false;
		for (int i=0; i<botones.length; i++)
		{
			boolean puls = (data[i/8]&1)==1;
			boolean prev = display.pressed(botones[i]);
			if (puls != prev && botones[i] == TipoBotón.Conex) different = true;
			display.pulsar(botones[i], puls);
            if(botones[i]==TipoBotón.PrePar) display.pulsar(TipoBotón.VLCond, puls);
			data[i/8] >>= 1;
		}
		if(different)
        {
        	synchronized(display.ASFA)
        	{
        		display.ASFA.notify();
        	}
        }
	}
	public void sendEstadoDisplay()
	{
		byte[] data = new byte[10];
		int vel = display.getDisplayValue("Velocidad");
		data[0] = (byte) (vel & 255);
		data[1] = (byte) (vel>>8);
		data[2] = (byte) (display.getDisplayValue("Velocidad Objetivo"));
		data[3] = (byte) (display.getDisplayValue("EstadoVobj") + 2*display.getDisplayValue("Velocidad Objetivo Degradada"));
		data[4] = (byte) display.getDisplayValue("Info");
		int pn = 0;
		int pnp = display.getDisplayValue("PN protegido");
		int pnd = display.getDisplayValue("PN sin protección");
		if (pnd > 0) pn = pnd == 1 ? 1 : 5;
		else if (pnp > 0) pn = pnp == 1 ? 2 : 6;
		data[5] = (byte) (display.getDisplayValue("LVI") + (pn<<4));
		data[6] = (byte) (display.getDisplayValue("Paso Desvío") != 0 ? 1 : 2*display.getDisplayValue("Secuencia AA"));
		data[7] = (byte) (display.getDisplayValue("Modo")+1);
		data[8] = (byte) (display.getDisplayValue("Tipo")/10);
		if (display.ASFA.modo == Modo.EXT) data[8] = (byte) (display.getDisplayValue("ModoEXT")+26);
		int ef = display.getDisplayValue("Eficacia");
		data[9] = (byte) (ef == 1 ? 2 : (ef == -1 ? 1 : 0));
		data[9] |= (byte) (display.ASFA.Fase2 ? 4 : 0);
		data[9] |= (byte) (display.getDisplayValue("Urgencia") == 1 ? 3 : display.getDisplayValue("Sobrevelocidad"))<<3;
		data[9] |= (byte) (display.getDisplayValue("Velo") == 1 ? 32 : 0);
		write(new Paquete(TipoPaquete.IconosDisplay, data));
	}
	public void queueSonido(String snd, boolean basico, boolean iniciar)
	{
		int num=0;
		if (snd.equals("S1-1")) num = 1;
		else if (snd.equals("S2-1")) num = 2;
		else if (snd.equals("S2-2")) num = 3;
		else if (snd.equals("S2-3")) num = 4;
		else if (snd.equals("S2-4")) num = 5;
		else if (snd.equals("S2-5")) num = 6;
		else if (snd.equals("S2-6")) num = 7;
		else if (snd.equals("S3-1")) num = 8;
		else if (snd.equals("S3-2")) num = 9;
		else if (snd.equals("S3-3")) num = 10;
		else if (snd.equals("S3-4")) num = 11;
		else if (snd.equals("S3-5")) num = 12;
		else if (snd.equals("S4")) num = 13;
		else if (snd.equals("S5")) num = 14;
		else if (snd.equals("S6")) num = 15;
		if (basico) num += 32;
		if (iniciar) num += 64;
		pendientes.add(new Paquete(TipoPaquete.Sonido, new byte[] {(byte)num}));
		pendientes.add(new Paquete(TipoPaquete.Sonido, new byte[] {(byte)num}));
		pendientes.add(new Paquete(TipoPaquete.Sonido, new byte[] {(byte)num}));
		synchronized(this)
		{
			notify();
		}
	}
	@Override
	protected void parse(Paquete paquete)
	{
		if (paquete.tipo == TipoPaquete.PulsadoresPR) setEstadoPR(paquete.data);
	}
}
