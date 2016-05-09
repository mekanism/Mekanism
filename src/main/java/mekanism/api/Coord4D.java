package mekanism.api;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.util.ReflectionUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

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
		super(x, y, z);

		dimensionId = 0;
	}
	
	public Coord4D(int x, int y, int z, World world)
	{
		this(x, y, z, world.provider.getDimensionId());
	}
	
	public Coord4D(Vec3i pos, World world)
	{
		this(pos, world.provider.getDimensionId());
	}

	public Coord4D(double x, double y, double z)
	{
		super(x, y, z);

		dimensionId = 0;
	}

	/**
	 * Creates a Coord4D from an entity's position, rounded down.
	 * @param entity - entity to create the Coord4D from
	 */
	public Coord4D(Entity entity)
	{
		super(entity);
		
		dimensionId = entity.worldObj.provider.getDimensionId();
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

	public Coord4D(double x, double y, double z, int dimension)
	{
		super(x, y, z);

		dimensionId = dimension;
	}

	public Coord4D(Vec3i pos)
	{
		this(pos, 0);
	}

	public Coord4D(Vec3i pos, int dimension)
	{
		this(pos.getX(), pos.getY(), pos.getZ());

		dimensionId = dimension;
	}


	public Coord4D(MovingObjectPosition mop)
	{
		super(mop.getBlockPos());

		dimensionId = 0;
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

		TileEntity tile = world.getTileEntity(this);
		
		return (tile != null && tile.hasWorldObj() && !tile.isInvalid()) ? tile : null;
	}
	
	public TileEntity safeTileGet(World world)
	{
		if(!exists(world))
		{
			return null;
		}
		
		TileEntity tile = world.getChunkFromBlockCoords(this).getTileEntity(this, EnumCreateEntityType.CHECK);
		
		if(tile != null)
		{
			return tile;
		}
		
		for(TileEntity iter : world.loadedTileEntityList)
		{
			if(!iter.isInvalid() && iter.getPos().equals(this))
			{
				return iter;
			}
		}
		
		List<TileEntity> added = (List<TileEntity>)ReflectionUtils.getPrivateValue(world, World.class, ObfuscatedNames.World_addedTileEntityList);
		
		for(TileEntity iter : added)
		{
			if(!iter.isInvalid() && iter.getPos().equals(this))
			{
				return iter;
			}
		}
		
		return null;
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

	public int getBlockMeta(IBlockAccess world)
	{
		IBlockState state = getBlockState(world);
		return state == null ? 0 : state.getBlock().getMetaFromState(state);
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
	public void write(ArrayList<Object> data)
	{
		data.add(toLong());
		data.add(dimensionId);
	}
	
	/**
	 * Writes this Coord4D's data to a ByteBuf for packet transfer.
	 * @param dataStream - the ByteBuf to add the data to
	 */
	public void write(ByteBuf dataStream)
	{
		dataStream.writeLong(toLong());
		dataStream.writeInt(dimensionId);
	}

	/**
	 * Translates this Coord4D by the defined x, y, and z values.
	 * @param x - x value to translate
	 * @param y - y value to translate
	 * @param z - z value to translate
	 * @return translated Coord4D
	 */
	public Coord4D add(int x, int y, int z)
	{
		return x == 0 && y == 0 && z == 0 ? this : new Coord4D(this.getX() + x, this.getY() + y, this.getZ() + z, dimensionId);
	}

	public Coord4D add(double x, double y, double z)
	{
		return x == 0.0D && y == 0.0D && z == 0.0D ? this : new Coord4D((double)this.getX() + x, (double)this.getY() + y, (double)this.getZ() + z, dimensionId);
	}

	/**
	 * Translates this Coord4D by the defined Coord4D's coordinates, regardless of dimension.
	 * @param coord - coordinates to translate by
	 * @return translated Coord4D
	 */
	public Coord4D add(Vec3i coord)
	{
		return add(coord.getX(), coord.getY(), coord.getZ());
	}

	/**
	 * Offset this BlockPos 1 block in the given direction
	 */
	public Coord4D offset(EnumFacing facing)
	{
		return this.offset(facing, 1);
	}

	/**
	 * Offsets this BlockPos n blocks in the given direction
	 */
	public Coord4D offset(EnumFacing facing, int n)
	{
		return (facing == null || n == 0) ? this : new Coord4D(getX() + facing.getFrontOffsetX() * n, getY() + facing.getFrontOffsetY() * n, getZ() + facing.getFrontOffsetZ() * n, dimensionId);
	}

	public ItemStack getStack(IBlockAccess world)
	{
		IBlockState state = getBlockState(world);
		
		if(state == null || state == Blocks.air)
		{
			return null;
		}

		return new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
	}

	/**
	 * Returns a new Coord4D from a defined TileEntity's xCoord, yCoord and zCoord values.
	 * @param tileEntity - TileEntity at the location that will represent this Coord4D
	 * @return the Coord4D object from the TileEntity
	 */
	public static Coord4D get(TileEntity tileEntity)
	{
		BlockPos pos = tileEntity.getPos();
		return new Coord4D(pos.getX(), pos.getY(), pos.getZ(), tileEntity.getWorld().provider.getDimensionId());
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
		return new Coord4D(BlockPos.fromLong(dataStream.readLong()), dataStream.readInt());
	}

	/**
	 * Creates and returns a new Coord4D with values representing the difference between the defined Coord4D
	 * @param vec - the Coord4D to subtract from this
	 * @return a Coord4D representing the distance between the defined Coord4D
	 */
	public Coord4D difference(Vec3i vec)
	{
		return vec.getX() == 0 && vec.getY() == 0 && vec.getZ() == 0 ? this : new Coord4D(getX() - vec.getX(), getY() - vec.getY(), getZ() - vec.getZ(), dimensionId);	
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

		for(EnumFacing side : EnumFacing.VALUES)
		{
			if(side.getDirectionVec().equals(diff))
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
		int subX = getX() - obj.getX();
		int subY = getY() - obj.getY();
		int subZ = getZ() - obj.getZ();
		return (int)MathHelper.sqrt_double(subX * subX + subY * subY + subZ * subZ);
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
	 * Whether or not the chunk this Coord4D is in exists and is loaded.
	 * @param world - world this Coord4D is in
	 * @return the chunk of this Coord4D
	 */
	public boolean exists(World world)
	{
		return world.getChunkProvider() == null || world.getChunkProvider().chunkExists(getX() >> 4, getZ() >> 4);
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
		return new AxisAlignedBB(this, add(1,1,1));
	}

	@Override
	public String toString()
	{
		return "[Coord4D: " + getX() + ", " + getY() + ", " + getZ() + ", dim=" + dimensionId + "]";
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Vec3i &&
				(
					((Vec3i)obj).getX() == getX() &&
					((Vec3i)obj).getY() == getY() &&
					((Vec3i)obj).getZ() == getZ()
				) && !(obj instanceof Coord4D && ((Coord4D)obj).dimensionId != dimensionId);
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