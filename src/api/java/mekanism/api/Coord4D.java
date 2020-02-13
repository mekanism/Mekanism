package mekanism.api;

import net.minecraft.entity.Entity;
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
import net.minecraft.world.dimension.DimensionType;

/**
 * Coord4D - an integer-based way to keep track of and perform operations on blocks in a Minecraft-based environment. This also takes in account the dimension the
 * coordinate is in.
 *
 * @author aidancbrady
 */
public class Coord4D {//TODO: Replace this with GlobalPos

    /**
     * Cached value of {@link Direction#values()}. DO NOT MODIFY THIS LIST.
     */
    private static final Direction[] DIRECTIONS = Direction.values();

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
        BlockPos entityPosition = entity.getPosition();
        this.x = entityPosition.getX();
        this.y = entityPosition.getY();
        this.z = entityPosition.getX();
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
     * @param tile - TileEntity at the location that will represent this Coord4D
     *
     * @return the Coord4D object from the TileEntity
     */
    public static Coord4D get(TileEntity tile) {
        return new Coord4D(tile.getPos(), tile.getWorld());
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

    public BlockPos getPos() {
        return new BlockPos(x, y, z);
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

    /**
     * A method used to find the Direction represented by the distance of the defined Coord4D. Most likely won't have many applicable uses.
     *
     * @param other - Coord4D to find the side difference of
     *
     * @return Direction representing the side the defined relative Coord4D is on to this
     */
    public Direction sideDifference(Coord4D other) {
        Coord4D diff = new Coord4D(x - other.x, y - other.y, z - other.z, dimension);
        for (Direction side : DIRECTIONS) {
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
    public double distanceTo(Coord4D obj) {
        int subX = x - obj.x;
        int subY = y - obj.y;
        int subZ = z - obj.z;
        return MathHelper.sqrt(subX * subX + subY * subY + subZ * subZ);
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