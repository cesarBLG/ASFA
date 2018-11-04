package ecp.Controles;

public interface ControlReanudo {
	void Activar(boolean val);
	boolean Activado();
	double UltimaDistancia();
	void ActualizarDistancia(double val);
}
