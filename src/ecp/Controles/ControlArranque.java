package ecp.Controles;

import ecp.ASFA;

public class ControlArranque extends ControlFASF {
	@Override
	Curva[] getCurvas_AESF(int T, int v) {
		return getCurvas_ADIF(T);
	}
	Curva[] getCurvas_ADIF(int O) {
		Curva VC = new Curva(Math.min(T, 140));
		Curva IF = new Curva(Math.min(T + 5, 145));
		return new Curva[] {VC, IF};
	}
    public ControlArranque(TrainParameters param) {
        super(0, 0, 0, 0, param);
        Curvas();
    }
}
