package ecp.Controles;

import ecp.ASFA;

public class ControlLVI extends Control implements ControlReanudo {

    public boolean Reached = false;

    public boolean AumentoVelocidad = false;
    
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
    int Vf;
    Curva[] getCurvas(int O) {
    	Curva VC = null;
    	Curva IF = null;
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
    	if (Reached) {
            VC = new Curva(VC.OrdenadaFinal);
            IF = new Curva(IF.OrdenadaFinal);
    	}
        return new Curva[] {VC, IF};
    }
    @Override
	Curva[] getCurvas_AESF(int T, int v) {
    	return Curva.generarCurvas(this, T, Vf);
    }
    public ControlLVI(TrainParameters param, int Vf, boolean aumentable, double time) {
        super(time, 0, 0, 0, param);
        this.Vf = Vf;
        this.Aumentable = aumentable;
        Curvas();
    }

    public void AumentarVelocidad() {
        if (Aumentable) {
        	int velAum = 0;
        	if (VC.OrdenadaFinal == 30) velAum = 10;
        	else if (VC.OrdenadaFinal == 50) velAum = 20;
        	else if (VC.OrdenadaFinal == 80) velAum = 30;
        	else if (VC.OrdenadaFinal == 120) velAum = 30;
            IF.OrdenadaFinal = Math.min(IF.OrdenadaFinal + velAum, IF.valor(0));
            VC.OrdenadaFinal = Math.min(VC.OrdenadaFinal + velAum, VC.valor(0));
            Aumentable = false;
        }
    }

    protected ControlLVI(double time, TrainParameters param) {
        super(time, 0, 0, 0, param);
    }
    boolean activado = false;
	@Override
	public void Activar(boolean val) {
		activado = val;
	}
	@Override
	public boolean Activado() {
		return activado;
	}
	double distancia = -1;

	public boolean Degradado;
	@Override
	public double UltimaDistancia() {
		return distancia;
	}
	@Override
	public void ActualizarDistancia(double val) {
		distancia = val;
	}
}
