package ecp;

public class Odometer {

	public static double elapsed = 0;
	public static double dist = 0;
    public static double getDistance() {
        dist += getSpeed() * (Clock.getSeconds()-elapsed);
        elapsed = Clock.getSeconds();
        return dist;
    }
    public static double speed = 0;

    public static double getSpeed() {
        return speed;
    }
}
