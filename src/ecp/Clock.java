package ecp;

import java.util.Calendar;

public class Clock {
    static double external_time=0;
    static double passedRef = 0;
    
    public static void set_external_time(double time)
    {
    	if (!Config.UsarHoraExterna) return;
    	Calendar date = Calendar.getInstance();
    	date.set(Calendar.HOUR_OF_DAY, 0);
    	date.set(Calendar.MINUTE, 0);
    	date.set(Calendar.SECOND, 0);
    	date.set(Calendar.MILLISECOND, 0);
    	if(external_time==0)
    	{
    		double prevTime = getSeconds();
        	external_time = time + date.getTimeInMillis()/1000.0;
    		PaqueteRegistro.cambio_hora(prevTime);
    	}
    	else external_time = time + date.getTimeInMillis()/1000.0;
    	passedRef = System.currentTimeMillis();
    }
    
    public static void reset_local_time()
    {
    	double prevTime = getSeconds();
		external_time = 0;
		PaqueteRegistro.cambio_hora(prevTime);
    }
    
    public static double getSeconds() {
    	if (external_time != 0)
    		return external_time + (Math.min(System.currentTimeMillis()-passedRef, 500))/1000.0;
        return System.currentTimeMillis()/1000.0;
    }
}
