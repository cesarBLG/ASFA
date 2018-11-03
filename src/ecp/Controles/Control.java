package ecp.Controles;

import ecp.ASFA;
import ecp.Clock;

public abstract class Control {

    public Curva IF = null;
    public Curva VC = null;
    public double TiempoInicial = 0;
    public double DistanciaInicial = 0;
    public double TiempoVigencia = 0;
    public double DistanciaVigencia = 0;
    public double TiempoVAlcanzada = 0;
    public double TiempoRec = 0;
    public int T;
    public int O;
    public boolean curvasT120;
    boolean modoRAM;
    ASFA.Modo Modo;
    
    public Control(double t0, double d0, double tv, double dv, TrainParameters param) {
        TiempoInicial = t0;
        DistanciaInicial = d0;
        TiempoVigencia = tv;
        DistanciaVigencia = dv;
        TiempoRec = Clock.getSeconds();
        curvasT120 = param.curvasT120;
        O = param.O;
        T = param.T;
        Modo = param.Modo;
        modoRAM = param.modoRAM;
    }
    abstract Curva[] getCurvas(int O);
    public void Curvas() {
    	if(O==100 && T==100 && curvasT120)
    	{
    		Curva[] curvasT120 = getCurvas(120);
    		VC = curvasT120[0];
    		IF = curvasT120[1];
    		IF.OrdenadaOrigen = Math.min(IF.OrdenadaOrigen, T + (IF.OrdenadaOrigen - VC.OrdenadaOrigen));
    		VC.OrdenadaOrigen = Math.min(VC.OrdenadaOrigen, T);
    	}
    	else
    	{
        	Curva[] curvasO = getCurvas(O);
        	VC = curvasO[0];
        	IF = curvasO[1];
    	}
    	Curva[] curvasT = getCurvas(curvasT120 ? 120 : T);
    	if(VC.OrdenadaFinal<curvasT[0].OrdenadaFinal)
    	{
    		VC.OrdenadaFinal = curvasT[0].OrdenadaFinal;
    		IF.OrdenadaFinal = curvasT[1].OrdenadaFinal;
    	}
    }
    public double getIF(double T) {
        return TiempoInicial != 0 ? IF.valor(T - TiempoInicial) : IF.OrdenadaFinal;
    }

    public double getVC(double T) {
        double v = TiempoInicial != 0 ? VC.valor(T - TiempoInicial) : VC.OrdenadaFinal;
        if (v == VC.OrdenadaFinal && TiempoVAlcanzada == 0) {
            TiempoVAlcanzada = T;
        }
        return v;
    }
}
