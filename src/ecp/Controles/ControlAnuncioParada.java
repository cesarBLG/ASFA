package ecp.Controles;

import ecp.ASFA;

public class ControlAnuncioParada extends ControlFASF {
	
    public ControlAnuncioParada(double time, double O, ASFA.Modo Modo) {
        super(time, 0, 0, 0);
        if (Modo == ASFA.Modo.CONV) {
            if (O <= 100) {
                VC = new Curva(O, 60, 0.26, 7.5);
                IF = new Curva(O + 3, 63, 0.26, 11);
            } else if (O == 120) {
                VC = new Curva(120, 80, 0.46, 7.5);
                IF = new Curva(123, 83, 0.36, 12);
            } else if (O == 140) {
                VC = new Curva(140, 80, 0.6, 7.5);
                IF = new Curva(143, 83, 0.5, 10);
            } else if (O >= 160) {
                VC = new Curva(160, 80, 0.6, 7.5);
                IF = new Curva(163, 83, 0.5, 9);
            }
        }
        if (Modo == ASFA.Modo.AV) {
            if (O <= 100) {
                VC = new Curva(O, O, 0, 0);
                IF = new Curva(O + 3, O + 3, 0, 0);
            } else if (O == 120) {
                VC = new Curva(120, 100, 0.46, 7.5);
                IF = new Curva(123, 103, 0.36, 12);
            } else if (O == 140) {
                VC = new Curva(140, 100, 0.6, 7.5);
                IF = new Curva(143, 103, 0.5, 10);
            } else if (O == 160) {
                VC = new Curva(160, 100, 0.6, 7.5);
                IF = new Curva(163, 103, 0.5, 9);
            } else if (O == 180) {
                VC = new Curva(180, 100, 0.55, 7.5);
                IF = new Curva(185, 103, 0.5, 9);
            } else if (O == 200) {
                VC = new Curva(200, 100, 0.55, 7.5);
                IF = new Curva(205, 103, 0.5, 9);
            }
        }
    }
}
