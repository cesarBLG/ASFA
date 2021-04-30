package ecp.Controles;

import ecp.ASFA.Modo;

public class Curva {

    public double OrdenadaOrigen;
    public double OrdenadaFinal;
    private double Deceleracion;
    private double TiempoReaccion;

    public Curva(double O0, double Of, double Dec, double TReac) {
        OrdenadaOrigen = O0;
        OrdenadaFinal = Of;
        Deceleracion = Dec;
        TiempoReaccion = TReac;
    }

    public Curva(double vconst) {
        OrdenadaOrigen = vconst;
        OrdenadaFinal = vconst;
        Deceleracion = 0;
        TiempoReaccion = 0;
    }

    public final double valor(double d) {
        return Math.max(OrdenadaFinal, Math.min(OrdenadaOrigen, 3.6 * ((OrdenadaOrigen / 3.6) - Deceleracion * (d - TiempoReaccion))));
    }
    
    public static Curva[] generarCurvas(Control c, double v0c, double vfc) {
    	double v0i;
    	double trc;
    	double tri;
    	double ac;
    	double ai;
    	double vfi;
    	if (c instanceof ControlPreviaSeñalParada) {
        	v0i = v0c + (v0c<=160 ? 3 : 5);
        	vfi = vfc + (v0c<=160 ? 3 : 5);
        	trc = v0c<=100 ? 2.5 : 1.5;
        	tri = v0c<=100 ? 5.5 : 3.5;
        	ac = v0c<=100 ? 0.36 : 0.6;
        	ai = v0c<=100 ? 0.36 : 0.55;
    	} else {
        	if (v0c >= 180 || (c instanceof ControlArranque || c instanceof ControlTransicion || c instanceof ControlViaLibre)) {
        		v0i = v0c + 5;
        		vfi = vfc + 5;
        	} else {
        		v0i = v0c + 3;
        		vfi = vfc + 3;
        	}
        	trc = 7.5;
        	if (v0c <= 110) {
        		tri = 11;
        		ac = 0.36;
        		ai = 0.26;
        	} else if (v0c <= 120) {
        		tri = 12;
        		ac = 0.46;
        		ai = 0.36;
    		} else {
    			tri = v0c <= 140 ? 10 : 9;
    			ac = 0.6;
    			ai = 0.5;
    		}
        	if (c instanceof ControlSecuenciaAN_A) {
        		ControlSecuenciaAN_A sec = (ControlSecuenciaAN_A)c;
        		if (sec.FirstBalise || sec.Modo != Modo.CONV || !sec.AnteriorAumVel) {
        			trc = (v0c <= 110 && sec.Modo == Modo.CONV) ? 2.5 : 3.5;
        			tri = 5;
        		}
        		if (c.Modo == Modo.AV) {
        			if (v0c <= 110) {
        				ac = 0.46;
        				ai = 0.36;
        			} else {
        				ac = 0.6;
        				ai = 0.5;
        			}
        		}
        	}
    	}
    	return new Curva[]{new Curva(v0c, vfc, ac, trc), new Curva(v0i, vfi, ai, tri)};
    }
}
