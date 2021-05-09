package ecp.Controles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import ecp.ASFA;

import java.util.Map.Entry;

public class TablaCurva
{
	enum ModoCurvas
	{
		CONV,
		BAS_CONV,
		AV,
		BAS_AV,
		RAM,
		BAS_RAM,
		BTS,
		BTS_RAM;
		static ModoCurvas fromModo(ASFA.Modo modo, boolean basico, boolean modoRAM)
		{
			if (modo == ASFA.Modo.CONV) return basico ? ModoCurvas.BAS_CONV : ModoCurvas.CONV;
			if (modo == ASFA.Modo.AV) return basico ? ModoCurvas.BAS_AV : ModoCurvas.AV;
			if (modo == ASFA.Modo.RAM) return basico ? ModoCurvas.BAS_RAM : ModoCurvas.RAM;
			if (modo == ASFA.Modo.BTS) return modoRAM ? ModoCurvas.BTS_RAM : ModoCurvas.BTS;
			return null;
		}
	}
	boolean Valida;
	String Control;
	boolean ConAumento;
	String Extra;
	List<ModoCurvas> Modos = new ArrayList<>();
	TreeMap<Integer, Curva[]> tabla = new TreeMap<>();
	private double parseOrZero(String s)
	{
		if (s.isEmpty()) return 0;
		return Double.parseDouble(s);
	}
	public TablaCurva(BufferedReader read) throws IOException
	{
		String[] arr = read.readLine().split(";");
		Control = arr[0].trim();
		ConAumento = arr.length>1 && arr[1].trim().toUpperCase().equals("CON AUMENTO");
		Extra = arr.length>2 ? arr[2].trim() : null;
		for (String str : read.readLine().split(";"))
		{
			Modos.add(ModoCurvas.valueOf(str.trim().replace(' ', '_').toUpperCase()));
		}
		String line;
		while(!(line = read.readLine().trim()).startsWith(";"))
		{
			String[] datos = line.split(";");
			int T = Integer.parseInt(datos[0].trim());
			int v0c = Integer.parseInt(datos[1].trim());
			double trc = parseOrZero(datos[2].trim());
			double ac = parseOrZero(datos[3].trim());
			int vfc = Integer.parseInt(datos[4].trim());
			int v0i = Integer.parseInt(datos[5].trim());
			double tri = parseOrZero(datos[6].trim());
			double ai = parseOrZero(datos[7].trim());
			int vfi = Integer.parseInt(datos[8].trim());
			Curva VC = new Curva(v0c, vfc, ac, trc);
			Curva IF = new Curva(v0i, vfi, ai, tri);
			tabla.put(T, new Curva[] { VC, IF });
			Valida = true;
		}
	}
	public TablaCurva() {
	}
	public void ToFile(BufferedWriter w)
	{
		try {
			w.write(Control);
			if (ConAumento) w.write(";CON AUMENTO");
			if (Extra != null) w.write((ConAumento ? ";" : ";;") + Extra);
			w.write("\n");
			for (ModoCurvas m : Modos)
			{
				w.write(m.toString().replace('_', ' ')+";");
			}
			w.write("\n");
			for (int i=tabla.size()-1; i>=0; i--)
			{
				int T = (int) tabla.keySet().toArray()[i];
				DecimalFormat fmt = new DecimalFormat("0.##");
				DecimalFormatSymbols s = fmt.getDecimalFormatSymbols();
				s.setDecimalSeparator('.');
				fmt.setDecimalFormatSymbols(s);
				w.write(Integer.toString(T));
				w.write(";");
				Curva VC = tabla.get(T)[0];
				Curva IF = tabla.get(T)[1];
				w.write(Integer.toString((int)VC.OrdenadaOrigen));
				w.write(";");
				w.write(fmt.format(VC.TiempoReaccion));
				w.write(";");
				w.write(fmt.format(VC.Deceleracion));
				w.write(";");
				w.write(Integer.toString((int)VC.OrdenadaFinal));
				w.write(";");
				w.write(Integer.toString((int)IF.OrdenadaOrigen));
				w.write(";");
				w.write(fmt.format(IF.TiempoReaccion));
				w.write(";");
				w.write(fmt.format(IF.Deceleracion));
				w.write(";");
				w.write(Integer.toString((int)IF.OrdenadaFinal));
				w.write("\n");
			}
			w.write(";\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public Curva[] getCurvas(int T) {
		return tabla.floorEntry(T).getValue();
	}
}
