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
                Main.ASFA.captador.nuevaFrecuencia(FrecASFA.values()[val]);
            }
            else if (functn == 9) Odometer.speed = (float) val / 3.6;
    	}
    }
}
