package mekanism.api;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

/**
 * Coord4D - an integer-based way to keep track of and perform operations on blocks in a Minecraft-based environment.
 * This also takes in account the dimension the coordinate is in.
 *
 * @author aidancbrady
 */
public class Coord4D {

    public int x;
    public int y;
    public int z;

    public int dimensionId;

    /**
     * Creates a Coord4D from an entity's position, rounded down.
     *
     * @param entity - entity to create the Coord4D from
     */
    public Coord4D(Entity entity) {
        this.x = (int) entity.posX;
        this.y = (int) entity.posY;
        this.z = (int) entity.posZ;

        this.dimensionId = entity.world.provider.getDimension();
    }

    /**
     * Creates a Coord4D from the defined x, y, z, and dimension values.
     *
     * @param x - x coordinate
     * @param y - y coordinate
     * @param z - z coordinate
     * @param dimension - dimension ID
     */
    public Coord4D(double x, double y, double z, int dimension) {
        this.x = MathHelper.floor(x);
        this.y = MathHelper.floor(y);
        this.z = MathHelper.floor(z);

        this.dimensionId = dimension;
    }

    public Coord4D(BlockPos pos, World world) {
        this(pos.getX(), pos.getY(), pos.getZ(), world.provider.getDimension());
    }

    public Coord4D(RayTraceResult mop, World world) {
        this(mop.getBlockPos(), world);
    }

    /**
     * Returns a new Coord4D from a defined TileEntity's x, y and z values.
     *
     * @param tileEntity - TileEntity at the location that will represent this Coord4D
     * @return the Coord4D object from the TileEntity
     */
    public static Coord4D get(TileEntity tileEntity) {
        return new Coord4D(tileEntity.getPos(), tileEntity.getWorld());
    }

    /**
     * Returns a new Coord4D from a tag compound.
     *
     * @param tag - tag compound to read from
     * @return the Coord4D from the tag compound
     */
    public static Coord4D read(NBTTagCompound tag) {
        return new Coord4D(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"), tag.getInteger("id"));
    }

    /**
     * Returns a new Coord4D from a ByteBuf.
     *
     * @param dataStream - data input to read from
     * @return the Coord4D from the data input
     */
    public static Coord4D read(ByteBuf dataStream) {
        return new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
    }

    /**
     * Gets the state of the block representing this Coord4D.
     *
     * @param world - world this Coord4D is in
     * @return the state of this Coord4D's block
     */
    public IBlockState getBlockState(IBlockAccess world) {
        return world.getBlockState(getPos());
    }

    public int getBlockMeta(IBlockAccess world) {
        IBlockState state = getBlockState(world);
        return state == null ? 0 : state.getBlock().getMetaFromState(state);
    }

    public BlockPos getPos() {
        return new BlockPos(x, y, z);
    }

    /**
     * Gets the TileEntity of the block representing this Coord4D.
     *
     * @param world - world this Coord4D is in
     * @return the TileEntity of this Coord4D's block
     */
    public TileEntity getTileEntity(IBlockAccess world) {
        if (world instanceof World && !exists((World) world)) {
            return null;
        }

        return world.getTileEntity(getPos());
    }

    /**
     * Gets the Block value of the block representing this Coord4D.
     *
     * @param world - world this Coord4D is in
     * @return the Block value of this Coord4D's block
     */
    public Block getBlock(IBlockAccess world) {
        if (world instanceof World && !exists((World) world)) {
            return null;
        }

        return getBlockState(world).getBlock();
    }

    /**
     * Writes this Coord4D's data to an NBTTagCompound.
     *
     * @param nbtTags - tag compound to write to
     * @return the tag compound with this Coord4D's data
     */
    public NBTTagCompound write(NBTTagCompound nbtTags) {
        nbtTags.setInteger("x", x);
        nbtTags.setInteger("y", y);
        nbtTags.setInteger("z", z);
        nbtTags.setInteger("dimensionId", dimensionId);

        return nbtTags;
    }

    /**
     * Writes this Coord4D's data to an TileNetworkList for packet transfer.
     *
     * @param data - the TileNetworkList to add the data to
     */
    public void write(TileNetworkList data) {
        data.add(x);
        data.add(y);
        data.add(z);
        data.add(dimensionId);
    }

    @Deprecated//binary compat - use write(TileNetworkData)
    public void write(ArrayList<Object> data) {
        data.add(x);
        data.add(y);
        data.add(z);
        data.add(dimensionId);
    }

    /**
     * Writes this Coord4D's data to a ByteBuf for packet transfer.
     *
     * @param dataStream - the ByteBuf to add the data to
     */
    public void write(ByteBuf dataStream) {
        dataStream.writeInt(x);
        dataStream.writeInt(y);
        dataStream.writeInt(z);
        dataStream.writeInt(dimensionId);
    }

    /**
     * Translates this Coord4D by the defined x, y, and z values.
     *
     * @param x - x value to translate
     * @param y - y value to translate
     * @param z - z value to translate
     * @return translated Coord4D
     */
    public Coord4D translate(int x, int y, int z) {
        return new Coord4D(this.x + x, this.y + y, this.z + z, dimensionId);
    }

    /**
     * Translates this Coord4D by the defined Coord4D's coordinates, regardless of dimension.
     *
     * @param coord - coordinates to translate by
     * @return translated Coord4D
     */
    public Coord4D translate(Coord4D coord) {
        return translate(coord.x, coord.y, coord.z);
    }

    /**
     * Creates and returns a new Coord4D translated to the defined offsets of the side.
     *
     * @param side - side to translate this Coord4D to
     * @return translated Coord4D
     */
    public Coord4D offset(EnumFacing side) {
        return offset(side, 1);
    }

    /**
     * Creates and returns a new Coord4D translated to the defined offsets of the side by the defined amount.
     *
     * @param side - side to translate this Coord4D to
     * @param amount - how far to translate this Coord4D
     * @return translated Coord4D
     */
    public Coord4D offset(EnumFacing side, int amount) {
        if (side == null || amount == 0) {
            return this;
        }

        return new Coord4D(x + (side.getXOffset() * amount), y + (side.getYOffset() * amount),
              z + (side.getZOffset() * amount), dimensionId);
    }

    public ItemStack getStack(IBlockAccess world) {
        IBlockState state = getBlockState(world);

        if (state == null || state.getBlock().isAir(state, world, null)) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
    }

    /**
     * Creates and returns a new Coord4D with values representing the difference between the defined Coord4D
     *
     * @param other - the Coord4D to subtract from this
     * @return a Coord4D representing the distance between the defined Coord4D
     */
    public Coord4D difference(Coord4D other) {
        return new Coord4D(x - other.x, y - other.y, z - other.z, dimensionId);
    }

    /**
     * A method used to find the EnumFacing represented by the distance of the defined Coord4D. Most likely won't have
     * many applicable uses.
     *
     * @param other - Coord4D to find the side difference of
     * @return EnumFacing representing the side the defined relative Coord4D is on to this
     */
    public EnumFacing sideDifference(Coord4D other) {
        Coord4D diff = difference(other);

        for (EnumFacing side : EnumFacing.VALUES) {
            if (side.getXOffset() == diff.x && side.getYOffset() == diff.y && side.getZOffset() == diff.z) {
                return side;
            }
        }

        return null;
    }

    /**
     * Gets the distance to a defined Coord4D.
     *
     * @param obj - the Coord4D to find the distance to
     * @return the distance to the defined Coord4D
     */
    public int distanceTo(Coord4D obj) {
        int subX = x - obj.x;
        int subY = y - obj.y;
        int subZ = z - obj.z;
        return (int) MathHelper.sqrt(subX * subX + subY * subY + subZ * subZ);
    }

    /**
     * @param side - side to check
     * @param world - world this Coord4D is in
     * @return Whether or not the defined side of this Coord4D is visible.
     */
    public boolean sideVisible(EnumFacing side, IBlockAccess world) {
        return world.isAirBlock(step(side).getPos());
    }

    /**
     * Gets a TargetPoint with the defined range from this Coord4D with the appropriate coordinates and dimension ID.
     *
     * @param range - the range the packet can be sent in of this Coord4D
     * @return TargetPoint relative to this Coord4D
     */
    public TargetPoint getTargetPoint(double range) {
        return new TargetPoint(dimensionId, x, y, z, range);
    }

    /**
     * Steps this Coord4D in the defined side's offset without creating a new value.
     *
     * @param side - side to step towards
     * @return this Coord4D
     */
    public Coord4D step(EnumFacing side) {
        return translate(side.getXOffset(), side.getYOffset(), side.getZOffset());
    }

    /**
     * Whether or not the chunk this Coord4D is in exists and is loaded.
     *
     * @param world - world this Coord4D is in
     * @return the chunk of this Coord4D
     */
    public boolean exists(World world) {
        return world.isBlockLoaded(new BlockPos(x, y,
              z));//world.getChunkProvider() == null || world.getChunkProvider().getLoadedChunk(x >> 4, z >> 4) != null;
    }

    /**
     * Gets the chunk this Coord4D is in.
     *
     * @param world - world this Coord4D is in
     * @return the chunk of this Coord4D
     */
    public Chunk getChunk(World world) {
        return world.getChunk(getPos());
    }

    /**
     * Gets the Chunk3D object with chunk coordinates correlating to this Coord4D's location
     *
     * @return Chunk3D with correlating chunk coordinates.
     */
    public Chunk3D getChunk3D() {
        return new Chunk3D(this);
    }

    /**
     * Whether or not the block this Coord4D represents is an air block.
     *
     * @param world - world this Coord4D is in
     * @return if this Coord4D is an air block
     */
    public boolean isAirBlock(IBlockAccess world) {
        return world.isAirBlock(getPos());
    }

    /**
     * Whether or not this block this Coord4D represents is replaceable.
     *
     * @param world - world this Coord4D is in
     * @return if this Coord4D is replaceable
     */
    public boolean isReplaceable(World world) {
        return getBlock(world).isReplaceable(world, getPos());
    }

    /**
     * Gets a bounding box that contains the area this Coord4D would take up in a world.
     *
     * @return this Coord4D's bounding box
     */
    public AxisAlignedBB getBoundingBox() {
        return new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
    }

    @Override
    public Coord4D clone() {
        return new Coord4D(x, y, z, dimensionId);
    }

    @Override
    public String toString() {
        return "[Coord4D: " + x + ", " + y + ", " + z + ", dim=" + dimensionId + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Coord4D &&
              ((Coord4D) obj).x == x &&
              ((Coord4D) obj).y == y &&
              ((Coord4D) obj).z == z &&
              ((Coord4D) obj).dimensionId == dimensionId;
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + x;
        code = 31 * code + y;
        code = 31 * code + z;
        code = 31 * code + dimensionId;
        return code;
    }
}
