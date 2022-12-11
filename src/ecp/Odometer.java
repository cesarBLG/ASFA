package ecp;

public class Odometer {

	static double prev = 0;
	static double dist = 0;
	static boolean computeDistance = true;
    public static double getDistance() {
        if (computeDistance)
        {
        	dist += getSpeed() * (Clock.getSeconds()-prev);
            prev = Clock.getSeconds();
        }
        return dist;
    }
    public static double speed = 0;

    public static double getSpeed() {
        return speed;
    }
    static int pulsosVuelta=88;
    static double diametroRueda;
    public static void received(long pulses, long elapsedMicros)
    {
    	double d = pulses*Math.PI*diametroRueda/pulsosVuelta;
    	dist += d;
    	speed = d/elapsedMicros*1000000;
    }
}
