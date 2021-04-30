package ecp.Controles;

public class ControlTransicion extends ControlFASF {

    public ControlTransicion(TrainParameters param) {
        super(0, 0, 0, 0, param);
        Curvas();
    }

	@Override
	Curva[] getCurvas(int O) {
		Curva VC = null;
		Curva IF = null;
        VC = new Curva(T, T, 0, 0);
        IF = new Curva(T + 5, T + 5, 0, 0);
		return new Curva[] {VC, IF};
	}

	@Override
	Curva[] getCurvas_AESF(int T, int v) {
		return Curva.generarCurvas(this, T, T);
	}
}
