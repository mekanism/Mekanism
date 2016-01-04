package mekanism.api;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Pos3D - a way of performing operations on objects in a three dimensional environment.
 * @author aidancbrady
 *
 */
public class Pos3D
{
	public double xPos;
	public double yPos;
	public double zPos;

	public Pos3D()
	{
		this(0, 0, 0);
	}
	
	public Pos3D(Vec3 vec)
	{
		xPos = vec.xCoord;
		yPos = vec.yCoord;
		zPos = vec.zCoord;
	}
	
	public Pos3D(MovingObjectPosition mop)
	{
		xPos = mop.blockX;
		yPos = mop.blockY;
		zPos = mop.blockZ;
	}

	public Pos3D(double x, double y, double z)
	{
		xPos = x;
		yPos = y;
		zPos = z;
	}
	
	public Pos3D(Coord4D coord)
	{
		xPos = coord.xCoord;
		yPos = coord.yCoord;
		zPos = coord.zCoord;
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
		this(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
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
		nbtTags.setDouble("x", xPos);
		nbtTags.setDouble("y", yPos);
		nbtTags.setDouble("z", zPos);

		return nbtTags;
	}

	/**
	 * Creates and returns a Pos3D with values representing the difference between this and the Pos3D in the parameters.
	 * @param pos - Pos3D to subtract
	 * @return difference of the two Pos3Ds
	 */
	public Pos3D diff(Pos3D pos)
	{
		return new Pos3D(xPos-pos.xPos, yPos-pos.yPos, zPos-pos.zPos);
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
        return new Coord4D((int)xPos, (int)yPos, (int)zPos, dimensionId);
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
		xPos += x;
		yPos += y;
		zPos += z;

		return this;
	}

	/**
	 * Performs the same operation as translate(x, y, z), but with a Pos3D value instead.
	 * @param pos - Pos3D value to translate by
	 * @return translated Pos3D
	 */
	public Pos3D translate(Pos3D pos)
	{
		return translate(pos.xPos, pos.yPos, pos.zPos);
	}

	/**
	 * Performs the same operation as translate(x, y, z), but by a set amount in a ForgeDirection
	 */
	public Pos3D translate(ForgeDirection direction, double amount)
	{
		return translate(direction.offsetX * amount, direction.offsetY * amount, direction.offsetZ * amount);
	}

	/**
	 * Performs the same operation as translate(x, y, z), but by a set amount in a ForgeDirection
	 */
	public Pos3D translateExcludingSide(ForgeDirection direction, double amount)
	{
		if(direction.offsetX == 0) xPos += amount;
		if(direction.offsetY == 0) yPos += amount;
		if(direction.offsetZ == 0) zPos += amount;

		return this;
	}

	/**
	 * Returns the distance between this and the defined Pos3D.
	 * @param pos - the Pos3D to find the distance to
	 * @return the distance between this and the defined Pos3D
	 */
	public double distance(Pos3D pos)
	{
		double subX = xPos - pos.xPos;
		double subY = yPos - pos.yPos;
		double subZ = zPos - pos.zPos;
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

		double x = xPos;
		double z = zPos;

		if(yaw != 0)
		{
			xPos = x * Math.cos(yawRadians) - z * Math.sin(yawRadians);
			zPos = z * Math.cos(yawRadians) + x * Math.sin(yawRadians);
		}

		return this;
	}
	
	public Pos3D rotatePitch(double pitch)
	{
		double pitchRadians = Math.toRadians(pitch);
		
		double y = yPos;
		double z = zPos;
		
		if(pitch != 0)
		{
			yPos = y * Math.cos(pitchRadians) - z * Math.sin(pitchRadians);
			zPos = z * Math.cos(pitchRadians) + y * Math.sin(pitchRadians);
		}
		
		return this;
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

        double x = xPos;
        double y = yPos;
        double z = zPos;

        xPos = x * Math.cos(yawRadians) * Math.cos(pitchRadians) + z * (Math.cos(yawRadians) * Math.sin(pitchRadians) * Math.sin(rollRadians) - Math.sin(yawRadians) * Math.cos(rollRadians)) + y * (Math.cos(yawRadians) * Math.sin(pitchRadians) * Math.cos(rollRadians) + Math.sin(yawRadians) * Math.sin(rollRadians));
        zPos = x * Math.sin(yawRadians) * Math.cos(pitchRadians) + z * (Math.sin(yawRadians) * Math.sin(pitchRadians) * Math.sin(rollRadians) + Math.cos(yawRadians) * Math.cos(rollRadians)) + y * (Math.sin(yawRadians) * Math.sin(pitchRadians) * Math.cos(rollRadians) - Math.cos(yawRadians) * Math.sin(rollRadians));
        yPos = -x * Math.sin(pitchRadians) + z * Math.cos(pitchRadians) * Math.sin(rollRadians) + y * Math.cos(pitchRadians) * Math.cos(rollRadians);
        
        return this;
    }
	
	public Pos3D multiply(Pos3D pos)
	{
		xPos *= pos.xPos;
		yPos *= pos.yPos;
		zPos *= pos.zPos;
		
		return this;
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
		xPos *= x;
		yPos *= y;
		zPos *= z;

		return this;
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
		
		double x = axis.xPos;
		double y = axis.yPos;
		double z = axis.zPos;
		
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
		double x = translation.xPos * matrix[0] + translation.yPos * matrix[1] + translation.zPos * matrix[2] + matrix[3];
		double y = translation.xPos * matrix[4] + translation.yPos * matrix[5] + translation.zPos * matrix[6] + matrix[7];
		double z = translation.xPos * matrix[8] + translation.yPos * matrix[9] + translation.zPos * matrix[10] + matrix[11];
		
		translation.xPos = x;
		translation.yPos = y;
		translation.zPos = z;
		
		return translation;
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
	
	public double dotProduct(Pos3D pos)
	{
		return xPos * pos.xPos + yPos * pos.yPos + zPos * pos.zPos;
	}
	
	public Pos3D crossProduct(Pos3D compare)
	{
		return clone().toCrossProduct(compare);
	}
	
	public Pos3D toCrossProduct(Pos3D compare)
	{
		double newX = yPos * compare.zPos - zPos * compare.yPos;
		double newY = zPos * compare.xPos - xPos * compare.zPos;
		double newZ = xPos * compare.yPos - yPos * compare.xPos;
		
		xPos = newX;
		yPos = newY;
		zPos = newZ;
		
		return this;
	}

	public Pos3D xCrossProduct()
	{
		return new Pos3D(0.0D, zPos, -yPos);
	}

	public Pos3D zCrossProduct()
	{
		return new Pos3D(-yPos, xPos, 0.0D);
	}
	
	public Pos3D getPerpendicular()
	{
		if(zPos == 0)
		{
			return zCrossProduct();
		}

		return xCrossProduct();
	}
	
	public Pos3D floor()
	{
		return new Pos3D(Math.floor(xPos), Math.floor(yPos), Math.floor(zPos));
	}

    public double getMagnitude()
    {
        return Math.sqrt(xPos * xPos + yPos * yPos + zPos * zPos);
    }

    public Pos3D normalize()
    {
        double d = getMagnitude();

        if (d != 0)
        {
            this.scale(1 / d);
        }

        return this;
    }

	public static AxisAlignedBB getAABB(Pos3D pos1, Pos3D pos2)
	{
		return AxisAlignedBB.getBoundingBox(
				Math.min(pos1.xPos, pos2.xPos),
				Math.min(pos1.yPos, pos2.yPos),
				Math.min(pos1.zPos, pos2.zPos),
				Math.max(pos1.xPos, pos2.xPos),
				Math.max(pos1.yPos, pos2.yPos),
				Math.max(pos1.zPos, pos2.zPos)
		);
	}

	@Override
	public Pos3D clone()
	{
		return new Pos3D(xPos, yPos, zPos);
	}

	@Override
	public String toString()
	{
		return "[Pos3D: " + xPos + ", " + yPos + ", " + zPos + "]";
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Pos3D &&
				((Pos3D)obj).xPos == xPos &&
				((Pos3D)obj).yPos == yPos &&
				((Pos3D)obj).zPos == zPos;
	}

	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * code + new Double(xPos).hashCode();
		code = 31 * code + new Double(yPos).hashCode();
		code = 31 * code + new Double(zPos).hashCode();
		return code;
	}
}
