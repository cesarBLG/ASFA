package ecp;

import dmi.DMI;

public class Main {

    public static ASFA ASFA;
    public static boolean ORconnected = false;
	public static DMI dmi;

    public static void main(String args[]) {
    	dmi = new DMI();
        ASFA = new ASFA();
    }
}
