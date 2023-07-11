package mekanism.common.lib.math;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class Quaternion {

    public static final Quaternion ONE = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);

    private double x;
    private double y;
    private double z;
    private double w;

    public Quaternion(double x, double y, double z, double w) {
        set(x, y, z, w);
    }

    public Quaternion(Vec3 axis, double angle, boolean degrees) {
        if (degrees) {
            angle *= (Math.PI / 180F);
        }

        double sin = Math.sin(angle / 2.0F);
        set(axis.x() * sin, axis.y() * sin, axis.z() * sin, Math.cos(angle / 2.0F));
    }

    // roll, pitch, yaw
    public Quaternion(double xAngle, double yAngle, double zAngle, boolean degrees) {
        if (degrees) {
            xAngle *= (Math.PI / 180F);
            yAngle *= (Math.PI / 180F);
            zAngle *= (Math.PI / 180F);
        }

        double sinX = Math.sin(0.5F * xAngle), cosX = Math.cos(0.5F * xAngle);
        double sinY = Math.sin(0.5F * yAngle), cosY = Math.cos(0.5F * yAngle);
        double sinZ = Math.sin(0.5F * zAngle), cosZ = Math.cos(0.5F * zAngle);

        this.x = sinX * cosY * cosZ + cosX * sinY * sinZ;
        this.y = cosX * sinY * cosZ - sinX * cosY * sinZ;
        this.z = sinX * sinY * cosZ + cosX * cosY * sinZ;
        this.w = cosX * cosY * cosZ - sinX * sinY * sinZ;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof Quaternion other && x == other.x && y == other.y && z == other.z && w == other.w;
    }

    @Override
    public int hashCode() {
        int i = Double.hashCode(x);
        i = 31 * i + Double.hashCode(y);
        i = 31 * i + Double.hashCode(z);
        i = 31 * i + Double.hashCode(w);
        return i;
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

    public double getW() {
        return w;
    }

    public Quaternion multiply(Quaternion other) {
        double prevX = getX(), prevY = getY(), prevZ = getZ(), prevW = getW();
        double otherX = other.getX(), otherY = other.getY(), otherZ = other.getZ(), otherW = other.getW();

        x = prevW * otherX + prevX * otherW + prevY * otherZ - prevZ * otherY;
        y = prevW * otherY - prevX * otherZ + prevY * otherW + prevZ * otherX;
        z = prevW * otherZ + prevX * otherY - prevY * otherX + prevZ * otherW;
        w = prevW * otherW - prevX * otherX - prevY * otherY - prevZ * otherZ;
        return this;
    }

    public Quaternion multiply(double val) {
        return set(x * val, y * val, z * val, w * val);
    }

    public Quaternion conjugate() {
        return set(-x, -y, -z, w);
    }

    public Quaternion set(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public double magnitude() {
        return getX() * getX() + getY() * getY() + getZ() * getZ() + getW() * getW();
    }

    public Quaternion normalize() {
        double mag = magnitude();
        if (mag > 1.0E-6F) {
            multiply(Mth.invSqrt(mag));
        } else {
            multiply(0);
        }
        return this;
    }

    public Quaternion copy() {
        return new Quaternion(x, y, z, w);
    }

    public Pos3D rotate(Vec3 vec) {
        return new Pos3D(vec).transform(this);
    }

    public static Pos3D rotate(Vec3 vec, Vec3 axis, double angle) {
        return new Quaternion(axis, angle, true).rotate(vec);
    }
}
