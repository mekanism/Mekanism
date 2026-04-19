package mekanism.common.attachments;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public record StabilizedChunks(byte[] chunks) {

    public static final StabilizedChunks NONE = new StabilizedChunks(new byte[TileEntityDimensionalStabilizer.ARRAY_SIZE]);

    public static final Codec<StabilizedChunks> CODEC = Codec.BYTE_BUFFER.xmap(buffer -> {
        if (buffer.array().length == TileEntityDimensionalStabilizer.ARRAY_SIZE) {
            return new StabilizedChunks(buffer.array());
        }
        return NONE;
    }, chunks -> ByteBuffer.wrap(chunks.chunks()));
    @Deprecated//TODO - 26.1: Do we want to just full on remove this old codec? or do we want to make thins use withAlternative?
    public static final Codec<StabilizedChunks> LEGACY_CODEC = Codec.BYTE.listOf(TileEntityDimensionalStabilizer.ARRAY_SIZE, TileEntityDimensionalStabilizer.ARRAY_SIZE).xmap(bytes -> {
        byte[] chunks = new byte[TileEntityDimensionalStabilizer.ARRAY_SIZE];
        for (int i = 0; i < TileEntityDimensionalStabilizer.ARRAY_SIZE; i++) {
            chunks[i] = bytes.get(i);
        }
        return new StabilizedChunks(chunks);
    }, chunks -> {
        List<Byte> list = new ArrayList<>(TileEntityDimensionalStabilizer.ARRAY_SIZE);
        for (byte chunk : chunks.chunks()) {
            list.add(chunk);
        }
        return list;
    });
    public static final StreamCodec<ByteBuf, StabilizedChunks> STREAM_CODEC = ByteBufCodecs.byteArray(TileEntityDimensionalStabilizer.ARRAY_SIZE)
          .map(StabilizedChunks::new, StabilizedChunks::chunks);

    public StabilizedChunks {
        if (chunks.length != TileEntityDimensionalStabilizer.ARRAY_SIZE) {
            throw new IllegalArgumentException("Expected to have " + TileEntityDimensionalStabilizer.ARRAY_SIZE + " chunks, but got " + chunks.length);
        }
    }

    public static StabilizedChunks create(TileEntityDimensionalStabilizer stabilizer) {
        byte[] chunksToLoad = new byte[TileEntityDimensionalStabilizer.ARRAY_SIZE];
        for (int x = 0; x < TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER; x++) {
            for (int z = 0; z < TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER; z++) {
                chunksToLoad[x * TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER + z] = stabilizer.isChunkLoadingAt(x, z) ? (byte) 1 : 0;
            }
        }
        return new StabilizedChunks(chunksToLoad);
    }

    public boolean loaded(int chunk) {
        return chunks[chunk] == 1;
    }

    //Note: We have to override equals and hashCode as the default implementation for records doesn't handle arrays properly
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Arrays.equals(chunks, ((StabilizedChunks) o).chunks);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(chunks);
    }
}