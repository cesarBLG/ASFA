package com;

import dmi.Sonidos;
import dmi.Botones.Botón;
import dmi.Botones.Botón.TipoBotón;
import dmi.Pantalla.Pantalla;
import dmi.Pantalla.ÚltimaInfo.Info;
import ecp.ASFA.Modo;
import ecp.FrecASFA;
import ecp.Main;
import ecp.Odometer;

public interface COM {

    void start();

    void write(byte[] b);

    String read(int count);

    int available();
    
    public static void parse(byte[] data) 
    {
        parse(data[0] & 0xFF, data[1] & 0xFF);
    }

    public static void parse(int functn, int val) 
    {
    	if(Main.dmi!=null)
    	{

        	Pantalla pantalla = Main.dmi.pantalla;
            if (functn == 0) {
                int BotNum = val >> 2;
                int Ilum = val & 1;
                if ((val & 2) != 0) return;
                if (BotNum == Botón.TipoBotón.VLCond.ordinal() && Ilum == 1) Ilum++;
                if (Botón.ListaBotones[BotNum] != null) Botón.ListaBotones[BotNum].iluminar(Ilum);
            }
            if (functn == 1) pantalla.info.setInfo(Info.values()[val>>1], (val & 1) != 0);
            if (functn == 2) pantalla.vreal.setValue(val);
            if (functn == 3) 
            {
                if ((val & 1) == 1)  pantalla.vtarget.figureVisible = val >> 1;
                else pantalla.vtarget.val = ((int) (val >> 1) & 0xFF) * 5;
                pantalla.vtarget.update();
            }
            if (functn == 4) 
            {
                if (val < 2) 
                {
                    pantalla.intervención.urgencia = val == 1;
                    pantalla.intervención.update();
                }
                if ((val & 2) != 0) pantalla.eficacia.set((val & 1) == 1);
                if ((val & 4) != 0)
                {
                    pantalla.intervención.frenado = val & 3;
                    pantalla.intervención.update();
                }
            }
            if (functn == 5)
            {
                int control = val >> 2;
                int activate = (val & 3);
                if (control == 0) pantalla.controles.Desv = activate;
                if (control == 1) pantalla.controles.SecAA = activate;
                if (control == 2) pantalla.controles.LVI = activate;
                if (control == 3) pantalla.controles.PNdesp = activate;
                if(control == 4) pantalla.controles.PNprot = activate;
                pantalla.controles.update();
            }
            if (functn == 6) pantalla.set();
            if (functn == 11)
            {
            	pantalla.ModoASFA.update(Modo.values()[val]);
            }
            if (functn == 12)
            {
            	pantalla.tipoTren.set(val);
            }
            if (functn == 14)
            {
            	if(val<3) pantalla.setup(val, null);
            	if(val == 3) pantalla.start();
            }
            if (functn == 15)
            {
            	boolean basic = (val & 1) != 0;
            	boolean trig = (val & 2) != 0;
            	int num = val >> 2;
            	String name = Sonidos.values()[num].toString().replace('_', '-');
            	if(trig) Main.dmi.sound.Trigger(name, basic);
            	else Main.dmi.sound.Stop(name);
            }
    	}
    	if(Main.ASFA!=null)
    	{
            if(functn == 7)
    		{
    			Main.ASFA.div.add((byte) val);
    		}
            if (functn == 8) Main.ASFA.captador.Recepción = FrecASFA.values()[val];
            if (functn == 9) Odometer.speed = (float) val / 3.6;
            if (functn == 10)
            {
                int BotNum = val >> 1;
                boolean pulsad = (val & 1) == 1;
                Main.ASFA.display.pulsar(TipoBotón.values()[BotNum], pulsad);
            }
            if (functn == 13)
            {
        		Main.ASFA.selectorT = (val & 7) + 1;
            }
    	}
    }
}
