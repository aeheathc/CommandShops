package net.centerleft.localshops;

public class Location {
    private long[] xyzA = { 0, 0, 0 };
    private long[] xyzB = { 0, 0, 0 };

    public Location(long[] xyzA, long[] xyzB) {
	this.xyzA = xyzA.clone();
	this.xyzB = xyzB.clone();
    }

    public Location() {

    }

    public long[] getLocation1() {
	return xyzA;
    }

    public long[] getLocation2() {
	return xyzB;
    }

    public boolean setLocation(long[] xyzA, long[] xyzB) {
	this.xyzA = xyzA.clone();
	this.xyzB = xyzB.clone();
	return true;
    }
}
