package ecp.Controles;

import ecp.ASFA;

public class ControlPasoDesvío extends Control {
	boolean AnteriorAumVel;
	Curva[] getCurvas(int O)
	{
		Curva VC = null;
		Curva IF = null;
        if (AnteriorAumVel) {
            if (Modo == ASFA.Modo.CONV || Modo == ASFA.Modo.BasicoCONV) {
                IF = new Curva(93, 93, 0, 0);
                VC = new Curva(90, 90, 0, 0);
            }
        } else {
            if (Modo == ASFA.Modo.CONV || Modo == ASFA.Modo.BasicoCONV) {
                IF = new Curva(63, 63, 0, 0);
                VC = new Curva(60, 60, 0, 0);
            }
        }
        return new Curva[] {VC, IF};
	}
    public ControlPasoDesvío(TrainParameters param, double time, boolean AnteriorAumVel) {
        super(time, 0, 20, 0, param);
        this.AnteriorAumVel = AnteriorAumVel;
        Curvas();
    }
}
