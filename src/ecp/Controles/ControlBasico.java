package ecp.Controles;

public class ControlBasico extends Control {
	int Vbasico;
	Curva[] getCurvas(int O)
	{
		if(modoRAM) return new Curva[] {new Curva(Vbasico), new Curva(Vbasico+4)};
		return new Curva[] {new Curva(Vbasico), new Curva(Vbasico+5)};
	}
	public ControlBasico(TrainParameters param, int Vbasico)
	{
		super(0,0,0,0,param);
		this.Vbasico = Vbasico;
		Curvas();
	}
}
