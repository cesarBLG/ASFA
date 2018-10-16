package ecp.Controles;

public class ControlTransicion extends ControlFASF {

    public ControlTransicion(double T) {
        super(0, 0, 0, 0);
        VC = new Curva(T, T, 0, 0);
        IF = new Curva(T + 5, T + 5, 0, 0);
    }
}
