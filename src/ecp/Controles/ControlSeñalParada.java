package ecp.Controles;

import ecp.ASFA;

public class ControlSeñalParada extends ControlFASF implements ControlAumentable {

    private boolean AumentoVelocidad = false;
    private double T;
    private ASFA.Modo Modo;

    public ControlSeñalParada(double T, ASFA.Modo Modo, double time) {
        super(time, 0, 20, 0);
        this.T = T;
        this.Modo = Modo;
        Curvas();
    }

    private void Curvas() {
        if (Modo == ASFA.Modo.CONV || Modo == ASFA.Modo.BasicoCONV || Modo == ASFA.Modo.AV || Modo == ASFA.Modo.BasicoAV) {
            if (AumentoVelocidad) {
                VC = new Curva(Math.min(T, 100));
                IF = new Curva(Math.min(T + 3, 103));
            } else {
                VC = new Curva(40);
                IF = new Curva(43);
            }
        }
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
