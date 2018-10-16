package ecp.Controles;

public class ControlBTS extends Control {

	public ControlBTS(double T, double Vbts) {
		super(0, 0, 0, 0);
        VC = new Curva(Math.min(T, Vbts));
        IF = new Curva(Math.min(T + 5, Vbts + 5));
	}

}
