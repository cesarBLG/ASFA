package ecp.Controles;

import ecp.ASFA;

public class ControlSecuenciaAA extends Control {

    public ControlSecuenciaAA(double time, TrainParameters param) {
        super(time, 0, 20, 0, param);
        Curvas();
    }

	@Override
	Curva[] getCurvas_ADIF(int O) {
		Curva VC = null;
		Curva IF = null;
        if (Modo == ASFA.Modo.CONV) {
            VC = new Curva(60);
            IF = new Curva(63);
        } else if (Modo == ASFA.Modo.AV) {
            VC = new Curva(100);
            IF = new Curva(103);
        }
		return new Curva[] {VC, IF};
	}

	@Override
	Curva[] getCurvas_AESF(int T, int v) {
		int vel;
		if (Modo == ASFA.Modo.AV) {
			vel = Math.min(T, 100);
		} else {
			vel = 60;
		}
		return Curva.generarCurvas(this, vel, vel);
	}
}
