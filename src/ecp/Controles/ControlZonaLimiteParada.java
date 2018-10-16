package ecp.Controles;

import ecp.ASFA;

public class ControlZonaLimiteParada extends ControlFASF {

    public ControlZonaLimiteParada(ASFA.Modo Modo, double time) {
        super(time, 0, 0, 0);
        if (Modo == ASFA.Modo.CONV || Modo == ASFA.Modo.BasicoCONV || Modo == ASFA.Modo.AV || Modo == ASFA.Modo.BasicoAV || Modo == ASFA.Modo.RAM) {
            VC = new Curva(15, 15, 0, 0);
            IF = new Curva(18, 18, 0, 0);
        }
    }
}
