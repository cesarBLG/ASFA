package ecp;

public class Clock {

    static double startval = 0;

    static double external_time=0;
    
    public static void set_external_time(double time)
    {
    	if (external_time == 0) {
    		startval = time-getSeconds();
    	}
    	external_time = time;
    }
    
    public static double getSeconds() {
    	if (external_time != 0)
    		return external_time - startval;
        if (startval == 0) {
            startval = System.currentTimeMillis()/1000.0;
            return 0;
        }
        double val = System.currentTimeMillis()/1000.0 - startval;
        return val;
    }
}
