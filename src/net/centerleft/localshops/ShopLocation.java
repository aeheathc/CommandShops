package net.centerleft.localshops;

public class ShopLocation {
    private long x = 0;
    private long y = 0;
    private long z = 0;

    public ShopLocation(long x, long y, long z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ShopLocation(long[] xyz) {
        this.x = xyz[0];
        this.y = xyz[1];
        this.z = xyz[2];
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    public long getZ() {
        return z;
    }

    public long[] toArray() {
        return new long[] { x, y, z };
    }

    public String toString() {
        return String.format("%d, %d, %d", x, y, z);
    }
}
