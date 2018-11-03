package ecp.Controles;

import ecp.ASFA;

public class ControlLVIL1F1 extends ControlLVI implements ControlAumentable {

    private boolean AumentoVelocidad = false;

    public ControlLVIL1F1(double time, TrainParameters param) {
        super(time, param);
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
                    IF = new Curva(163, 63, 0.5, 9);
                    VC = new Curva(160, 60, 0.6, 7.5);
                } else if (O == 140) {
                    IF = new Curva(143, 63, 0.5, 10);
                    VC = new Curva(140, 60, 0.6, 7.5);
                } else if (O == 120) {
                    IF = new Curva(123, 63, 0.36, 12);
                    VC = new Curva(120, 60, 0.46, 7.5);
                } else if (O <= 100) {
                    IF = new Curva(O + 3, 63, 0.26, 11);
                    VC = new Curva(O, 60, 0.36, 7.5);
                }
            }
        }
        else if(Modo == ASFA.Modo.RAM)
        {
        	if (AumentoVelocidad) {
                if (O == 120) {
                    IF = new Curva(123, 53, 0.36, 12);
                    VC = new Curva(120, 50, 0.46, 7.5);
                } else if (O < 120) {
                    IF = new Curva(O + 3, 53, 0, 0);
                    VC = new Curva(O, 50, 0, 0);
                }
            } else {
                if (O == 120) {
                    IF = new Curva(123, 33, 0.36, 12);
                    VC = new Curva(120, 30, 0.46, 7.5);
                } else if (O < 120) {
                    IF = new Curva(O + 3, 33, 0.26, 11);
                    VC = new Curva(O, 30, 0.36, 7.5);
                }
            }
        }
        return new Curva[] {VC, IF};
    }

    public final void SpeedUp() {
        if (!Reached) {
            return;
        }
        VC = new Curva(VC.OrdenadaFinal + 20);
        IF = new Curva(IF.OrdenadaFinal + 20);
    }

    public final void SpeedDown() {
        if (!Reached) {
            return;
        }
        if(VC.OrdenadaFinal<40) return;
        VC = new Curva(VC.OrdenadaFinal - 20);
        IF = new Curva(IF.OrdenadaFinal - 20);
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
