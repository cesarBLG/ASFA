package ecp.Controles;

import ecp.ASFA;

public class ControlPasoDesvío extends Control {
	boolean AnteriorAumVel;
	Curva[] getCurvas(int O)
	{
		Curva VC = null;
		Curva IF = null;
		if(Modo == ASFA.Modo.CONV)
		{
	        if (AnteriorAumVel) {
	        	IF = new Curva(93);
	            VC = new Curva(90);
	        } else {
	        	IF = new Curva(63);
	            VC = new Curva(60);
	        }
		}
		if(Modo == ASFA.Modo.AV)
		{
	        if (AnteriorAumVel) {
	        	IF = new Curva(163);
	            VC = new Curva(160);
	        } else {
	        	IF = new Curva(103);
	            VC = new Curva(100);
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
