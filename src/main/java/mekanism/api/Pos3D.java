package mekanism.api;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;

/**
 * Pos3D - a way of performing operations on objects in a three dimensional environment.
 * @author aidancbrady
 *
 */
public class Pos3D extends Vec3
{
	public Pos3D()
	{
		this(0, 0, 0);
	}

	public Pos3D(Vec3 vec)
	{
		super(vec.xCoord, vec.yCoord, vec.zCoord);
	}

	public Pos3D(Vec3i vec)
	{
		super(vec);
	}

	public Pos3D(MovingObjectPosition mop)
	{
		this(mop.getBlockPos());
	}

	public Pos3D(double x, double y, double z)
	{
		super(x, y, z);
	}

	/**
	 * Creates a Pos3D with an entity's posX, posY, and posZ values.
	 * @param entity - entity to create the Pos3D from
	 */
	public Pos3D(Entity entity)
	{
		this(entity.posX, entity.posY, entity.posZ);
	}

	/**
	 * Creates a Pos3D with a TileEntity's xCoord, yCoord and zCoord values.
	 * @param tileEntity - TileEntity to create the Pos3D from
	 */
	public Pos3D(TileEntity tileEntity)
	{
		this(tileEntity.getPos());
	}
	
	/**
	 * Returns a new Pos3D from a tag compound.
	 * @param tag - tag compound to read from
	 * @return the Pos3D from the tag compound
	 */
    public static Pos3D read(NBTTagCompound tag)
    {
        return new Pos3D(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
    }
    
    /**
	 * Writes this Pos3D's data to an NBTTagCompound.
	 * @param nbtTags - tag compound to write to
	 * @return the tag compound with this Pos3D's data
	 */
	public NBTTagCompound write(NBTTagCompound nbtTags)
	{
		nbtTags.setDouble("x", xCoord);
		nbtTags.setDouble("y", yCoord);
		nbtTags.setDouble("z", zCoord);

		return nbtTags;
	}

	/**
	 * Creates and returns a Pos3D with values representing the difference between this and the Pos3D in the parameters.
	 * @param vec - Vec3 to subtract
	 * @return difference of the two Pos3Ds
	 */
	public Pos3D diff(Vec3 vec)
	{
		return new Pos3D(xCoord-vec.xCoord, yCoord-vec.yCoord, zCoord-vec.zCoord);
	}

	/**
	 * Creates a new Pos3D from the motion of an entity.
	 * @param entity
	 * @return Pos3D representing the motion of the given entity
	 */
	public static Pos3D fromMotion(Entity entity)
	{
		return new Pos3D(entity.motionX, entity.motionY, entity.motionZ);
	}

    /**
     * Creates a new Coord4D representing this Pos3D in the provided dimension.
     * @param dimensionId - the dimension this Pos3D is in
     * @return Coord4D representing this Pos3D
     */
    public Coord4D getCoord(int dimensionId)
    {
        return new Coord4D((int)xCoord, (int)yCoord, (int)zCoord, dimensionId);
    }

	/**
	 * Centres a block-derived Pos3D
	 */
	public Pos3D centre()
	{
		return translate(0.5, 0.5, 0.5);
	}

	/**
	 * Translates this Pos3D by the defined values.
	 * @param x - amount to translate on the x axis
	 * @param y - amount to translate on the y axis
	 * @param z - amount to translate on the z axis
	 * @return the translated Pos3D
	 */
	public Pos3D translate(double x, double y, double z)
	{
		return new Pos3D(xCoord + x, yCoord + y, zCoord + z);
	}

	/**
	 * Performs the same operation as translate(x, y, z), but with a Pos3D value instead.
	 * @param pos - Pos3D value to translate by
	 * @return translated Pos3D
	 */
	public Pos3D translate(Vec3 pos)
	{
		return translate(pos.xCoord, pos.yCoord, pos.zCoord);
	}

	/**
	 * Performs the same operation as translate(x, y, z), but by a set amount in a ForgeDirection
	 */
	public Pos3D translate(EnumFacing direction, double amount)
	{
		return translate(direction.getDirectionVec().getX() * amount, direction.getDirectionVec().getY() * amount, direction.getDirectionVec().getZ() * amount);
	}

	/**
	 * Performs the same operation as translate(x, y, z), but by a set amount in a ForgeDirection
	 */
	public Pos3D translateExcludingSide(EnumFacing direction, double amount)
	{
		double xPos = xCoord, yPos = yCoord, zPos = zCoord;
		if(direction.getAxis() != Axis.X) xPos += amount;
		if(direction.getAxis() != Axis.Y) yPos += amount;
		if(direction.getAxis() != Axis.Z) zPos += amount;

		return new Pos3D(xPos, yPos, zPos);
	}

	/**
	 * Returns the distance between this and the defined Pos3D.
	 * @param pos - the Pos3D to find the distance to
	 * @return the distance between this and the defined Pos3D
	 */
	public double distance(Vec3 pos)
	{
		double subX = xCoord - pos.xCoord;
		double subY = yCoord - pos.yCoord;
		double subZ = zCoord - pos.zCoord;
		return MathHelper.sqrt_double(subX * subX + subY * subY + subZ * subZ);
	}

	/**
	 * Rotates this Pos3D by the defined yaw value.
	 * @param yaw - yaw to rotate by
	 * @return rotated Pos3D
	 */
	public Pos3D rotateYaw(double yaw)
	{
		double yawRadians = Math.toRadians(yaw);

		double xPos = xCoord;
		double zPos = zCoord;

		if(yaw != 0)
		{
			xPos = xCoord * Math.cos(yawRadians) - zCoord * Math.sin(yawRadians);
			zPos = zCoord * Math.cos(yawRadians) + xCoord * Math.sin(yawRadians);
		}

		return new Pos3D(xPos, yCoord, zPos);
	}
	
	public Pos3D rotatePitch(double pitch)
	{
		double pitchRadians = Math.toRadians(pitch);
		
		double yPos = yCoord;
		double zPos = zCoord;
		
		if(pitch != 0)
		{
			yPos = yCoord * Math.cos(pitchRadians) - zCoord * Math.sin(pitchRadians);
			zPos = zCoord * Math.cos(pitchRadians) + yCoord * Math.sin(pitchRadians);
		}
		
		return new Pos3D(xCoord, yPos, zPos);
	}
	
	public Pos3D rotate(double yaw, double pitch)
	{
		return rotate(yaw, pitch, 0);
	}

    public Pos3D rotate(double yaw, double pitch, double roll)
    {
        double yawRadians = Math.toRadians(yaw);
        double pitchRadians = Math.toRadians(pitch);
        double rollRadians = Math.toRadians(roll);

        double xPos = xCoord;
        double yPos = yCoord;
        double zPos = zCoord;

        xPos = xCoord * Math.cos(yawRadians) * Math.cos(pitchRadians) + zCoord * (Math.cos(yawRadians) * Math.sin(pitchRadians) * Math.sin(rollRadians) - Math.sin(yawRadians) * Math.cos(rollRadians)) + yCoord * (Math.cos(yawRadians) * Math.sin(pitchRadians) * Math.cos(rollRadians) + Math.sin(yawRadians) * Math.sin(rollRadians));
        zPos = xCoord * Math.sin(yawRadians) * Math.cos(pitchRadians) + zCoord * (Math.sin(yawRadians) * Math.sin(pitchRadians) * Math.sin(rollRadians) + Math.cos(yawRadians) * Math.cos(rollRadians)) + yCoord * (Math.sin(yawRadians) * Math.sin(pitchRadians) * Math.cos(rollRadians) - Math.cos(yawRadians) * Math.sin(rollRadians));
        yPos = -xCoord * Math.sin(pitchRadians) + zCoord * Math.cos(pitchRadians) * Math.sin(rollRadians) + yCoord * Math.cos(pitchRadians) * Math.cos(rollRadians);
        
        return new Pos3D(xPos, yPos, zPos);
    }
	
	public Pos3D multiply(Vec3 pos)
	{
		return scale(pos.xCoord, pos.yCoord, pos.zCoord);
	}

	/**
	 * Scales this Pos3D by the defined x, y, an z values.
	 * @param x - x value to scale by
	 * @param y - y value to scale by
	 * @param z - z value to scale by
	 * @return scaled Pos3D
	 */
	public Pos3D scale(double x, double y, double z)
	{
		return new Pos3D(xCoord * x, yCoord * y, zCoord * z);
	}

	/**
	 * Performs the same operation as scale(x, y, z), but with a value representing all three dimensions.
	 * @param scale - value to scale by
	 * @return scaled Pos3D
	 */
	public Pos3D scale(double scale)
	{
		return scale(scale, scale, scale);
	}
	
	public Pos3D rotate(float angle, Pos3D axis)
	{
		return translateMatrix(getRotationMatrix(angle, axis), this);
	}

	public double[] getRotationMatrix(float angle)
	{
		double[] matrix = new double[16];
		Pos3D axis = clone().normalize();
		
		double x = axis.xCoord;
		double y = axis.yCoord;
		double z = axis.zCoord;
		
		angle *= 0.0174532925D;
		
		float cos = (float)Math.cos(angle);
		float ocos = 1.0F - cos;
		float sin = (float)Math.sin(angle);
		
		matrix[0] = (x * x * ocos + cos);
		matrix[1] = (y * x * ocos + z * sin);
		matrix[2] = (x * z * ocos - y * sin);
		matrix[4] = (x * y * ocos - z * sin);
		matrix[5] = (y * y * ocos + cos);
		matrix[6] = (y * z * ocos + x * sin);
		matrix[8] = (x * z * ocos + y * sin);
		matrix[9] = (y * z * ocos - x * sin);
		matrix[10] = (z * z * ocos + cos);
		matrix[15] = 1.0F;
		
		return matrix;
	}

	public static Pos3D translateMatrix(double[] matrix, Pos3D translation)
	{
		double x = translation.xCoord * matrix[0] + translation.yCoord * matrix[1] + translation.zCoord * matrix[2] + matrix[3];
		double y = translation.xCoord * matrix[4] + translation.yCoord * matrix[5] + translation.zCoord * matrix[6] + matrix[7];
		double z = translation.xCoord * matrix[8] + translation.yCoord * matrix[9] + translation.zCoord * matrix[10] + matrix[11];
		
		return new Pos3D(x, y, z);
	}

	public static double[] getRotationMatrix(float angle, Pos3D axis)
	{
		return axis.getRotationMatrix(angle);
	}
	
	public double anglePreNorm(Pos3D pos2)
	{
		return Math.acos(dotProduct(pos2));
	}

	public static double anglePreNorm(Pos3D pos1, Pos3D pos2)
	{
		return Math.acos(pos1.clone().dotProduct(pos2));
	}

	public Pos3D normalize() {
		return new Pos3D(super.normalize());
	}
	
	public Pos3D xCrossProduct()
	{
		return new Pos3D(0.0D, zCoord, -yCoord);
	}

	public Pos3D zCrossProduct()
	{
		return new Pos3D(-yCoord, xCoord, 0.0D);
	}
	
	public Pos3D getPerpendicular()
	{
		if(zCoord == 0)
		{
			return zCrossProduct();
		}

		return xCrossProduct();
	}
	
	public Pos3D floor()
	{
		return new Pos3D(Math.floor(xCoord), Math.floor(yCoord), Math.floor(zCoord));
	}

	public static AxisAlignedBB getAABB(Pos3D pos1, Pos3D pos2)
	{
		return new AxisAlignedBB(
			pos1.xCoord,
			pos1.yCoord,
			pos1.zCoord,
			pos2.xCoord,
			pos2.yCoord,
			pos2.zCoord
		);
	}

	@Override
	public Pos3D clone()
	{
		return new Pos3D(xCoord, yCoord, zCoord);
	}

	@Override
	public String toString()
	{
		return "[Pos3D: " + xCoord + ", " + yCoord + ", " + zCoord + "]";
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Vec3 &&
				((Vec3)obj).xCoord == xCoord &&
				((Vec3)obj).xCoord == yCoord &&
				((Vec3)obj).xCoord == zCoord;
	}

	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * code + new Double(xCoord).hashCode();
		code = 31 * code + new Double(yCoord).hashCode();
		code = 31 * code + new Double(zCoord).hashCode();
		return code;
	}
}
