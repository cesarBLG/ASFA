package ecp;

public class Clock {

    static long startval = 0;

    public static double getSeconds() {
        if (startval == 0) {
            startval = System.currentTimeMillis();
            return 0;
        }
        double val = ((double) (System.currentTimeMillis() - startval)) / 1000;
        return val;
    }
}
