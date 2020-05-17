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
    public int speed;
    public boolean curvasT120;
    public int ASFA_version;
    boolean modoRAM;
    public ASFA.Modo Modo;
    public boolean basico = false;
    public boolean Velado = false;
    
    public Control(double t0, double d0, double tv, double dv, TrainParameters param) {
        TiempoInicial = t0;
        DistanciaInicial = d0;
        TiempoVigencia = tv;
        DistanciaVigencia = dv;
        TiempoRec = Clock.getSeconds();
        curvasT120 = param.curvasT120;
        speed = param.Speed;
        T = param.T;
        Modo = param.Modo;
        modoRAM = param.modoRAM;
        basico = param.basico;
        ASFA_version = param.ASFA_version;
    }
    abstract Curva[] getCurvas(int O);
    public void Curvas() {
    	int O = T;
    	if (ASFA_version >= 3)
    	{
            O = (int) Math.min(speed + 5, T);
    		if (O <= 50) O = 50;
    		else if (O <= 60) O = 60;
    		else if (O <= 70) O = 70;
    		else if (O <= 80) O = 80;
    		else if (O <= 90) O = 90;
    		else if (O <= 100) O = 100;
    		else if (O <= 120) O = 120;
    		else if (O <= 140) O = 140;
    		else if (O <= 160) O = 160;
    		else if (O <= 180) O = 180;
    		else O = 200;
    	}
		Curvas(O);
    }
    public void Curvas(int O)
    {
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
    		if (O == 50) O = 60;
    		else if (O == 60) O = 70;
    		else if (O == 70) O = 80;
    		else if (O == 80) O = 90;
    		else if (O == 90) O = 100;
    		else if (O == 100) O = 120;
    		else if (O == 120) O = 140;
    		else if (O == 140) O = 160;
    		else if (O == 160) O = 180;
    		else O = 200;
    		if(O>T) return;
    		Curvas(O);
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
    public boolean equals(Control c)
    {	
    	if (c == null) return false;
    	if (this != c) return false;
    	if (c instanceof ControlAumentable && this instanceof ControlAumentable
    			&& (((ControlAumentable) c).Aumentado() != ((ControlAumentable) this).Aumentado())) return false;
    	return true;
    }
}
