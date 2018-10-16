package ecp.Controles;

public class ControlViaLibre extends ControlFASF {

    public ControlViaLibre(double T, double time) {
        super(time, 0, 0, 0);
        VC = new Curva(T, T, 0, 0);
        IF = new Curva(T + 5, T + 5, 0, 0);
    }
}
