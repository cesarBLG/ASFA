package ecp.Controles;

public class ControlLVI extends Control {

    public boolean Reached = false;

    public final boolean isReached(double time, double speed) {
        if (Reached) {
            return true;
        }
        if (speed <= VC.OrdenadaFinal) {
            Reached = true;
            VC = new Curva(VC.OrdenadaFinal);
            IF = new Curva(IF.OrdenadaFinal);
        }
        return Reached;
    }
    public boolean Aumentable;

    public ControlLVI(double O, double Vf, boolean aumentable, double time) {
        super(time, 0, 0, 0);
        if (O > 160) {
            IF = new Curva(O + 5, Vf + 3, 0.5, 9);
            VC = new Curva(O, Vf, 0.6, 7.5);
        } else if (O == 160) {
            IF = new Curva(163, Vf + 3, 0.5, 9);
            VC = new Curva(160, Vf, 0.6, 7.5);
        } else if (O == 140) {
            IF = new Curva(143, Vf + 3, 0.5, 10);
            VC = new Curva(140, Vf, 0.6, 7.5);
        } else if (O == 120) {
            IF = new Curva(123, Vf + 3, 0.36, 12);
            VC = new Curva(120, Vf, 0.46, 7.5);
        } else if (O <= 100) {
            IF = new Curva(O + 3, Vf + 3, 0.26, 11);
            VC = new Curva(O, Vf, 0.36, 7.5);
        }
        if (Vf > VC.valor(0)) {
            IF.OrdenadaFinal = IF.valor(0);
            VC.OrdenadaFinal = VC.valor(0);
        }
    }

    public void AumentarVelocidad() {
        if (Aumentable) {
            IF.OrdenadaFinal = Math.min(IF.OrdenadaFinal + 20, IF.valor(0));
            VC.OrdenadaFinal = Math.min(VC.OrdenadaFinal + 20, VC.valor(0));
            Aumentable = false;
        }
    }

    protected ControlLVI(double time) {
        super(time, 0, 0, 0);
    }
}
