package ecp;

import java.io.IOException;

import dmi.DMI;
import dmi.Sound;

public class Main {

    public static ASFA ASFA = null;
	public static DMI dmi = null;
	
	public static int num = 0;

    public static void main(String args[]) {
    	try {
			Runtime.getRuntime().exec("./server");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if(args.length!=0) num = Integer.parseInt(args[0]);
        if(num!=2) ASFA = new ASFA();
    	if(num!=1) dmi = new DMI();
    	Sound sound = new Sound();
    }
}
