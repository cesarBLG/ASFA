package ecp.Controles;

import ecp.ASFA;

public class ControlSeñalParada extends ControlFASF implements ControlAumentable {

    private boolean AumentoVelocidad = false;

    public ControlSeñalParada(TrainParameters param, double time) {
        super(time, 0, 20, 0, param);
        Curvas();
    }

    Curva[] getCurvas(int O) {
    	Curva VC = null;
    	Curva IF = null;
        if (Modo == ASFA.Modo.CONV || Modo == ASFA.Modo.BasicoCONV || Modo == ASFA.Modo.AV || Modo == ASFA.Modo.BasicoAV) {
            if (AumentoVelocidad) {
                VC = new Curva(Math.min(T, 100));
                IF = new Curva(Math.min(T + 3, 103));
            } else {
                VC = new Curva(40);
                IF = new Curva(43);
            }
        }
        if( Modo == ASFA.Modo.RAM )
        {
        	if (AumentoVelocidad) {
                VC = new Curva(Math.min(T, 70));
                IF = new Curva(Math.min(T + 3, 73));
            } else {
                VC = new Curva(30);
                IF = new Curva(33);
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

    @Override
    public double getVC(double time) {
        return VC.valor(time - TiempoInicial);
    }
}
