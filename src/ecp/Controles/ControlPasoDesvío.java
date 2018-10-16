package ecp.Controles;

import ecp.ASFA;

public class ControlPasoDesvío extends Control {

    public ControlPasoDesvío(double time, boolean AnteriorAumVel, ASFA.Modo Modo) {
        super(time, 0, 20, 0);
        if (AnteriorAumVel) {
            if (Modo == ASFA.Modo.CONV || Modo == ASFA.Modo.BasicoCONV) {
                IF = new Curva(93, 93, 0, 0);
                VC = new Curva(90, 90, 0, 0);
            }
        } else {
            if (Modo == ASFA.Modo.CONV || Modo == ASFA.Modo.BasicoCONV) {
                IF = new Curva(63, 63, 0, 0);
                VC = new Curva(60, 60, 0, 0);
            }
        }
    }
}
