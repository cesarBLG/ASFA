package ecp.Controles;

import ecp.ASFA;

public class ControlArranque extends ControlFASF {
	@Override
	Curva[] getCurvas_AESF(int T, int v) {
		return getCurvas(T);
	}
	Curva[] getCurvas(int O) {
		Curva VC = new Curva(Math.min(T, 140));
		Curva IF = new Curva(Math.min(T + 5, 145));
		return new Curva[] {VC, IF};
	}
    public ControlArranque(TrainParameters param) {
        super(0, 0, 0, 0, param);
        Curvas();
    }
}
