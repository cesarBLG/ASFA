package ecp;

import java.util.Map.Entry;

import dmi.Botones.Botón.TipoBotón;
import ecp.ASFA.Modo;

public class Autotest {
	ASFA ASFA;
	public boolean AvisadorBasico = true;
	public boolean AvisadorNormal = true;
	public boolean Pulsadores = true;
	public boolean Redundancia = true;
	public boolean CompatibilidadVia = true;
	public boolean EficaciaFP = true;
	public Autotest(ASFA asfa)
	{
		ASFA = asfa;
	}
	public void autotest(boolean inicio)
	{
        for (Entry<TipoBotón, EstadoBotón> b : ASFA.display.botoneraActiva.entrySet())
        {
        	if (b.getKey() != TipoBotón.ASFA_básico && b.getKey() != TipoBotón.Conex && b.getValue().averiado(inicio ? 3 : 10))
        	{
                ASFA.Registro.falloPulsadores(ASFA.display.cabinaActiva == -1 ? 2 : 1, true);
                Pulsadores = false;
        	}
        }
	}
	public boolean Eficacia(Modo modo, boolean basico)
	{
		boolean eficacia = true;
		if (basico) eficacia &= AvisadorBasico;
		else eficacia &= ASFA.display.pantallaconectada && AvisadorNormal && ASFA.display.pantallaactiva;
		if (modo != Modo.MBRA) eficacia &= EficaciaFP;
		eficacia &= Pulsadores;
		eficacia &= Redundancia;
		eficacia &= CompatibilidadVia;
		return eficacia;
	}
}
