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
			w.write("		[\""+m.toString()+(t.ConAumento ? "-AUMENTO" : "")+"\"]={\n");
			for (Entry<Integer,Curva[]> e : t.tabla.entrySet())
			{
				w.write("			["+e.getKey()+"] = {\n");
				for (int i=0; i<2; i++)
				{
					Curva c = e.getValue()[i];
					w.write("				["+i+"] = {");
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
				w.write("			},\n");
			}
			w.write("		},\n");
		}
	}
	void ToLua(BufferedWriter w)
	{
		try {
			w.write("	Nombre=\""+Control.substring(7));
			if(Extra != null) w.write("-"+Extra);
			w.write("\",\n");
			w.write("	CFreno = {\n");
			for (TablaCurva t : Tablas)
			{
				formatearTablaLua(w,t);
			}
			for (TablaCurva t : TablasAumento)
			{
				formatearTablaLua(w,t);
			}
			w.write("	}\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}