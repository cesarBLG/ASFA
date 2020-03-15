package ecp.Controles;

import ecp.ASFA;
import ecp.ASFA.Modo;

public class ControlPreviaSeñalParada extends ControlFASF implements ControlReanudo {

	public ControlPreviaSeñalParada(double time, TrainParameters param) {
        super(time, 0, 0, 0, param);
        Curvas();
    }
	@Override
	Curva[] getCurvas(int O) {
		Curva VC = null;
		Curva IF = null;
        if (Modo == ASFA.Modo.AV || Modo == ASFA.Modo.BasicoAV || Modo == ASFA.Modo.CONV || Modo == ASFA.Modo.BasicoCONV) {
            if (ASFA_version < 3)
            {
            	if (O > 100) {
                    VC = new Curva(60, 30, 0.6, 1.5);
                    IF = new Curva(63, 33, 0.55, 3.5);
                } else if (O <= 100) {
                    VC = new Curva(50, 25, 0.36, 2.5);
                    IF = new Curva(53, 28, 0.36, 5.5);
                }
            }
            else
            {
            	if (O > 100) {
                    VC = new Curva(50, 15, 0.6, 1.5);
                    IF = new Curva(53, 18, 0.55, 3.5);
                } else if (O <= 100) {
                    VC = new Curva(40, 15, 0.36, 2.5);
                    IF = new Curva(43, 18, 0.36, 5.5);
                }
            }
        } else if (Modo == ASFA.Modo.RAM) {
        	if (O > 100) {
                VC = new Curva(50, 10, 0.6, 1.5);
                IF = new Curva(53, 13, 0.55, 3.5);
            } else if (O <= 100) {
                VC = new Curva(40, 10, 0.36, 2.5);
                IF = new Curva(43, 13, 0.36, 5.5);
            }
        }
		return new Curva[] {VC, IF};
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
	@Override
	public double UltimaDistancia() {
		return distancia;
	}
	@Override
	public void ActualizarDistancia(double val) {
		distancia = val;
	}
}
