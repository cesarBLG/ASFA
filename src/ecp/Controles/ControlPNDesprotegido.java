package ecp.Controles;

import ecp.ASFA;

public class ControlPNDesprotegido extends ControlPN implements ControlReanudo {

	boolean segundaCurva = false;
	public boolean Rec = false;
	Curva[] getCurvas(int O)
	{
		Curva VC = null;
		Curva IF = null;
		if(modoRAM)
		{
			VC = new Curva(30);
			IF = new Curva(33);
		}
		else
		{
			if (segundaCurva)
			{
	            IF = new Curva(83);
	            VC = new Curva(80);
			}
			else
			{
		        if (Modo == ASFA.Modo.CONV || Modo == ASFA.Modo.AV || Modo == ASFA.Modo.BTS) {
		            if (O >= 160) {
		                IF = new Curva(163, 33, 0.5, 9);
		                VC = new Curva(160, 30, 0.6, 7.5);
		            } else if (O == 140) {
		                IF = new Curva(143, 33, 0.5, 10);
		                VC = new Curva(140, 30, 0.6, 7.5);
		            } else if (O == 120) {
		                IF = new Curva(123, 33, 0.36, 12);
		                VC = new Curva(120, 30, 0.46, 7.5);
		            } else if (O <= 100) {
		                IF = new Curva(O + 3, 33, 0.26, 11);
		                VC = new Curva(O, 30, 0.36, 7.5);
		            }
		        }
			}
		}
        return new Curva[] {VC, IF};
	}
    public ControlPNDesprotegido(TrainParameters param, double time, double dist) {
        super(time, dist, 0, 1800, param);
        Curvas();
    }

    @Override
    public double getVC(double time) {
        double val = super.getVC(time);
        if (val == 30) {
        	segundaCurva = true;
            Curvas();
            return super.getVC(time);
        }
        return val;
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
