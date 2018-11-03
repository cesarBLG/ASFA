package ecp.Controles;

public class ControlPNProtegido extends ControlPN {

	Curva[] getCurvas(int O)
	{
		Curva VC = null;
		Curva IF = null;
        if (O == 200) {
            IF = new Curva(205, 158, 0.5, 9);
            VC = new Curva(200, 155, 0.55, 6);
        } else if (O == 180) {
            IF = new Curva(185, 158, 0.5, 9);
            VC = new Curva(180, 155, 0.55, 7.5);
        } else if (O == 160) {
            IF = new Curva(165, 158, 0.5, 9);
            VC = new Curva(160, 155, 0.55, 7.5);
        } else {
            IF = new Curva(Math.min(T + 5, 158));
            VC = new Curva(Math.min(T, 155));
        }
        return new Curva[] {VC, IF};
	}
    public ControlPNProtegido(TrainParameters param, double time, double dist) {
        super(time, dist, 0, 1800, param);
        Curvas();
    }
}
