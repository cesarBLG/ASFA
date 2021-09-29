package ecp;

import ecp.Controles.Control;
import ecp.Controles.ControlAnuncioParada;
import ecp.Controles.ControlAnuncioPrecaución;
import ecp.Controles.ControlArranque;
import ecp.Controles.ControlAumentable;
import ecp.Controles.ControlBTS;
import ecp.Controles.ControlLVI;
import ecp.Controles.ControlManiobras;
import ecp.Controles.ControlPNDesprotegido;
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
import ecp.Redundancia.DatosRedundancia;

public class Redundancia {
	public class DatosRedundancia
	{
		int Velocidad;
		int Distancia;
		int ControlActivo;
		int VelocidadControl;
		int VelocidadIntervencion;
		long DistanciaUltimaRecepcion;
		long TiempoUltimaRecepcion;
		FrecASFA FrecuenciaCaptador;
		FrecASFA UltimaFrecuencia;
	}
	ASFA ASFA;
	int numeroFallos;
	DatosRedundancia datos;
	public Redundancia(ASFA asfa)
	{
		ASFA = asfa;
	}
	boolean coinciden(int a, int b, int margen)
	{
		return Math.abs(a-b)<=margen;
	}
	boolean coinciden(long a, long b, int margen)
	{
		return Math.abs(a-b)<=margen;
	}
	public void actualizar(double vreal, double control, double max, double dist, Control ControlActivo, 
			FrecASFA frecRecibida, FrecASFA UltimaFrecValida, double TiempoUltimaRecepcion, double DistanciaUltimaRecepcion)
	{
    	DatosRedundancia d = new Redundancia.DatosRedundancia();
    	d.Velocidad = (int)Math.round(vreal);
    	d.VelocidadControl = (int)Math.round(control);
    	d.VelocidadIntervencion = (int)Math.round(max);
    	d.FrecuenciaCaptador = frecRecibida;
    	d.UltimaFrecuencia = UltimaFrecValida;
    	d.TiempoUltimaRecepcion = (long) TiempoUltimaRecepcion;
    	d.DistanciaUltimaRecepcion = (long) DistanciaUltimaRecepcion;
    	int num=18;
		Control c = ControlActivo;
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
		else if (c instanceof ControlZonaLimiteParada) num = 14;
		else if (c instanceof ControlPasoDesvío) num = 15;
		else if (c instanceof ControlLVI) num = 16;
		else if (c instanceof ControlBTS) num = 30;
		else if (c instanceof ControlManiobras) num = 31;
		//else if (c instanceof ControlCambioSeñalizacion) num = 17; TODO
		if (c instanceof ControlAumentable && ((ControlAumentable) c).Aumentado()) num += 32;
		else if (c instanceof ControlLVI && ((ControlLVI)c).AumentoVelocidad) num += 32;
		d.ControlActivo = num;
		datos = d;
		/*if (ASFA == Main.ASFA) Main.ASFA2.redundancia.received(datos);
		else Main.ASFA.redundancia.received(datos);*/
	}
	void received(DatosRedundancia d)
	{
		if (!coinciden(d,datos)) numeroFallos++;
		else if (numeroFallos > 0) numeroFallos--;
	}
	boolean coinciden(DatosRedundancia d1, DatosRedundancia d2)
	{
		return d1!=null && d2 != null && coinciden(d1.Velocidad, d2.Velocidad, 2) && 
				coinciden(d1.Distancia, d2.Distancia, 30) && d1.ControlActivo == d2.ControlActivo &&
				coinciden(d1.VelocidadControl, d2.VelocidadControl, 3) && 
				coinciden(d1.VelocidadIntervencion, d2.VelocidadIntervencion, 3) && 
				d1.FrecuenciaCaptador == d2.FrecuenciaCaptador && d1.UltimaFrecuencia == d2.UltimaFrecuencia &&
				coinciden(d1.DistanciaUltimaRecepcion, d2.DistanciaUltimaRecepcion, 5) &&
				coinciden(d1.TiempoUltimaRecepcion, d2.TiempoUltimaRecepcion, 1);
	}
}
