package ecp.Controles;

import ecp.ASFA;

public class ControlPreanuncioLTV extends ControlLVI {

    public ControlPreanuncioLTV(double time, double distance, TrainParameters param) {
        super(time, param);
        DistanciaInicial = distance;
        Curvas();
    }
    @Override
    public boolean isReached(double time, double speed) {
    	return false;
    }
    @Override
    Curva[] getCurvas_ADIF(int O) {
    	Curva VC = null;
    	Curva IF = null;
    	if (O == 200) {
            IF = new Curva(203, 163, 0.5, 9);
            VC = new Curva(200, 160, 0.55, 7.5);
    	} else if (O == 180) {
            IF = new Curva(183, 163, 0.5, 9);
            VC = new Curva(180, 160, 0.55, 7.5);
    	} else {
    		IF = new Curva(O + 3);
    		VC = new Curva(O);
        }
        return new Curva[] {VC, IF};
    }
}
