package ecp.Controles;

import ecp.ASFA;

public class ControlZonaLimiteParada extends ControlFASF {

    public ControlZonaLimiteParada(TrainParameters param) {
        super(0, 0, 0, 0, param);
        Curvas();
    }

	@Override
	Curva[] getCurvas(int O) {
		Curva VC = null;
		Curva IF = null;
        if (Modo == ASFA.Modo.CONV || Modo == ASFA.Modo.AV) {
            VC = new Curva(15, 15, 0, 0);
            IF = new Curva(18, 18, 0, 0);
        } else if (Modo == ASFA.Modo.RAM) {
        	VC = new Curva(10, 10, 0, 0);
            IF = new Curva(13, 13, 0, 0);
        }
		return new Curva[] {VC, IF};
	}

	@Override
	Curva[] getCurvas_AESF(int T, int v) {
		int vel = Modo == ASFA.Modo.RAM ? 10 : 15;
		return Curva.generarCurvas(this, vel, vel);
	}
}
