package ecp.Controles;

import ecp.ASFA;

public class ControlSecuenciaAA extends Control {

    public ControlSecuenciaAA(double time, TrainParameters param) {
        super(time, 0, 20, 0, param);
        Curvas();
    }

	@Override
	Curva[] getCurvas(int O) {
		Curva VC = null;
		Curva IF = null;
        if (Modo == ASFA.Modo.CONV || Modo == ASFA.Modo.BasicoCONV) {
            VC = new Curva(60, 60, 0, 0);
            IF = new Curva(63, 63, 0, 0);
        } else if (Modo == ASFA.Modo.AV || Modo == ASFA.Modo.BasicoAV) {
            VC = new Curva(100, 100, 0, 0);
            IF = new Curva(103, 103, 0, 0);
        }
		return new Curva[] {VC, IF};
	}
}
