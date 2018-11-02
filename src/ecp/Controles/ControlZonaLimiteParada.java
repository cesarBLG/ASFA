package ecp.Controles;

import ecp.ASFA;

public class ControlZonaLimiteParada extends ControlFASF {

    public ControlZonaLimiteParada(TrainParameters param, double time) {
        super(time, 0, 0, 0, param);
        Curvas();
    }

	@Override
	Curva[] getCurvas(int O) {
		Curva VC = null;
		Curva IF = null;
        if (Modo == ASFA.Modo.CONV || Modo == ASFA.Modo.BasicoCONV || Modo == ASFA.Modo.AV || Modo == ASFA.Modo.BasicoAV || Modo == ASFA.Modo.RAM) {
            VC = new Curva(15, 15, 0, 0);
            IF = new Curva(18, 18, 0, 0);
        }
		return new Curva[] {VC, IF};
	}
}
