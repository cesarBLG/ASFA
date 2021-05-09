package ecp.Controles;

public class ControlBTS extends Control {
	int Vbts;
	Curva[] getCurvas_ADIF(int O) {
		Curva VC = new Curva(Math.min(T, Vbts));
		Curva IF = new Curva(Math.min(T + 5, Vbts + 5));
		return new Curva[] {VC, IF};
	}
    public ControlBTS(TrainParameters param, int Vbts) {
        super(0, 0, 0, 0, param);
        this.Vbts = Vbts;
        Curvas();
    }
	@Override
	Curva[] getCurvas_AESF(int T, int v) {
		return getCurvas_ADIF(T);
	}

}
