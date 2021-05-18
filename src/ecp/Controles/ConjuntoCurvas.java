package ecp.Controles;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import ecp.ASFA;
import ecp.Controles.TablaCurva.ModoCurvas;

public class ConjuntoCurvas
{
	String Control;
	String Extra;
	List<TablaCurva> Tablas = new ArrayList<>();
	List<TablaCurva> TablasAumento = new ArrayList<>();
	TablaCurva getTabla(ModoCurvas modo, boolean aumento)
	{
		for (TablaCurva t : aumento ? TablasAumento : Tablas)
		{
			if (t.Modos.contains(modo)) return t;
		}
		return null;
	}
	void addCurva(TablaCurva curva)
	{
		if (curva.ConAumento) TablasAumento.add(curva);
		else Tablas.add(curva);
	}
	void formatearTablaLua(BufferedWriter w, TablaCurva t) throws IOException
	{
		for (ModoCurvas m : t.Modos)
		{
			if ((m == ModoCurvas.BAS_CONV && t.Modos.contains(ModoCurvas.CONV)) ||
					(m == ModoCurvas.BAS_AV && t.Modos.contains(ModoCurvas.AV)) ||
					(m == ModoCurvas.BAS_RAM && t.Modos.contains(ModoCurvas.RAM)))
				continue;
			TablaCurva taum = getTabla(m, true);
			w.write("			[\""+m.toString()+"\"]={\n");
			for (Entry<Integer,Curva[]> e : t.tabla.entrySet())
			{
				w.write("				["+e.getKey()+"] = {\n");
				Curva[] curvas = e.getValue();
				Curva[] curvas_aum = taum!=null ? taum.getCurvas(e.getKey()) : null;
				for (int i=0; i<4; i++)
				{
					Curva c = null;
					if (i%2 == 0) c =  curvas[i/2];
					else if (curvas_aum != null) c = curvas_aum[i/2];
					if (c == null) continue;
					w.write("					["+(i+1)+"] = {");
					w.write(Integer.toString((int)c.OrdenadaOrigen));
					w.write(",");
					DecimalFormat fmt = new DecimalFormat("0.##");
					DecimalFormatSymbols s = fmt.getDecimalFormatSymbols();
					s.setDecimalSeparator('.');
					fmt.setDecimalFormatSymbols(s);
					w.write(fmt.format(c.TiempoReaccion));
					w.write(",");
					w.write(fmt.format(c.Deceleracion));
					w.write(",");
					w.write(Integer.toString((int)c.OrdenadaFinal));
					w.write("},\n");
				}
				w.write("				},\n");
			}
			w.write("			},\n");
		}
	}
	static int index = 0;
	void ToLua(BufferedWriter w)
	{
		try {
			w.write("	--Control: "+Control.substring(7));
			if(Extra != null) w.write("-"+Extra);
			w.write("\n");
			w.write("	["+index+"]={\n");
			w.write("		Aumento			= {");
			w.write("[\"CONV\"]=");
			w.write(getTabla(ModoCurvas.CONV, true)!=null ? "true" : "false");
			w.write(", [\"AV\"]=");
			w.write(getTabla(ModoCurvas.AV, true)!=null ? "true" : "false");
			w.write(", [\"RAM\"]=");
			w.write(getTabla(ModoCurvas.RAM, true)!=null ? "true" : "false");
			w.write("},\n");
			w.write("		CFreno = {\n");
			for (TablaCurva t : Tablas)
			{
				formatearTablaLua(w,t);
			}
			w.write("		}\n");
			w.write("	},\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}