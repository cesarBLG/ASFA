package ecp.Controles;

import ecp.ASFA;

public class ControlSecuenciaAN_A extends ControlFASF {

    public ControlSecuenciaAN_A(double time, double O, double T, boolean AnteriorAumVel, boolean FirstBalise, ASFA.Modo Modo) {
        super(time, 0, 0, 0);
        if (Modo == ASFA.Modo.CONV) {
            if (!AnteriorAumVel) {
                double Vf = 60;
                if (FirstBalise) {
                    if (O >= 140) {
                        IF = new Curva(83, 63, 0.5, 5);
                        VC = new Curva(80, 60, 0.6, 3.5);
                    } else if (O == 120) {
                        IF = new Curva(83, 63, 0.36, 5);
                        VC = new Curva(80, 60, 0.46, 3.5);
                    } else if (O <= 100) {
                        IF = new Curva(63);
                        VC = new Curva(60);
                    }
                } else {
                    IF = new Curva(Vf + 3);
                    VC = new Curva(Vf);
                }
            } else {
                if (FirstBalise) {
                    if (O >= 140) {
                        IF = new Curva(103, 93, 0.5, 5);
                        VC = new Curva(100, 90, 0.6, 3.5);
                    } else if (O == 120) {
                        IF = new Curva(103, 93, 0.36, 5);
                        VC = new Curva(100, 90, 0.46, 3.5);
                    } else if (O <= 100) {
                        IF = new Curva(83, 63, 0.26, 5);
                        VC = new Curva(80, 60, 0.36, 2.5);
                    }
                    if(T>100) {
                    	VC.OrdenadaFinal = 90;
                    	IF.OrdenadaFinal = 93;
                    }
                } else {
                    if (O >= 160) {
                        IF = new Curva(93, 83, 0.5, 9);
                        VC = new Curva(90, 80, 0.6, 7.5);
                    } else if (O == 140) {
                        IF = new Curva(93, 83, 0.5, 10);
                        VC = new Curva(90, 80, 0.6, 7.5);
                    } else if (O == 120) {
                        IF = new Curva(93, 83, 0.36, 12);
                        VC = new Curva(90, 80, 0.46, 7.5);
                    } else if (O <= 100) {
                        IF = new Curva(63);
                        VC = new Curva(60);
                    }
                    if(T>100) {
                    	VC.OrdenadaFinal = 80;
                    	IF.OrdenadaFinal = 83;
                    }
                }
            }
        }
    }
}
