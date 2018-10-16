package ecp.Controles;

public class ControlArranque extends ControlFASF {

    public ControlArranque(double T) {
        super(0, 0, 0, 0);
        VC = new Curva(Math.min(T, 140));
        IF = new Curva(Math.min(T + 5, 145));
    }
}
