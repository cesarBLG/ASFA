package ecp.Controles;

import ecp.ASFA;
import ecp.FrecASFA;

public class ControlLVI extends Control implements ControlReanudo {

	FrecASFA Frecuencia1;
	FrecASFA Frecuencia2;
	
    public boolean Reached = false;

    public boolean AumentoVelocidad = false;
    
    public boolean isReached(double time, double speed) {
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
    @Override
	public Curva[] obtenerCurvasAlmacen(int T)
    {
    	Curva[] c = obtenerCurvasAlmacen(T, AumentoVelocidad, Frecuencia1.name()+"-"+Frecuencia2.name());
    	return c;
    }
    @Override
    public Curva[] getCurvas(int T) {
    	Curva[] curvas = super.getCurvas(T);
    	if (Reached) {
            curvas[0] = new Curva(curvas[0].OrdenadaFinal);
            curvas[1] = new Curva(curvas[1].OrdenadaFinal);
    	}
    	return curvas;
    }
    public boolean Aumentable;
    Curva[] getCurvas_ADIF(int O) {
    	int Vf;
    	if (Frecuencia1 == FrecASFA.L10 && Frecuencia2 == FrecASFA.L10)
    		Vf = modoRAM ? 70 : AumentoVelocidad ? 150 : 120;
    	else if (Frecuencia1 == FrecASFA.L10 && Frecuencia2 == FrecASFA.L11)
    		Vf = modoRAM ? 50 : AumentoVelocidad ? 100 : 80;
    	else if (Frecuencia1 == FrecASFA.L11 && Frecuencia2 == FrecASFA.L10)
    		Vf = modoRAM ? 40 : AumentoVelocidad ? 70 : 50;
    	else
    		Vf = modoRAM ? 30 : AumentoVelocidad ? 40 : 30;
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
        } else if (O < 120) {
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
    	int Vf;
    	if (Frecuencia1 == FrecASFA.L10 && Frecuencia2 == FrecASFA.L10)
    		Vf = modoRAM ? 70 : AumentoVelocidad ? 150 : 120;
    	else if (Frecuencia1 == FrecASFA.L10 && Frecuencia2 == FrecASFA.L11)
    		Vf = modoRAM ? 50 : AumentoVelocidad ? 100 : 80;
    	else if (Frecuencia1 == FrecASFA.L11 && Frecuencia2 == FrecASFA.L10)
    		Vf = modoRAM ? 40 : AumentoVelocidad ? 70 : 50;
    	else
    		Vf = modoRAM ? 30 : AumentoVelocidad ? 40 : 30;
    	if (modoRAM && AumentoVelocidad) return null;
    	return Curva.generarCurvas(this, T, Vf);
    }
    public ControlLVI(TrainParameters param, FrecASFA frec1, FrecASFA frec2, boolean aumentable, double time) {
        super(time, 0, 0, 0, param);
        Frecuencia1 = frec1;
        Frecuencia2 = frec2;
        Aumentable = aumentable;
        Curvas();
    }

    public void AumentarVelocidad() {
        if (Aumentable) {
        	AumentoVelocidad = true;
            Aumentable = false;
            Curvas();
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
