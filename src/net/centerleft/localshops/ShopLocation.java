package net.centerleft.localshops;

import org.bukkit.Location;

public class ShopLocation {
    private double x = 0;
    private double y = 0;
    private double z = 0;

    public ShopLocation(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ShopLocation(double[] xyz) {
        this.x = xyz[0];
        this.y = xyz[1];
        this.z = xyz[2];
    }
    
    public ShopLocation(Location loc) {
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double[] toArray() {
        return new double[] { x, y, z };
    }

    public String toString() {
        return String.format("%.0f, %.0f, %.0f", x, y, z);
    }
}
