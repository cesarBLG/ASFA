package ecp.Controles;

import ecp.ASFA;
import ecp.ASFA.Modo;

public class ControlPreviaSeñalParada extends ControlFASF {

	public ControlPreviaSeñalParada(double time, TrainParameters param) {
        super(time, 0, 0, 0, param);
        Curvas();
    }
	@Override
	Curva[] getCurvas(int O) {
		Curva VC = null;
		Curva IF = null;
        if (Modo == ASFA.Modo.AV || Modo == ASFA.Modo.BasicoAV || Modo == ASFA.Modo.CONV || Modo == ASFA.Modo.BasicoCONV) {
            if (O > 100) {
                VC = new Curva(50, 15, 0.6, 1.5);
                IF = new Curva(53, 18, 0.55, 3.5);
            } else if (O <= 100) {
                VC = new Curva(40, 15, 0.36, 2.5);
                IF = new Curva(43, 18, 0.36, 5.5);
            }
        } else if (Modo == ASFA.Modo.RAM) {
        	if (O > 100) {
                VC = new Curva(50, 10, 0.6, 1.5);
                IF = new Curva(53, 13, 0.55, 3.5);
            } else if (O <= 100) {
                VC = new Curva(40, 10, 0.36, 2.5);
                IF = new Curva(43, 13, 0.36, 5.5);
            }
        }
		return new Curva[] {VC, IF};
	}
}
