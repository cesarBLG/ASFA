package ecp.Controles;

import ecp.ASFA;

public class ControlPreanuncioParada extends ControlFASF implements ControlAumentable {

    public boolean AumentoVelocidad;

    public ControlPreanuncioParada(double time, TrainParameters param) {
        super(time, 0, 0, 0, param);
        Curvas();
    }

    Curva[] getCurvas(int O) {
    	Curva VC = null;
    	Curva IF = null;
        if (Modo == ASFA.Modo.CONV) {
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
            if (AumentoVelocidad) {
                IF.OrdenadaFinal += 20;
                VC.OrdenadaFinal += 20;
            }
        }
        return new Curva[] {VC, IF};
    }

    @Override
    public void AumentarVelocidad(boolean value) {
        AumentoVelocidad = value;
        Curvas();
    }

    public final boolean Aumentado() {
        return AumentoVelocidad;
    }
}
