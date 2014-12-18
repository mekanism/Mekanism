package mekanism.api;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import io.netty.buffer.ByteBuf;

/**
 * Coord4D - an integer-based way to keep track of and perform operations on blocks in a Minecraft-based environment. This also takes
 * in account the dimension the coordinate is in.
 * @author aidancbrady
 *
 */
public class Coord4D extends BlockPos
{
	public int dimensionId;

	/**
	 * Creates a Coord4D WITHOUT a dimensionId. Don't use unless absolutely necessary.
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 */
	public Coord4D(int x, int y, int z)
	{
		this(x, y, z, 0);
	}

	public Coord4D(Vec3i vec3i)
	{
		this(vec3i, 0);
	}

	/**
	 * Creates a Coord4D from the defined x, y, z, and dimension values.
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 * @param dimension - dimension ID
	 */
	public Coord4D(int x, int y, int z, int dimension)
	{
		super(x, y, z);

		dimensionId = dimension;
	}

	public Coord4D(Vec3i vec3i, int dimension)
	{
		super(vec3i);

		dimensionId = dimension;
	}

	/**
	 * Gets the metadata of the block representing this Coord4D.
	 * @param world - world this Coord4D is in
	 * @return the metadata of this Coord4D's block
	 */
	public IBlockState getBlockState(IBlockAccess world)
	{
		return world.getBlockState(this);
	}

	/**
	 * Gets the TileEntity of the block representing this Coord4D.
	 * @param world - world this Coord4D is in
	 * @return the TileEntity of this Coord4D's block
	 */
	public TileEntity getTileEntity(IBlockAccess world)
	{
		if(world instanceof World && !exists((World)world))
		{
			return null;
		}

		return world.getTileEntity(this);
	}

	/**
	 * Gets the Block value of the block representing this Coord4D.
	 * @param world - world this Coord4D is in
	 * @return the Block value of this Coord4D's block
	 */
	public Block getBlock(IBlockAccess world)
	{
		if(world instanceof World && !exists((World)world))
		{
			return null;
		}
		
		return world.getBlockState(this).getBlock();
	}

	/**
	 * Writes this Coord4D's data to an NBTTagCompound.
	 * @param nbtTags - tag compound to write to
	 * @return the tag compound with this Coord4D's data
	 */
	public NBTTagCompound write(NBTTagCompound nbtTags)
	{
		nbtTags.setInteger("x", getX());
		nbtTags.setInteger("y", getY());
		nbtTags.setInteger("z", getZ());
		nbtTags.setInteger("dimensionId", dimensionId);

		return nbtTags;
	}

	/**
	 * Writes this Coord4D's data to an ArrayList for packet transfer.
	 * @param data - the ArrayList to add the data to
	 */
	public void write(ArrayList data)
	{
		data.add(getX());
		data.add(getY());
		data.add(getZ());
		data.add(dimensionId);
	}
	
	/**
	 * Writes this Coord4D's data to a ByteBuf for packet transfer.
	 * @param dataStream - the ByteBuf to add the data to
	 */
	public void write(ByteBuf dataStream)
	{
		dataStream.writeInt(getX());
		dataStream.writeInt(getY());
		dataStream.writeInt(getZ());
		dataStream.writeInt(dimensionId);
	}

	/**
	 * Translates this Coord4D by the defined x, y, and z values.
	 * @param x - x value to add
	 * @param y - y value to add
	 * @param z - z value to add
	 * @return translated Coord4D
	 */
	@Override
	public Coord4D add(int x, int y, int z)
	{
		return new Coord4D(getX() + x, getY() + y, getZ() + z, dimensionId);
	}

	/**
	 * Creates and returns a new Coord4D translated to the defined offsets of the side.
	 * @param side - side to add this Coord4D to
	 * @return translated Coord4D
	 */
	@Override
	public Coord4D offset(EnumFacing side)
	{
		return offset(side, 1);
	}

	/**
	 * Creates and returns a new Coord4D translated to the defined offsets of the side by the defined amount.
	 * @param side - side to add this Coord4D to
	 * @param amount - how far to add this Coord4D
	 * @return translated Coord4D
	 */
	@Override
	public Coord4D offset(EnumFacing side, int amount)
	{
		return new Coord4D(getX()+(side.getFrontOffsetX()*amount), getY()+(side.getFrontOffsetY()*amount), getZ()+(side.getFrontOffsetZ()*amount), dimensionId);
	}
	
	public ItemStack getStack(IBlockAccess world)
	{
		Block block = getBlock(world);
		
		if(block == null || block == Blocks.air)
		{
			return null;
		}

		IBlockState state = getBlockState(world);
		
		return new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
	}

	/**
	 * Returns a new Coord4D from a defined TileEntity's xCoord, yCoord and zCoord values.
	 * @param tileEntity - TileEntity at the location that will represent this Coord4D
	 * @return the Coord4D object from the TileEntity
	 */
	public static Coord4D get(TileEntity tileEntity)
	{
		return new Coord4D(tileEntity.getPos(), tileEntity.getWorld().provider.getDimensionId());
	}

	/**
	 * Returns a new Coord4D from a tag compound.
	 * @param tag - tag compound to read from
	 * @return the Coord4D from the tag compound
	 */
    public static Coord4D read(NBTTagCompound tag)
    {
        return new Coord4D(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"), tag.getInteger("id"));
    }

	/**
	 * Returns a new Coord4D from a ByteBuf.
	 * @param dataStream - data input to read from
	 * @return the Coord4D from the data input
	 */
	public static Coord4D read(ByteBuf dataStream)
	{
		return new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
	}

	/**
	 * Creates and returns a new Coord4D with values representing the difference between the defined Coord4D
	 * @param other - the Coord4D to subtract from this
	 * @return a Coord4D representing the distance between the defined Coord4D
	 */
	public Coord4D difference(Coord4D other)
	{
		return new Coord4D(getX()-other.getX(), getY()-other.getY(), getZ()-other.getZ(), dimensionId);
	}

	/**
	 * A method used to find the EnumFacing represented by the distance of the defined Coord4D. Most likely won't have many
	 * applicable uses.
	 * @param other - Coord4D to find the side difference of
	 * @return EnumFacing representing the side the defined relative Coord4D is on to this
	 */
	public EnumFacing sideDifference(Coord4D other)
	{
		Coord4D diff = difference(other);

		for(EnumFacing side : EnumFacing.values())
		{
			if(side.getFrontOffsetX() == diff.getX() && side.getFrontOffsetY() == diff.getY() && side.getFrontOffsetZ() == diff.getZ())
			{
				return side;
			}
		}

		return null;
	}

	/**
	 * Gets the distance to a defined Coord4D.
	 * @param obj - the Coord4D to find the distance to
	 * @return the distance to the defined Coord4D
	 */
	public int distanceTo(Vec3i obj)
	{
		return (int)MathHelper.sqrt_double(distanceSq(obj));
	}

	/**
	 * Whether or not the defined side of this Coord4D is visible.
	 * @param side - side to check
	 * @param world - world this Coord4D is in
	 * @return
	 */
	public boolean sideVisible(EnumFacing side, IBlockAccess world)
	{
		return world.isAirBlock(offset(side));
	}
	
	/**
	 * Gets a TargetPoint with the defined range from this Coord4D with the appropriate coordinates and dimension ID.
	 * @param range - the range the packet can be sent in of this Coord4D
	 * @return TargetPoint relative to this Coord4D
	 */
	public TargetPoint getTargetPoint(double range)
	{
		return new TargetPoint(dimensionId, getX(), getY(), getZ(), range);
	}

	/**
	 * Steps this Coord4D in the defined side's offset without creating a new value.
	 * @param side - side to step towards
	 * @return this Coord4D
	 */
	public Coord4D step(EnumFacing side)
	{
		return add(side.getFrontOffsetX(), side.getFrontOffsetY(), side.getFrontOffsetZ());
	}

	/**
	 * Whether or not the chunk this Coord4D is in exists and is loaded.
	 * @param world - world this Coord4D is in
	 * @return the chunk of this Coord4D
	 */
	public boolean exists(World world)
	{
		return world.getChunkProvider().chunkExists(getX() >> 4, getZ() >> 4);
	}

	/**
	 * Gets the chunk this Coord4D is in.
	 * @param world - world this Coord4D is in
	 * @return the chunk of this Coord4D
	 */
	public Chunk getChunk(World world)
	{
		return world.getChunkFromBlockCoords(this);
	}
	
	/**
	 * Gets the Chunk3D object with chunk coordinates correlating to this Coord4D's location
	 * @return Chunk3D with correlating chunk coordinates.
	 */
	public Chunk3D getChunk3D()
	{
		return new Chunk3D(this);
	}

	/**
	 * Whether or not the block this Coord4D represents is an air block.
	 * @param world - world this Coord4D is in
	 * @return if this Coord4D is an air block
	 */
	public boolean isAirBlock(IBlockAccess world)
	{
		return world.isAirBlock(this);
	}
	
	/**
	 * Whether or not this block this Coord4D represents is replaceable.
	 * @param world - world this Coord4D is in
	 * @return if this Coord4D is replaceable
	 */
	public boolean isReplaceable(World world)
	{
		return getBlock(world).isReplaceable(world, this);
	}
	
	/**
	 * Gets a bounding box that contains the area this Coord4D would take up in a world.
	 * @return this Coord4D's bounding box
	 */
	public AxisAlignedBB getBoundingBox()
	{
		return AxisAlignedBB.fromBounds(getX(), getY(), getZ(), getX()+1, getY()+1, getZ()+1);
	}

	@Override
	public Coord4D clone()
	{
		return new Coord4D(this, dimensionId);
	}

	@Override
	public String toString()
	{
		return "[Coord4D: " + getX() + ", " + getY() + ", " + getZ() + ", dim=" + dimensionId + "]";
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Coord4D &&
				((Coord4D)obj).getX() == getX() &&
				((Coord4D)obj).getY() == getY() &&
				((Coord4D)obj).getZ() == getZ() &&
				((Coord4D)obj).dimensionId == dimensionId;
	}

	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * code + getX();
		code = 31 * code + getY();
		code = 31 * code + getZ();
		code = 31 * code + dimensionId;
		return code;
	}
}