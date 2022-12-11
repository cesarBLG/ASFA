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

    public static void parse(int functn, int val) 
    {
    	if(Main.ASFA!=null)
    	{
            if (functn == 8)
            {
            	FrecASFA freq = FrecASFA.AL;
            	if (val == 0) freq = FrecASFA.FP;
            	else if (val < 10) freq = FrecASFA.values()[val-1];
            	else if (val == 10) freq = FrecASFA.L10;
            	else if (val == 11) freq = FrecASFA.L11;
                Main.ASFA.captador.nuevaFrecuencia(freq);
            }
            else if (functn == 9) Odometer.speed = (float) val / 3.6;
    	}
    }
}
