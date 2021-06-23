package ecp;

import java.util.Calendar;
import java.util.Date;

import ecp.Controles.Control;
import ecp.Controles.ControlAnuncioParada;
import ecp.Controles.ControlAnuncioPrecaución;
import ecp.Controles.ControlArranque;
import ecp.Controles.ControlAumentable;
import ecp.Controles.ControlBTS;
import ecp.Controles.ControlLVI;
import ecp.Controles.ControlLVIL1F1;
import ecp.Controles.ControlManiobras;
import ecp.Controles.ControlPNDesprotegido;
import ecp.Controles.ControlPNProtegido;
import ecp.Controles.ControlPasoDesvío;
import ecp.Controles.ControlPreanuncioParada;
import ecp.Controles.ControlPreviaSeñalParada;
import ecp.Controles.ControlSecuenciaAA;
import ecp.Controles.ControlSecuenciaAN_A;
import ecp.Controles.ControlSeñalParada;
import ecp.Controles.ControlTransicion;
import ecp.Controles.ControlViaLibre;
import ecp.Controles.ControlViaLibreCondicional;
import ecp.Controles.ControlZonaLimiteParada;

public class JRU {
	ASFA ASFA;
	boolean FullDuplex;
	void put(int data, int size)
	{
		
	}
	int getNumero(Control c)
	{
		int num=31;
		if (c instanceof ControlArranque) num = 0;
		else if (c instanceof ControlTransicion) num = 1;
		else if (c instanceof ControlViaLibre) num = 2;
		else if (c instanceof ControlViaLibreCondicional) num = 3;
		else if (c instanceof ControlAnuncioParada) num = 4;
		else if (c instanceof ControlPreanuncioParada) num = 5;
		else if (c instanceof ControlSecuenciaAN_A) num = 6;
		else if (c instanceof ControlPreviaSeñalParada) num = 7;
		else if (c instanceof ControlSeñalParada)
		{
			if (((ControlSeñalParada)c).conRebase) num = 9;
			else num = 8;
		}
		else if (c instanceof ControlSecuenciaAA) num = 10;
		else if (c instanceof ControlAnuncioPrecaución) num = 11;
		else if (c instanceof ControlPNDesprotegido) num = 12;
		else if (c instanceof ControlLVIL1F1) num = 14;
		else if (c instanceof ControlLVI) num = 13;
		else if (c instanceof ControlPasoDesvío) num = 15;
		else if (c instanceof ControlZonaLimiteParada) num = 16;
		else if (c instanceof ControlPNProtegido) num = 17;
		if (c instanceof ControlAumentable && ((ControlAumentable) c).Aumentado()) num += 32;
		else if (c instanceof ControlLVI && ((ControlLVI)c).AumentoVelocidad) num += 32;
		return num;
	}
	void sendMessage()
	{
		put(FullDuplex ? 1 : 0, 1);
		put(2, 4);
		put(1, 4);
		put(0, 8);
		put(0, 5);
		put(0, 3);
		{
			Calendar c = Calendar.getInstance();
			c.setTime(new Date((long)(Clock.getSeconds()*1000)));
			put(c.get(Calendar.DAY_OF_MONTH), 5);
			put(c.get(Calendar.MONTH), 4);
			put(c.get(Calendar.YEAR)%100, 7);
			put(c.get(Calendar.HOUR_OF_DAY), 5);
			put(c.get(Calendar.MINUTE), 6);
			put(c.get(Calendar.SECOND), 6);
		}
		put((int)(Odometer.getSpeed()*3.6f),9);
		int VC = 400;
		int IF = 400;
		int VCF = 400;
		Control activo = null;
		for (Control c : ASFA.Controles)
		{
			if (c.getVC(Clock.getSeconds())<VC)
			{
				VC = (int)c.getVC(Clock.getSeconds());
				IF = (int)c.getIF(Clock.getSeconds());
				VCF = (int)c.VC.OrdenadaFinal;
			}
			activo = ASFA.controlPrioritario(activo, c);
		}
		Control conc1 = null;
		for (Control c : ASFA.Controles)
		{
			if (c == activo) continue;
			conc1 = ASFA.controlPrioritario(conc1, c);
		}
		Control conc2 = null;
		for (Control c : ASFA.Controles)
		{
			if (c == activo || c == conc1) continue;
			conc2 = ASFA.controlPrioritario(conc2, c);
		}
		put(VC,9);
		put(IF,9);
		put(VCF,9);
		put(ASFA.Eficacia ? 1 : 0, 1);
		put(ASFA.FE ? 1 : 0, 1);
		put(ASFA.RebaseAuto ? 1 : 0, 1);
		int modo = 0;
		switch(ASFA.modo)
		{
			case AV:
				modo = ASFA.basico ? 7 : 2;
				break;
			case BTS:
				modo = 3;
				break;
			case CONV:
				modo = ASFA.basico ? 6 : 1;
				break;
			case EXT:
				modo = 14;
				break;
			case MBRA:
				modo = 5;
				break;
			case MTO:
				modo = 15;
				break;
			case RAM:
				modo = ASFA.basico ? 9 : 8;
				break;
		}
		put(modo, 4);
		put(1, 1); //cab 1 activa
		put(0, 1); //cab 2
		put(getNumero(activo),6);
		put(getNumero(conc1),6);
		put(getNumero(conc2),6);
		put(0,15); //diam rueda
	}
}
