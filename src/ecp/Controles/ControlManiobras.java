package ecp.Controles;

public class ControlManiobras extends Control {
	Curva[] getCurvas(int O)
	{
		return new Curva[] {new Curva(30), new Curva(35)};
	}
	public ControlManiobras(TrainParameters param)
	{
		super(0,0,0,0,param);
		Curvas();
	}
}
