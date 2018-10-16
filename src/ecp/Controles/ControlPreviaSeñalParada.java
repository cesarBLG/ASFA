package ecp.Controles;

import ecp.ASFA;

public class ControlPreviaSeñalParada extends ControlFASF {

    public ControlPreviaSeñalParada(double time, double O, ASFA.Modo Modo) {
        super(time, 0, 0, 0);
        if (Modo == ASFA.Modo.AV || Modo == ASFA.Modo.BasicoAV || Modo == ASFA.Modo.CONV || Modo == ASFA.Modo.BasicoCONV) {
            if (O > 100) {
                VC = new Curva(50, 15, 0.6, 1.5);
                IF = new Curva(53, 18, 0.55, 3.5);
            } else if (O <= 100) {
                VC = new Curva(40, 15, 0.36, 2.5);
                IF = new Curva(43, 18, 0.36, 5.5);
            }
        } else if (Modo == ASFA.Modo.RAM) {

        }
    }
}
