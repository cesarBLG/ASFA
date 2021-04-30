package ecp.Controles;

import ecp.ASFA;

public class ControlSeñalParada extends ControlFASF implements ControlAumentable {

	public int recCount = 0;
	public double recStart = -1;
	public double lastTimeRec = -1;
	public double lastDistRec = -1;
    boolean AumentoVelocidad = false;
    public boolean conRebase = false;

    public ControlSeñalParada(TrainParameters param, double time, double dist, boolean conRebase) {
        super(time, dist, 20, 0, param);
        this.conRebase = conRebase;
        Curvas();
    }

    Curva[] getCurvas(int O) {
    	Curva VC = null;
    	Curva IF = null;
        if (Modo == ASFA.Modo.CONV || Modo == ASFA.Modo.AV) {
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
    public final boolean Aumentable() {
    	return !AumentoVelocidad;
    }

    @Override
    public double getVC(double time) {
        return VC.valor(time - TiempoInicial);
    }

	@Override
	Curva[] getCurvas_AESF(int T, int v) {
		int vel;
		if (Modo == ASFA.Modo.AV || Modo == ASFA.Modo.CONV) {
			vel = ((ControlSeñalParada)this).AumentoVelocidad ? Math.min(T, 100) : 40;
		} else {
			vel = ((ControlSeñalParada)this).AumentoVelocidad ? Math.min(T, 70) : 30;
		}
		return Curva.generarCurvas(this, vel, vel);
	}
}
