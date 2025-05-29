package mekanism.common.lib.math;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Pos3D - a way of performing operations on objects in a three-dimensional environment.
 *
 * @author aidancbrady
 */
public class Pos3D extends Vec3 {
    //TODO - 1.18: Go through this class, it seems a decent number of these methods are effectively the same as in Vector3D
    // Though it seems like at least our yRot method is subtly different than the Vector3D yrot method

    public Pos3D() {
        this(0, 0, 0);
    }

    public Pos3D(Vec3 vec) {
        super(vec.x, vec.y, vec.z);
    }

    public Pos3D(double x, double y, double z) {
        super(x, y, z);
    }

    /**
     * Creates a Pos3D with an entity's posX, posY, and posZ values.
     *
     * @param entity - entity to create the Pos3D from
     */
    public Pos3D(Entity entity) {
        this(entity.getX(), entity.getY(), entity.getZ());
    }

    public static Pos3D create(BlockEntity tile) {
        return create(tile.getBlockPos());
    }

    public static Pos3D create(GlobalPos pos) {
        return create(pos.pos());
    }

    public static Pos3D create(Vec3i vec) {
        return new Pos3D(atLowerCornerOf(vec));
    }

    public static Pos3D translateMatrix(double[] matrix, Pos3D translation) {
        double x = translation.x * matrix[0] + translation.y * matrix[1] + translation.z * matrix[2] + matrix[3];
        double y = translation.x * matrix[4] + translation.y * matrix[5] + translation.z * matrix[6] + matrix[7];
        double z = translation.x * matrix[8] + translation.y * matrix[9] + translation.z * matrix[10] + matrix[11];
        return new Pos3D(x, y, z);
    }

    public static double[] getRotationMatrix(float angle, Pos3D axis) {
        return axis.getRotationMatrix(angle);
    }

    public static double anglePreNorm(Pos3D pos1, Pos3D pos2) {
        return Math.acos(pos1.dot(pos2));
    }

    public static AABB getAABB(Vec3 pos1, Vec3 pos2) {
        return new AABB(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z);
    }

    /**
     * Creates and returns a Pos3D with values representing the difference between this and the Pos3D in the parameters.
     *
     * @param vec - Vec3 to subtract
     *
     * @return difference of the two Pos3Ds
     */
    public Pos3D diff(Vec3 vec) {
        return new Pos3D(x - vec.x, y - vec.y, z - vec.z);
    }

    /**
     * Creates a new GlobalPos representing this Pos3D in the provided dimension.
     *
     * @param dimension - the dimension this Pos3D is in
     *
     * @return GlobalPos representing this Pos3D
     */
    public GlobalPos getGlobalPos(ResourceKey<Level> dimension) {
        return GlobalPos.of(dimension, BlockPos.containing(x, y, z));
    }

    /**
     * Centres a block-derived Pos3D
     */
    public Pos3D centre() {
        return translate(0.5, 0.5, 0.5);
    }

    /**
     * Translates this Pos3D by the defined values.
     *
     * @param x - amount to translate on the x-axis
     * @param y - amount to translate on the y-axis
     * @param z - amount to translate on the z-axis
     *
     * @return the translated Pos3D
     */
    public Pos3D translate(double x, double y, double z) {
        return new Pos3D(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Performs the same operation as translate(x, y, z), but with a Pos3D value instead.
     *
     * @param pos - Pos3D value to translate by
     *
     * @return translated Pos3D
     */
    public Pos3D translate(Vec3 pos) {
        return translate(pos.x, pos.y, pos.z);
    }

    /**
     * Performs the same operation as translate(x, y, z), but with multiple Pos3Ds to avoid having to create intermediary objects.
     *
     * @param positions - Pos3D values to translate by
     *
     * @return translated Pos3D
     */
    public Pos3D translate(Vec3... positions) {
        double x = this.x;
        double y = this.y;
        double z = this.z;
        for (Vec3 position : positions) {
            x += position.x;
            y += position.y;
            z += position.z;
        }
        return new Pos3D(x, y, z);
    }

    /**
     * Performs the same operation as translate(x, y, z), but by a set amount in a Direction
     */
    public Pos3D translate(Direction direction, double amount) {
        return translate(direction.getNormal().getX() * amount, direction.getNormal().getY() * amount, direction.getNormal().getZ() * amount);
    }

    /**
     * Performs the same operation as translate(x, y, z), but by a set amount in a Direction
     */
    public Pos3D translateExcludingSide(Direction direction, double amount) {
        double xPos = x, yPos = y, zPos = z;
        if (direction.getAxis() != Axis.X) {
            xPos += amount;
        }
        if (direction.getAxis() != Axis.Y) {
            yPos += amount;
        }
        if (direction.getAxis() != Axis.Z) {
            zPos += amount;
        }
        return new Pos3D(xPos, yPos, zPos);
    }

    /**
     * Adjusts the position for the given direction to match that as the entity
     */
    public Pos3D adjustPosition(Direction direction, Entity entity) {
        if (direction.getAxis() == Axis.X) {
            return new Pos3D(entity.getX(), y, z);
        } else if (direction.getAxis() == Axis.Y) {
            return new Pos3D(x, entity.getY(), z);
        } //Axis.Z
        return new Pos3D(x, y, entity.getZ());
    }

    /**
     * Returns the distance between this and the defined Pos3D.
     *
     * @param pos - the Pos3D to find the distance to
     *
     * @return the distance between this and the defined Pos3D
     */
    public double distance(Vec3 pos) {
        return Mth.length(x - pos.x, y - pos.y, z - pos.z);
    }

    /**
     * Rotates this Pos3D by the defined yaw value.
     *
     * @param yaw - yaw to rotate by
     *
     * @return rotated Pos3D
     */
    @NotNull
    @Override
    public Pos3D yRot(float yaw) {
        double yawRadians = Math.toRadians(yaw);
        double xPos = x;
        double zPos = z;
        if (yaw != 0) {
            double cos = Math.cos(yawRadians);
            double sin = Math.sin(yawRadians);
            xPos = x * cos - z * sin;
            zPos = z * cos + x * sin;
        }
        return new Pos3D(xPos, y, zPos);
    }

    @NotNull
    @Override
    public Pos3D xRot(float pitch) {
        double pitchRadians = Math.toRadians(pitch);
        double yPos = y;
        double zPos = z;
        if (pitch != 0) {
            double cos = Math.cos(pitchRadians);
            double sin = Math.sin(pitchRadians);
            yPos = y * cos - z * sin;
            zPos = z * cos + y * sin;
        }
        return new Pos3D(x, yPos, zPos);
    }

    public Pos3D rotate(float yaw, float pitch) {
        return rotate(yaw, pitch, 0);
    }

    public Pos3D rotate(float yaw, float pitch, float roll) {
        double yawRadians = Math.toRadians(yaw);
        double pitchRadians = Math.toRadians(pitch);
        double rollRadians = Math.toRadians(roll);

        double xPos = x * Math.cos(yawRadians) * Math.cos(pitchRadians) + z * (
              Math.cos(yawRadians) * Math.sin(pitchRadians) * Math.sin(rollRadians) - Math.sin(yawRadians) * Math.cos(rollRadians)) +
                      y * (Math.cos(yawRadians) * Math.sin(pitchRadians) * Math.cos(rollRadians) + Math.sin(yawRadians) * Math.sin(rollRadians));
        double zPos = x * Math.sin(yawRadians) * Math.cos(pitchRadians) + z * (
              Math.sin(yawRadians) * Math.sin(pitchRadians) * Math.sin(rollRadians) + Math.cos(yawRadians) * Math.cos(rollRadians)) +
                      y * (Math.sin(yawRadians) * Math.sin(pitchRadians) * Math.cos(rollRadians) - Math.cos(yawRadians) * Math.sin(rollRadians));
        double yPos = -x * Math.sin(pitchRadians) + z * Math.cos(pitchRadians) * Math.sin(rollRadians) + y * Math.cos(pitchRadians) * Math.cos(rollRadians);
        return new Pos3D(xPos, yPos, zPos);
    }

    @NotNull
    @Override
    public Pos3D multiply(Vec3 pos) {
        return multiply(pos.x, pos.y, pos.z);
    }

    /**
     * Scales this Pos3D by the defined x, y, and z values.
     *
     * @param x - x value to scale by
     * @param y - y value to scale by
     * @param z - z value to scale by
     *
     * @return scaled Pos3D
     */
    @NotNull
    @Override
    public Pos3D multiply(double x, double y, double z) {
        return new Pos3D(this.x * x, this.y * y, this.z * z);
    }

    @NotNull
    @Override
    public Pos3D scale(double scale) {
        return multiply(scale, scale, scale);
    }

    public Pos3D rotate(float angle, Pos3D axis) {
        return translateMatrix(getRotationMatrix(angle, axis), this);
    }

    public Pos3D transform(Quaternion quaternion) {
        Quaternion q = quaternion.copy();
        q.multiply(new Quaternion(x, y, z, 0.0F));
        q.multiply(quaternion.copy().conjugate());
        return new Pos3D(q.getX(), q.getY(), q.getZ());
    }

    public double[] getRotationMatrix(float angle) {
        double[] matrix = new double[16];
        //Note: We don't need to bother cloning it here as normalize will return a new object regardless
        Vec3 axis = normalize();

        double x = axis.x;
        double y = axis.y;
        double z = axis.z;

        double angleAsRadian = Math.toRadians(angle);
        float cos = (float) Math.cos(angleAsRadian);
        float ocos = 1.0F - cos;
        float sin = (float) Math.sin(angleAsRadian);

        matrix[0] = x * x * ocos + cos;
        matrix[1] = y * x * ocos + z * sin;
        matrix[2] = x * z * ocos - y * sin;
        matrix[4] = x * y * ocos - z * sin;
        matrix[5] = y * y * ocos + cos;
        matrix[6] = y * z * ocos + x * sin;
        matrix[8] = x * z * ocos + y * sin;
        matrix[9] = y * z * ocos - x * sin;
        matrix[10] = z * z * ocos + cos;
        matrix[15] = 1.0F;
        return matrix;
    }

    public double anglePreNorm(Pos3D pos2) {
        return Math.acos(dot(pos2));
    }

    @NotNull
    @Override
    public Pos3D normalize() {
        return new Pos3D(super.normalize());
    }

    public Pos3D xCrossProduct() {
        return new Pos3D(0.0D, z, -y);
    }

    public Pos3D zCrossProduct() {
        return new Pos3D(-y, x, 0.0D);
    }

    public Pos3D getPerpendicular() {
        return z == 0 ? zCrossProduct() : xCrossProduct();
    }

    public Pos3D floor() {
        return new Pos3D(Math.floor(x), Math.floor(y), Math.floor(z));
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Pos3D clone() {
        return new Pos3D(x, y, z);
    }

    @NotNull
    @Override
    public String toString() {
        return "[Pos3D: " + x + ", " + y + ", " + z + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Vec3 other && other.x == x && other.y == y && other.z == z;
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + Double.hashCode(x);
        code = 31 * code + Double.hashCode(y);
        code = 31 * code + Double.hashCode(z);
        return code;
    }
}