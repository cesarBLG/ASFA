package ecp.Controles;

public class ControlPNProtegido extends Control {

    public ControlPNProtegido(double O, double T, double time, double dist) {
        super(time, dist, 0, 1800);
        if (O == 200) {
            IF = new Curva(205, 158, 0.5, 9);
            VC = new Curva(200, 155, 0.55, 6);
        } else if (O == 180) {
            IF = new Curva(185, 158, 0.5, 9);
            VC = new Curva(180, 155, 0.55, 7.5);
        } else if (O == 160) {
            IF = new Curva(165, 158, 0.5, 9);
            VC = new Curva(160, 155, 0.55, 7.5);
        } else {
            IF = new Curva(Math.min(T + 5, 158));
            VC = new Curva(Math.min(T, 155));
        }
    }
}
