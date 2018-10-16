package ecp.Controles;

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

    public Control(double t0, double d0, double tv, double dv) {
        TiempoInicial = t0;
        DistanciaInicial = d0;
        TiempoVigencia = tv;
        DistanciaVigencia = dv;
        TiempoRec = Clock.getSeconds();
    }

    public final double getIF(double T) {
        return TiempoInicial != 0 ? IF.valor(T - TiempoInicial) : IF.OrdenadaFinal;
    }

    public double getVC(double T) {
        double v = TiempoInicial != 0 ? VC.valor(T - TiempoInicial) : VC.OrdenadaFinal;
        if (v == VC.OrdenadaFinal && TiempoVAlcanzada == 0) {
            TiempoVAlcanzada = T;
        }
        return v;
    }
    
    //abstract Curva getCurvaVC(int T);
    //abstract Curva getCurvaIF(int T);
}
