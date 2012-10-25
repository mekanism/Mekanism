package universalelectricity.core;

import net.minecraft.src.ChunkCoordinates;
import net.minecraft.src.Entity;
import net.minecraft.src.MathHelper;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.implement.IConnector;

/**
 * Vector3 Class is used for defining objects in a 3D space. Vector3 makes it
 * easier to handle the coordinates of objects. Instead of fumbling with x, y
 * and z variables, all x, y and z variables are stored in one class. Vector3.x,
 * Vector3.y, Vector3.z.
 * 
 * @author Calclavia
 */

public class Vector3 extends Vector2 implements Cloneable
{
	public double z;

	public Vector3()
	{
		this(0, 0, 0);
	}

	public Vector3(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Returns the coordinates as integers
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
	 * Converts a TileEntity's position into Vector3
	 */
	public static Vector3 get(Entity par1)
	{
		return new Vector3(par1.posX, par1.posY, par1.posZ);
	}

	/**
	 * Converts an entity's position into Vector3
	 */
	public static Vector3 get(TileEntity par1)
	{
		return new Vector3(par1.xCoord, par1.yCoord, par1.zCoord);
	}

	/**
	 * Converts from Vec3 into a Vector3
	 */
	public static Vector3 get(Vec3 par1)
	{
		return new Vector3(par1.xCoord, par1.yCoord, par1.zCoord);
	}

	/**
	 * Converts a MovingObjectPosition to Vector3
	 */
	public static Vector3 get(MovingObjectPosition par1)
	{
		return new Vector3(par1.blockX, par1.blockY, par1.blockZ);
	}

	/**
	 * Converts a MovingObjectPosition to Vector3
	 */
	public static Vector3 get(ChunkCoordinates par1)
	{
		return new Vector3(par1.posX, par1.posY, par1.posZ);
	}

	public int getBlockID(World world)
	{
		return world.getBlockId(this.intX(), this.intY(), this.intZ());
	}

	public int getBlockMetadata(World world)
	{
		return world.getBlockMetadata(this.intX(), this.intY(), this.intZ());
	}

	public TileEntity getTileEntity(World world)
	{
		return world.getBlockTileEntity(this.intX(), this.intY(), this.intZ());
	}

	public void setBlock(World world, int id, int metadata)
	{
		world.setBlockAndMetadata(this.intX(), this.intY(), this.intZ(), id, metadata);
	}

	public void setBlock(World world, int id)
	{
		world.setBlock(this.intX(), this.intY(), this.intZ(), id);
	}

	public void setBlockWithNotify(World world, int id, int metadata)
	{
		world.setBlockAndMetadataWithNotify(this.intX(), this.intY(), this.intZ(), id, metadata);
	}

	public void setBlockWithNotify(World world, int id)
	{
		world.setBlockWithNotify(this.intX(), this.intY(), this.intZ(), id);
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

	/**
	 * Checks if a Vector3 point is located inside a region
	 */
	public static boolean isPointInRegion(Vector3 point, Vector3 minPoint, Vector3 maxPoint)
	{
		return (point.x > minPoint.x && point.x < maxPoint.x) && (point.y > minPoint.y && point.y < maxPoint.y) && (point.z > minPoint.z && point.z < maxPoint.z);
	}

	/**
	 * Compares two vectors and see if they are equal. True if so.
	 */
	public boolean isEqual(Vector3 vector3)
	{
		return (this.x == vector3.x && this.y == vector3.y && this.z == vector3.z);
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

	public void add(Vector3 par1)
	{
		this.x += par1.x;
		this.y += par1.y;
		this.z += par1.z;
	}

	@Override
	public void add(double par1)
	{
		this.x += par1;
		this.y += par1;
		this.z += par1;
	}

	public static Vector3 multiply(Vector3 par1, Vector3 par2)
	{
		return new Vector3(par1.x * par2.x, par1.y * par2.y, par1.z * par2.z);
	}

	public static Vector3 multiply(Vector3 par1, double par2)
	{
		return new Vector3(par1.x * par2, par1.y * par2, par1.z * par2);
	}

	public static Vector3 readFromNBT(String prefix, NBTTagCompound par1NBTTagCompound)
	{
		Vector3 tempVector = new Vector3();
		tempVector.x = par1NBTTagCompound.getDouble(prefix + "X");
		tempVector.y = par1NBTTagCompound.getDouble(prefix + "Y");
		tempVector.z = par1NBTTagCompound.getDouble(prefix + "Z");
		return tempVector;
	}

	/**
	 * Saves this Vector3 to disk
	 * 
	 * @param prefix
	 *            - The prefix of this save. Use some unique string.
	 * @param par1NBTTagCompound
	 *            - The NBT compound object to save the data in
	 */
	public void writeToNBT(String prefix, NBTTagCompound par1NBTTagCompound)
	{
		par1NBTTagCompound.setDouble(prefix + "X", this.x);
		par1NBTTagCompound.setDouble(prefix + "Y", this.y);
		par1NBTTagCompound.setDouble(prefix + "Z", this.z);
	}

	@Override
	public Vector3 round()
	{
		return new Vector3(Math.round(this.x), Math.round(this.y), Math.round(this.z));
	}

	@Override
	public Vector3 floor()
	{
		return new Vector3(Math.floor(this.x), Math.floor(this.y), Math.floor(this.z));
	}

	@Override
	public String output()
	{
		return "Vector3: " + this.x + "," + this.y + "," + this.z;
	}

	/**
	 * Gets a position relative to another position's side
	 * 
	 * @param position
	 *            - The position
	 * @param side
	 *            - The side. 0-5
	 * @return The position relative to the original position's side
	 */
	public void modifyPositionFromSide(ForgeDirection side)
	{
		switch (side.ordinal())
		{
			case 0:
				this.y -= 1;
				break;
			case 1:
				this.y += 1;
				break;
			case 2:
				this.z -= 1;
				break;
			case 3:
				this.z += 1;
				break;
			case 4:
				this.x -= 1;
				break;
			case 5:
				this.x += 1;
				break;
		}
	}

	public static TileEntity getTileEntityFromSide(World world, Vector3 position, ForgeDirection side)
	{
		position.modifyPositionFromSide(side);
		return world.getBlockTileEntity(position.intX(), position.intY(), position.intZ());
	}

	/**
	 * Gets a connector unit based on the given side.
	 */
	public static TileEntity getConnectorFromSide(World world, Vector3 position, ForgeDirection side)
	{
		TileEntity tileEntity = getTileEntityFromSide(world, position, side);

		if (tileEntity instanceof IConnector)
		{
			if (((IConnector) tileEntity).canConnect(getOrientationFromSide(side, ForgeDirection.NORTH))) { return tileEntity; }
		}

		return null;
	}

	/**
	 * Finds the side of a block depending on it's facing direction from the
	 * given side. The side numbers are compatible with the
	 * function"getBlockTextureFromSideAndMetadata".
	 * 
	 * Bottom: 0; Top: 1; Back: 2; Front: 3; Left: 4; Right: 5;
	 * 
	 * @param front
	 *            - The direction in which this block is facing/front. Use a
	 *            number between 0 and 5. Default is 3.
	 * @param side
	 *            - The side you are trying to find. A number between 0 and 5.
	 * @return The side relative to the facing direction.
	 */

	public static ForgeDirection getOrientationFromSide(ForgeDirection front, ForgeDirection side)
	{
		switch (front.ordinal())
		{
			case 0:
				switch (side.ordinal())
				{
					case 0:
						return ForgeDirection.getOrientation(3);
					case 1:
						return ForgeDirection.getOrientation(2);
					case 2:
						return ForgeDirection.getOrientation(1);
					case 3:
						return ForgeDirection.getOrientation(0);
					case 4:
						return ForgeDirection.getOrientation(5);
					case 5:
						return ForgeDirection.getOrientation(4);
				}

			case 1:
				switch (side.ordinal())
				{
					case 0:
						return ForgeDirection.getOrientation(4);
					case 1:
						return ForgeDirection.getOrientation(5);
					case 2:
						return ForgeDirection.getOrientation(0);
					case 3:
						return ForgeDirection.getOrientation(1);
					case 4:
						return ForgeDirection.getOrientation(2);
					case 5:
						return ForgeDirection.getOrientation(3);
				}

			case 2:
				switch (side.ordinal())
				{
					case 0:
						return ForgeDirection.getOrientation(0);
					case 1:
						return ForgeDirection.getOrientation(1);
					case 2:
						return ForgeDirection.getOrientation(3);
					case 3:
						return ForgeDirection.getOrientation(2);
					case 4:
						return ForgeDirection.getOrientation(5);
					case 5:
						return ForgeDirection.getOrientation(4);
				}

			case 3:
				return side;

			case 4:
				switch (side.ordinal())
				{
					case 0:
						return ForgeDirection.getOrientation(0);
					case 1:
						return ForgeDirection.getOrientation(1);
					case 2:
						return ForgeDirection.getOrientation(5);
					case 3:
						return ForgeDirection.getOrientation(4);
					case 4:
						return ForgeDirection.getOrientation(3);
					case 5:
						return ForgeDirection.getOrientation(2);
				}

			case 5:
				switch (side.ordinal())
				{
					case 0:
						return ForgeDirection.getOrientation(0);
					case 1:
						return ForgeDirection.getOrientation(1);
					case 2:
						return ForgeDirection.getOrientation(4);
					case 3:
						return ForgeDirection.getOrientation(5);
					case 4:
						return ForgeDirection.getOrientation(2);
					case 5:
						return ForgeDirection.getOrientation(3);
				}
		}

		return ForgeDirection.UNKNOWN;
	}
}