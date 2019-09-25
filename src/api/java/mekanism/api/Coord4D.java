package mekanism.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;

/**
 * Coord4D - an integer-based way to keep track of and perform operations on blocks in a Minecraft-based environment. This also takes in account the dimension the
 * coordinate is in.
 *
 * @author aidancbrady
 */
public class Coord4D {

    public int x;
    public int y;
    public int z;
    public DimensionType dimension;

    /**
     * Creates a Coord4D from an entity's position, rounded down.
     *
     * @param entity - entity to create the Coord4D from
     */
    public Coord4D(Entity entity) {
        this.x = (int) entity.posX;
        this.y = (int) entity.posY;
        this.z = (int) entity.posZ;
        this.dimension = entity.world.getDimension().getType();
    }

    /**
     * Creates a Coord4D from the defined x, y, z, and dimension values.
     *
     * @param x         - x coordinate
     * @param y         - y coordinate
     * @param z         - z coordinate
     * @param dimension - dimension ID
     */
    public Coord4D(double x, double y, double z, DimensionType dimension) {
        this.x = MathHelper.floor(x);
        this.y = MathHelper.floor(y);
        this.z = MathHelper.floor(z);
        this.dimension = dimension;
    }

    public Coord4D(BlockPos pos, IWorldReader world) {
        this(pos.getX(), pos.getY(), pos.getZ(), world.getDimension().getType());
    }

    public Coord4D(BlockRayTraceResult mop, IWorldReader world) {
        this(mop.getPos(), world);
    }

    /**
     * Returns a new Coord4D from a defined TileEntity's x, y and z values.
     *
     * @param tileEntity - TileEntity at the location that will represent this Coord4D
     *
     * @return the Coord4D object from the TileEntity
     */
    public static Coord4D get(TileEntity tileEntity) {
        return new Coord4D(tileEntity.getPos(), tileEntity.getWorld());
    }

    /**
     * Returns a new Coord4D from a tag compound.
     *
     * @param tag - tag compound to read from
     *
     * @return the Coord4D from the tag compound
     */
    public static Coord4D read(CompoundNBT tag) {
        //TODO: Store the id as something that doesn't change?
        return new Coord4D(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"), DimensionType.byName(new ResourceLocation(tag.getString("dimension"))));
    }

    /**
     * Returns a new Coord4D from a PacketBuffer.
     *
     * @param dataStream - data input to read from
     *
     * @return the Coord4D from the data input
     */
    public static Coord4D read(PacketBuffer dataStream) {
        return new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), DimensionType.byName(dataStream.readResourceLocation()));
    }

    /**
     * Gets the state of the block representing this Coord4D.
     *
     * @param world - world this Coord4D is in
     *
     * @return the state of this Coord4D's block
     */
    public BlockState getBlockState(IWorldReader world) {
        return world.getBlockState(getPos());
    }

    public BlockPos getPos() {
        return new BlockPos(x, y, z);
    }

    /**
     * Gets the TileEntity of the block representing this Coord4D.
     *
     * @param world - world this Coord4D is in
     *
     * @return the TileEntity of this Coord4D's block
     */
    public TileEntity getTileEntity(IWorldReader world) {
        if (world == null || world instanceof World && !exists(world)) {
            return null;
        }
        return world.getTileEntity(getPos());
    }

    /**
     * Gets the Block value of the block representing this Coord4D.
     *
     * @param world - world this Coord4D is in
     *
     * @return the Block value of this Coord4D's block
     */
    public Block getBlock(IWorldReader world) {
        if (world instanceof World && !exists(world)) {
            return null;
        }
        return getBlockState(world).getBlock();
    }

    /**
     * Writes this Coord4D's data to an CompoundNBT.
     *
     * @param nbtTags - tag compound to write to
     *
     * @return the tag compound with this Coord4D's data
     */
    public CompoundNBT write(CompoundNBT nbtTags) {
        nbtTags.putInt("x", x);
        nbtTags.putInt("y", y);
        nbtTags.putInt("z", z);
        nbtTags.putString("dimension", dimension.getRegistryName().toString());
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
        data.add(dimension.getRegistryName());
    }

    /**
     * Writes this Coord4D's data to a PacketBuffer for packet transfer.
     *
     * @param dataStream - the PacketBuffer to add the data to
     */
    public void write(PacketBuffer dataStream) {
        dataStream.writeInt(x);
        dataStream.writeInt(y);
        dataStream.writeInt(z);
        dataStream.writeResourceLocation(dimension.getRegistryName());
    }

    /**
     * Translates this Coord4D by the defined x, y, and z values.
     *
     * @param x - x value to translate
     * @param y - y value to translate
     * @param z - z value to translate
     *
     * @return translated Coord4D
     */
    public Coord4D translate(int x, int y, int z) {
        return new Coord4D(this.x + x, this.y + y, this.z + z, dimension);
    }

    /**
     * Translates this Coord4D by the defined Coord4D's coordinates, regardless of dimension.
     *
     * @param coord - coordinates to translate by
     *
     * @return translated Coord4D
     */
    public Coord4D translate(Coord4D coord) {
        return translate(coord.x, coord.y, coord.z);
    }

    /**
     * Creates and returns a new Coord4D translated to the defined offsets of the side.
     *
     * @param side - side to translate this Coord4D to
     *
     * @return translated Coord4D
     */
    public Coord4D offset(Direction side) {
        return offset(side, 1);
    }

    /**
     * Creates and returns a new Coord4D translated to the defined offsets of the side by the defined amount.
     *
     * @param side   - side to translate this Coord4D to
     * @param amount - how far to translate this Coord4D
     *
     * @return translated Coord4D
     */
    public Coord4D offset(Direction side, int amount) {
        if (side == null || amount == 0) {
            return this;
        }
        return new Coord4D(x + (side.getXOffset() * amount), y + (side.getYOffset() * amount), z + (side.getZOffset() * amount), dimension);
    }

    public ItemStack getStack(IWorldReader world) {
        BlockState state = getBlockState(world);
        if (state == null || state.getBlock().isAir(state, world, null)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(state.getBlock());
    }

    /**
     * Creates and returns a new Coord4D with values representing the difference between the defined Coord4D
     *
     * @param other - the Coord4D to subtract from this
     *
     * @return a Coord4D representing the distance between the defined Coord4D
     */
    public Coord4D difference(Coord4D other) {
        return new Coord4D(x - other.x, y - other.y, z - other.z, dimension);
    }

    /**
     * A method used to find the Direction represented by the distance of the defined Coord4D. Most likely won't have many applicable uses.
     *
     * @param other - Coord4D to find the side difference of
     *
     * @return Direction representing the side the defined relative Coord4D is on to this
     */
    public Direction sideDifference(Coord4D other) {
        Coord4D diff = difference(other);
        for (Direction side : Direction.values()) {
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
     *
     * @return the distance to the defined Coord4D
     */
    public int distanceTo(Coord4D obj) {
        int subX = x - obj.x;
        int subY = y - obj.y;
        int subZ = z - obj.z;
        return (int) MathHelper.sqrt(subX * subX + subY * subY + subZ * subZ);
    }

    /**
     * @param side  - side to check
     * @param world - world this Coord4D is in
     *
     * @return Whether or not the defined side of this Coord4D is visible.
     */
    public boolean sideVisible(Direction side, IWorldReader world) {
        return world.isAirBlock(step(side).getPos());
    }

    /**
     * Gets a TargetPoint with the defined range from this Coord4D with the appropriate coordinates and dimension ID.
     *
     * @param range - the range the packet can be sent in of this Coord4D
     *
     * @return TargetPoint relative to this Coord4D
     */
    public TargetPoint getTargetPoint(double range) {
        return new TargetPoint(x, y, z, range, dimension);
    }

    /**
     * Steps this Coord4D in the defined side's offset without creating a new value.
     *
     * @param side - side to step towards
     *
     * @return this Coord4D
     */
    public Coord4D step(Direction side) {
        return translate(side.getXOffset(), side.getYOffset(), side.getZOffset());
    }

    /**
     * Whether or not the chunk this Coord4D is in exists and is loaded.
     *
     * @param world - world this Coord4D is in
     *
     * @return the chunk of this Coord4D
     */
    public boolean exists(IWorldReader world) {
        return world.isAreaLoaded(new BlockPos(x, y, z), 0);//world.getChunkProvider() == null || world.getChunkProvider().getLoadedChunk(x >> 4, z >> 4) != null;
    }

    /**
     * Gets the chunk this Coord4D is in.
     *
     * @param world - world this Coord4D is in
     *
     * @return the chunk of this Coord4D
     */
    public IChunk getChunk(IWorldReader world) {
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
     *
     * @return if this Coord4D is an air block
     */
    public boolean isAirBlock(IWorldReader world) {
        return world.isAirBlock(getPos());
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
        return new Coord4D(x, y, z, dimension);
    }

    @Override
    public String toString() {
        return "[Coord4D: " + x + ", " + y + ", " + z + ", dim=" + dimension + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Coord4D && ((Coord4D) obj).x == x && ((Coord4D) obj).y == y && ((Coord4D) obj).z == z && ((Coord4D) obj).dimension == dimension;
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + x;
        code = 31 * code + y;
        code = 31 * code + z;
        code = 31 * code + dimension.hashCode();
        return code;
    }
}