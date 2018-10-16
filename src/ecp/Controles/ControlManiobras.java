package ecp.Controles;

public class ControlManiobras extends Control {
	public ControlManiobras() {
		super(0,0,0,0);
        VC = new Curva(30);
        IF = new Curva(35);
	}

}
