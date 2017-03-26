package buildcraft.api.core;

import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

import io.netty.buffer.ByteBuf;

public enum EnumPipePart implements IStringSerializable, INetworkLoadable_BC8<EnumPipePart> {
    DOWN(EnumFacing.DOWN),
    UP(EnumFacing.UP),
    NORTH(EnumFacing.NORTH),
    SOUTH(EnumFacing.SOUTH),
    WEST(EnumFacing.WEST),
    EAST(EnumFacing.EAST),
    /** CENTER, UNKNOWN and ALL are all valid uses of this. */
    CENTER(null);

    public static final EnumPipePart[] VALUES = values();
    public static final EnumPipePart[] FACES = { DOWN, UP, NORTH, SOUTH, WEST, EAST };
    private static final Map<EnumFacing, EnumPipePart> facingMap = Maps.newEnumMap(EnumFacing.class);
    private static final Map<String, EnumPipePart> nameMap = Maps.newHashMap();
    private static final int MAX_VALUES = values().length;

    public final EnumFacing face;

    static {
        for (EnumPipePart part : values()) {
            nameMap.put(part.name(), part);
            if (part.face != null) facingMap.put(part.face, part);
        }
    }

    public static int ordinal(EnumFacing face) {
        return face == null ? 6 : face.ordinal();
    }

    public static EnumPipePart fromFacing(EnumFacing face) {
        if (face == null) return EnumPipePart.CENTER;
        return facingMap.get(face);
    }

    public static EnumPipePart[] validFaces() {
        return FACES;
    }

    public static EnumPipePart fromMeta(int meta) {
        if (meta < 0 || meta >= MAX_VALUES) return EnumPipePart.CENTER;
        return VALUES[meta];
    }

    private EnumPipePart(EnumFacing face) {
        this.face = face;
    }

    public int getIndex() {
        if (face == null) return 6;
        return face.getIndex();
    }

    @Override
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public EnumPipePart next() {
        switch (this) {
            case DOWN:
                return EAST;
            case EAST:
                return NORTH;
            case NORTH:
                return SOUTH;
            case SOUTH:
                return UP;
            case UP:
                return WEST;
            case WEST:
                return DOWN;
            default:
                return DOWN;
        }
    }

    public EnumPipePart opposite() {
        if (this == CENTER) return CENTER;
        return fromFacing(face.getOpposite());
    }

    public static EnumPipePart readFromNBT(NBTBase base) {
        if (base == null) return CENTER;
        if (base instanceof NBTTagString) {
            NBTTagString nbtString = (NBTTagString) base;
            String string = nbtString.getString();
            return nameMap.containsKey(string) ? nameMap.get(string) : CENTER;
        } else {
            byte ord = ((NBTPrimitive) base).getByte();
            if (ord < 0 || ord > 6) return CENTER;
            return values()[ord];
        }
    }

    public NBTBase writeToNBT() {
        return new NBTTagString(name());
    }

    @Override
    public EnumPipePart readFromByteBuf(ByteBuf buf) {
        byte ord = buf.readByte();
        return fromMeta(ord);
    }

    @Override
    public void writeToByteBuf(ByteBuf buf) {
        buf.writeByte(ordinal());
    }
}
