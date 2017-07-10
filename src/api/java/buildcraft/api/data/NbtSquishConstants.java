package buildcraft.api.data;

import java.util.zip.GZIPInputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

public class NbtSquishConstants {
    /** Default written NBT Tag type- this is provided by
     * {@link CompressedStreamTools#write(NBTTagCompound, java.io.DataOutput)} and
     * {@link CompressedStreamTools#read(java.io.DataInput, net.minecraft.nbt.NBTSizeTracker)}.
     * 
     * Generally more suited to smaller NBT tags, and it writes fairly quickly. Can quickly use up a lot of space for
     * larger/more complex tags so it is recommended that you also pass it through a GZIP compressor to take up a much
     * smaller space. */
    public static final int VANILLA = 0;
    public static final int VANILLA_COMPRESSED = 1;
    /** Buildcraft provided NBT compressor - puts every tag type into a dictionary and then refers to the dictionary for
     * every tag which is written out. Can get much better space usage than {@link #VANILLA}, but at the cost of
     * time. */
    public static final int BUILDCRAFT_V1 = 2;
    public static final int BUILDCRAFT_V1_COMPRESSED = 3;

    public static final int BUILDCRAFT_MAGIC_1 = 0xbc;
    public static final int BUILDCRAFT_MAGIC_2 = 0xa1;
    /** The magic identifier for this type of file. First byte is BC, second is A1. */
    public static final int BUILDCRAFT_MAGIC = (BUILDCRAFT_MAGIC_1 << 8) | BUILDCRAFT_MAGIC_2;

    // GZIP uses the opposite byte order to us, so swap it around for us
    public static final int GZIP_MAGIC_1 = GZIPInputStream.GZIP_MAGIC & 0xff;
    public static final int GZIP_MAGIC_2 = GZIPInputStream.GZIP_MAGIC >> 8;
    public static final int GZIP_MAGIC = (GZIP_MAGIC_1 << 8) | GZIP_MAGIC_2;

    // The flags used by BUILDCRAFT_V1 to check the existence of each dictionary
    public static final int FLAG_HAS_BYTES = 1 << 0;
    public static final int FLAG_HAS_SHORTS = 1 << 1;
    public static final int FLAG_HAS_INTS = 1 << 2;
    public static final int FLAG_HAS_LONGS = 1 << 3;
    public static final int FLAG_HAS_FLOATS = 1 << 4;
    public static final int FLAG_HAS_DOUBLES = 1 << 5;
    public static final int FLAG_HAS_BYTE_ARRAYS = 1 << 6;
    public static final int FLAG_HAS_INT_ARRAYS = 1 << 7;
    public static final int FLAG_HAS_STRINGS = 1 << 8;
    public static final int FLAG_HAS_COMPLEX = 1 << 9;

    // Complex types
    public static final int COMPLEX_COMPOUND = 0;
    public static final int COMPLEX_LIST = 1;
    public static final int COMPLEX_LIST_PACKED = 2;
}
