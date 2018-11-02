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
        VC = new Curva(T, T, 0, 0);
        IF = new Curva(T + 5, T + 5, 0, 0);
		return new Curva[] {VC, IF};
	}
}
