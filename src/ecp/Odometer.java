package ecp;

public class Odometer {

	public static double prev = 0;
	public static double dist = 0;
    public static double getDistance() {
        dist += getSpeed() * (Clock.getSeconds()-prev);
        prev = Clock.getSeconds();
        return dist;
    }
    public static double speed = 0;

    public static double getSpeed() {
        return speed;
    }
}
