package ecp.Controles;

import ecp.ASFA;

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
	@Override
	Curva[] getCurvas_AESF(int T, int v) {
		double vfc=0,v0c=0;
		if (T>=160) {
			vfc = 155;
			if (v > 180) v0c = 200;
			else if (v > 160) v0c = 180;
			else v0c = 160;
		} else {
			v0c = vfc = T;
		}
		return Curva.generarCurvas(this, v0c, vfc);
	}
    public ControlPNProtegido(TrainParameters param, double time, double dist) {
        super(time, dist, 0, 1800, param);
        Curvas();
    }
}
