package ecp.Controles;

public class ControlArranque extends ControlFASF {
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
