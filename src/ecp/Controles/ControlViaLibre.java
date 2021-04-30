package ecp.Controles;

public class ControlViaLibre extends ControlFASF {

    public ControlViaLibre(TrainParameters param, double time) {
        super(time, 0, 0, 0, param);
        Curvas();
    }

	@Override
	Curva[] getCurvas(int O) {
		Curva VC = null;
		Curva IF = null;
        VC = new Curva(T);
        IF = new Curva(T + 5);
		return new Curva[] {VC, IF};
	}

	@Override
	Curva[] getCurvas_AESF(int T, int v) {
		return Curva.generarCurvas(this, T, T);
	}
}
