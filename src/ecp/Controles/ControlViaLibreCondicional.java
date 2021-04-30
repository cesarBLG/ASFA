package ecp.Controles;

public class ControlViaLibreCondicional extends ControlFASF {

	boolean Fixed;
    public ControlViaLibreCondicional(double time, TrainParameters param, boolean Fixed) {
        super(time, 0, 0, 0, param);
        this.Fixed = Fixed;
        Curvas();
    }
	@Override
	Curva[] getCurvas(int O) {
		Curva VC = null;
		Curva IF = null;
        if (Fixed || T <= 160 || O <= 160) {
            VC = new Curva(Math.min(T, 160), Math.min(T, 160), 0, 0);
            IF = new Curva(Math.min(T + 3, 163), Math.min(T + 3, 163), 0, 0);
        } else if (O == 180) {
            VC = new Curva(180, 160, 0.55, 7.5);
            IF = new Curva(185, 163, 0.5, 9);
        } else {
            VC = new Curva(200, 160, 0.55, 7.5);
            IF = new Curva(205, 163, 0.5, 9);
        }
		return new Curva[] {VC, IF};
	}
	@Override
	Curva[] getCurvas_AESF(int T, int v) {
    	double vfc=0,v0c=0;
		if (T>160) {
			vfc = 160;
			if (v > 180) v0c = 200;
			else v0c = 180;
		} else {
			v0c = vfc = T;
		}
    	return Curva.generarCurvas(this, v0c, vfc);
	}
}
