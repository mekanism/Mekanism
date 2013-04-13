package universalelectricity.core.vector;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

/**
 * Vector3 Class is used for defining objects in a 3D space.
 * 
 * @author Calclavia
 */

public class Vector3 implements Cloneable
{
	public double x;
	public double y;
	public double z;

	public Vector3()
	{
		this(0, 0, 0);
	}

	public Vector3(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3(Entity par1)
	{
		this.x = par1.posX;
		this.y = par1.posY;
		this.z = par1.posZ;
	}

	public Vector3(TileEntity par1)
	{
		this.x = par1.xCoord;
		this.y = par1.yCoord;
		this.z = par1.zCoord;
	}

	public Vector3(Vec3 par1)
	{
		this.x = par1.xCoord;
		this.y = par1.yCoord;
		this.z = par1.zCoord;
	}

	public Vector3(MovingObjectPosition par1)
	{
		this.x = par1.blockX;
		this.y = par1.blockY;
		this.z = par1.blockZ;
	}

	public Vector3(ChunkCoordinates par1)
	{
		this.x = par1.posX;
		this.y = par1.posY;
		this.z = par1.posZ;
	}

	public Vector3(ForgeDirection direction)
	{
		this.x = direction.offsetX;
		this.y = direction.offsetY;
		this.z = direction.offsetZ;
	}

	/**
	 * Returns the coordinates as integers, ideal for block placement.
	 */
	public int intX()
	{
		return (int) Math.floor(this.x);
	}

	public int intY()
	{
		return (int) Math.floor(this.y);
	}

	public int intZ()
	{
		return (int) Math.floor(this.z);
	}

	/**
	 * Makes a new copy of this Vector. Prevents variable referencing problems.
	 */
	@Override
	public Vector3 clone()
	{
		return new Vector3(this.x, this.y, this.z);
	}

	/**
	 * Easy block access functions.
	 * 
	 * @param world
	 * @return
	 */
	public int getBlockID(IBlockAccess world)
	{
		return world.getBlockId(this.intX(), this.intY(), this.intZ());
	}

	public int getBlockMetadata(IBlockAccess world)
	{
		return world.getBlockMetadata(this.intX(), this.intY(), this.intZ());
	}

	public TileEntity getTileEntity(IBlockAccess world)
	{
		return world.getBlockTileEntity(this.intX(), this.intY(), this.intZ());
	}

	public boolean setBlock(World world, int id, int metadata, int notify)
	{
		return world.setBlock(this.intX(), this.intY(), this.intZ(), id, metadata, notify);
	}

	public boolean setBlock(World world, int id, int metadata)
	{
		return this.setBlock(world, id, metadata, 3);
	}

	public boolean setBlock(World world, int id)
	{
		return this.setBlock(world, id, 0);
	}

	/**
	 * Converts this Vector3 into a Vector2 by dropping the Y axis.
	 */
	public Vector2 toVector2()
	{
		return new Vector2(this.x, this.z);
	}

	/**
	 * Converts this vector three into a Minecraft Vec3 object
	 */
	public Vec3 toVec3()
	{
		return Vec3.createVectorHelper(this.x, this.y, this.z);
	}

	public double getMagnitude()
	{
		return Math.sqrt(this.getMagnitudeSquared());
	}

	public double getMagnitudeSquared()
	{
		return x * x + y * y + z * z;
	}

	public Vector3 normalize()
	{
		double d = getMagnitude();

		if (d != 0)
		{
			multiply(1 / d);
		}
		return this;
	}

	/**
	 * Gets the distance between two vectors
	 * 
	 * @return The distance
	 */
	public static double distance(Vector3 par1, Vector3 par2)
	{
		double var2 = par1.x - par2.x;
		double var4 = par1.y - par2.y;
		double var6 = par1.z - par2.z;
		return MathHelper.sqrt_double(var2 * var2 + var4 * var4 + var6 * var6);
	}

	public double distanceTo(Vector3 vector3)
	{
		double var2 = vector3.x - this.x;
		double var4 = vector3.y - this.y;
		double var6 = vector3.z - this.z;
		return MathHelper.sqrt_double(var2 * var2 + var4 * var4 + var6 * var6);
	}

	public Vector3 add(Vector3 par1)
	{
		this.x += par1.x;
		this.y += par1.y;
		this.z += par1.z;
		return this;
	}

	public Vector3 add(double par1)
	{
		this.x += par1;
		this.y += par1;
		this.z += par1;
		return this;
	}

	public Vector3 subtract(Vector3 amount)
	{
		this.x -= amount.x;
		this.y -= amount.y;
		this.z -= amount.z;
		return this;
	}

	/**
	 * Multiplies the vector by negative one.
	 */
	public Vector3 invert()
	{
		this.multiply(-1);
		return this;
	}

	public Vector3 multiply(double amount)
	{
		this.x *= amount;
		this.y *= amount;
		this.z *= amount;
		return this;
	}

	public Vector3 multiply(Vector3 vec)
	{
		this.x *= vec.x;
		this.y *= vec.y;
		this.z *= vec.z;
		return this;
	}

	public static Vector3 subtract(Vector3 par1, Vector3 par2)
	{
		return new Vector3(par1.x - par2.x, par1.y - par2.y, par1.z - par2.z);
	}

	public static Vector3 add(Vector3 par1, Vector3 par2)
	{
		return new Vector3(par1.x + par2.x, par1.y + par2.y, par1.z + par2.z);
	}

	public static Vector3 add(Vector3 par1, double par2)
	{
		return new Vector3(par1.x + par2, par1.y + par2, par1.z + par2);
	}

	public static Vector3 multiply(Vector3 vec1, Vector3 vec2)
	{
		return new Vector3(vec1.x * vec2.x, vec1.y * vec2.y, vec1.z * vec2.z);
	}

	public static Vector3 multiply(Vector3 vec1, double vec2)
	{
		return new Vector3(vec1.x * vec2, vec1.y * vec2, vec1.z * vec2);
	}

	public Vector3 round()
	{
		return new Vector3(Math.round(this.x), Math.round(this.y), Math.round(this.z));
	}

	public Vector3 floor()
	{
		return new Vector3(Math.floor(this.x), Math.floor(this.y), Math.floor(this.z));
	}

	/**
	 * Gets all entities inside of this position in block space.
	 */
	public List<Entity> getEntitiesWithin(World worldObj, Class<? extends Entity> par1Class)
	{
		return worldObj.getEntitiesWithinAABB(par1Class, AxisAlignedBB.getBoundingBox(this.intX(), this.intY(), this.intZ(), this.intX() + 1, this.intY() + 1, this.intZ() + 1));
	}

	/**
	 * Gets a position relative to a position's side
	 * 
	 * @param position - The position
	 * @param side - The side. 0-5
	 * @return The position relative to the original position's side
	 */
	public Vector3 modifyPositionFromSide(ForgeDirection side, double amount)
	{
		switch (side.ordinal())
		{
			case 0:
				this.y -= amount;
				break;
			case 1:
				this.y += amount;
				break;
			case 2:
				this.z -= amount;
				break;
			case 3:
				this.z += amount;
				break;
			case 4:
				this.x -= amount;
				break;
			case 5:
				this.x += amount;
				break;
		}
		return this;
	}

	public Vector3 modifyPositionFromSide(ForgeDirection side)
	{
		this.modifyPositionFromSide(side, 1);
		return this;
	}

	/**
	 * Loads a Vector3 from an NBT compound.
	 */
	public static Vector3 readFromNBT(NBTTagCompound nbtCompound)
	{
		Vector3 tempVector = new Vector3();
		tempVector.x = nbtCompound.getDouble("x");
		tempVector.y = nbtCompound.getDouble("y");
		tempVector.z = nbtCompound.getDouble("z");
		return tempVector;
	}

	/**
	 * Saves this Vector3 to disk
	 * 
	 * @param prefix - The prefix of this save. Use some unique string.
	 * @param par1NBTTagCompound - The NBT compound object to save the data in
	 */
	public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		par1NBTTagCompound.setDouble("x", this.x);
		par1NBTTagCompound.setDouble("y", this.y);
		par1NBTTagCompound.setDouble("z", this.z);
		return par1NBTTagCompound;
	}

	@Override
	public int hashCode()
	{
		return ("X:" + this.x + "Y:" + this.y + "Z:" + this.z).hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Vector3)
		{
			Vector3 vector3 = (Vector3) o;
			return this.x == vector3.x && this.y == vector3.y && this.z == vector3.z;
		}

		return false;
	}

	@Override
	public String toString()
	{
		return "Vector3 [" + this.x + "," + this.y + "," + this.z + "]";
	}
}