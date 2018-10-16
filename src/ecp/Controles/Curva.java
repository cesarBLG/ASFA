package ecp.Controles;

public class Curva {

    private double OrdenadaOrigen;
    public double OrdenadaFinal;
    private double Deceleracion;
    private double TiempoReaccion;

    public Curva(double O0, double Of, double Dec, double TReac) {
        OrdenadaOrigen = O0;
        OrdenadaFinal = Of;
        Deceleracion = Dec;
        TiempoReaccion = TReac;
    }

    public Curva(double vconst) {
        OrdenadaOrigen = vconst;
        OrdenadaFinal = vconst;
        Deceleracion = 0;
        TiempoReaccion = 0;
    }

    public final double valor(double d) {
        return Math.max(OrdenadaFinal, Math.min(OrdenadaOrigen, 3.6 * ((OrdenadaOrigen / 3.6) - Deceleracion * (d - TiempoReaccion))));
    }
}
