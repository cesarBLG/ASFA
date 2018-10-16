package ecp.Controles;

public class ControlViaLibreCondicional extends ControlFASF {

    public ControlViaLibreCondicional(double time, double T, double O, boolean Fixed) {
        super(time, 0, 0, 0);
        if (Fixed || T <= 160) {
            VC = new Curva(Math.min(T, 160), Math.min(T, 160), 0, 0);
            IF = new Curva(Math.min(T + 5, 163), Math.min(T + 5, 163), 0, 0);
        } else if (O == 180) {
            VC = new Curva(180, 160, 0.55, 7.5);
            IF = new Curva(185, 163, 0.5, 9);
        } else {
            VC = new Curva(200, 160, 0.55, 7.5);
            IF = new Curva(205, 163, 0.5, 9);
        }
    }
}
