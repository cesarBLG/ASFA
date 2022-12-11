package ecp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import dmi.DMI;
import dmi.Sound;

public class Main {

    public static ASFA ASFA = null;
	public static DMI dmi = null;
    public static ASFA ASFA2 = null;
	
	public static int num = 0;

    public static void main(String args[]) {
    	Config.load();
    	/*try {
        	System.setErr(new PrintStream("/dev/null"));
			//System.setErr(new PrintStream(new FileOutputStream("errores.log", true)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}*/
    	if(args.length!=0) num = Integer.parseInt(args[0]);
    	else num = Config.Modo;
        if(num!=2) ASFA = new ASFA(true);
    	if(num!=1) dmi = new DMI();
    	//ASFA2 = new ASFA(false);
    }
}
