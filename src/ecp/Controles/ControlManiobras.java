package ecp.Controles;

public class ControlManiobras extends Control {
	Curva[] getCurvas_ADIF(int O)
	{
		if(modoRAM) return new Curva[] {new Curva(10), new Curva(15)};
		return new Curva[] {new Curva(30), new Curva(35)};
	}
	public ControlManiobras(TrainParameters param)
	{
		super(0,0,0,0,param);
		Curvas();
	}
	@Override
	Curva[] getCurvas_AESF(int T, int v) {
		return getCurvas_ADIF(T);
	}
}
