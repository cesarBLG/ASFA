package ecp.Controles;

import ecp.ASFA;

public class ControlAnuncioPrecaución extends ControlFASF implements ControlAumentable {

    public boolean AumentoVelocidad;
    
    public ControlAnuncioPrecaución(double time, TrainParameters param) {
        super(time, 0, 0, param.Modo == ASFA.Modo.RAM ? 200 : 0, param);
        Curvas();
    }

    Curva[] getCurvas(int O) {
    	Curva VC = null;
    	Curva IF = null;
        if (Modo == ASFA.Modo.CONV) {
            if (AumentoVelocidad) {
                if (O >= 160) {
                    IF = new Curva(163, 103, 0.5, 9);
                    VC = new Curva(160, 100, 0.6, 7.5);
                } else if (O == 140) {
                    IF = new Curva(143, 103, 0.5, 10);
                    VC = new Curva(140, 100, 0.6, 7.5);
                } else if (O == 120) {
                    IF = new Curva(123, 103, 0.36, 12);
                    VC = new Curva(120, 100, 0.46, 7.5);
                } else if (O <= 100) {
                    IF = new Curva(O + 3, O + 3, 0, 0);
                    VC = new Curva(O, O, 0, 0);
                }
            } else {
                if (O >= 160) {
                    IF = new Curva(163, 83, 0.5, 9);
                    VC = new Curva(160, 80, 0.6, 7.5);
                } else if (O == 140) {
                    IF = new Curva(143, 83, 0.5, 10);
                    VC = new Curva(140, 80, 0.6, 7.5);
                } else if (O == 120) {
                    IF = new Curva(123, 83, 0.36, 12);
                    VC = new Curva(120, 80, 0.46, 7.5);
                } else if (O <= 100) {
                    IF = new Curva(O + 3, 63, 0.26, 11);
                    VC = new Curva(O, 60, 0.36, 7.5);
                }
            }
        }
        if(Modo == ASFA.Modo.RAM)
        {
        	if (O == 120) {
                IF = new Curva(123, 33, 0.36, 12);
                VC = new Curva(120, 30, 0.46, 7.5);
            } else if (O <= 100) {
                IF = new Curva(O + 3, 33, 0.26, 11);
                VC = new Curva(O, 30, 0.36, 7.5);
            }
        }
        return new Curva[] {VC, IF};
    }

    public final void AumentarVelocidad(boolean value) {
        AumentoVelocidad = value;
        Curvas();
    }

    public final boolean Aumentado() {
        return AumentoVelocidad || Modo == ASFA.Modo.RAM;
    }
}
