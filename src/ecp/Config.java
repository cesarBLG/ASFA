package ecp;

import java.io.BufferedReader;
import java.io.FileReader;

public class Config {
	public static int Version=2;
	public static String Fabricante="DIMETRONIC";
	public static int Modo=0;
	public static boolean SoloPantalla=false;
	public static boolean PantallaCompleta=false;
	public static boolean ApagarOrdenador=false;
	public static void load()
	{
    	try
    	{
    		FileReader fileReader = new FileReader("config.ini");
    		BufferedReader bufferedReader = new BufferedReader(fileReader);
    		String line = bufferedReader.readLine();
    		while (line != null)
    		{
    			String[] token = line.trim().split("=");
    			if (token.length == 2)
    			{
        			if (token[0].trim().equalsIgnoreCase("fabricante"))
        			{
        				String fabr = token[1].trim();
        				int c1 = fabr.indexOf('"');
        				if (c1 >= 0) fabr = fabr.substring(c1+1);
        				int c2 = fabr.lastIndexOf('"');
        				if (c2 >= 0) fabr = fabr.substring(0, c2);
        				Fabricante = fabr.toUpperCase();
        			}
        			else if (token[0].trim().equalsIgnoreCase("version"))
        			{
        				Version = Integer.parseInt(token[1].trim());
        			}
        			else if (token[0].trim().equalsIgnoreCase("modo"))
        			{
        				String mod = token[1].trim();
        				if (mod.equalsIgnoreCase("DMI")) Modo = 2;
        				else if (mod.equalsIgnoreCase("ECP")) Modo = 1;
        				else Modo = 0;
        			}
        			else if (token[0].trim().equalsIgnoreCase("solopantalla"))
        			{
        				String val = token[1].trim().toLowerCase();
        				SoloPantalla = val.equals("true") || val.equals("1");
        			}
        			else if (token[0].trim().equalsIgnoreCase("pantallacompleta"))
        			{
        				String val = token[1].trim().toLowerCase();
        				PantallaCompleta = val.equals("true") || val.equals("1");
        			}
        			else if (token[0].trim().equalsIgnoreCase("apagarordenador"))
        			{
        				String val = token[1].trim().toLowerCase();
        				ApagarOrdenador = val.equals("true") || val.equals("1");
        			}
    			}
        		line = bufferedReader.readLine();
    		}
    		bufferedReader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
