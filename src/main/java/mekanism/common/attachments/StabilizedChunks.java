package mekanism.common.attachments;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public record StabilizedChunks(byte[] chunks) {

    private static final int ARRAY_SIZE = TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER * TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER;
    public static final StabilizedChunks NONE = new StabilizedChunks(new byte[ARRAY_SIZE]);

    public static final Codec<StabilizedChunks> CODEC = Codec.BYTE.listOf(ARRAY_SIZE, ARRAY_SIZE).xmap(bytes -> {
        byte[] chunks = new byte[ARRAY_SIZE];
        for (int i = 0; i < ARRAY_SIZE; i++) {
            chunks[i] = bytes.get(i);
        }
        return new StabilizedChunks(chunks);
    }, chunks -> {
        List<Byte> list = new ArrayList<>(ARRAY_SIZE);
        for (byte chunk : chunks.chunks()) {
            list.add(chunk);
        }
        return list;
    });
    public static final StreamCodec<ByteBuf, StabilizedChunks> STREAM_CODEC = ByteBufCodecs.byteArray(ARRAY_SIZE)
          .map(StabilizedChunks::new, StabilizedChunks::chunks);

    public StabilizedChunks {
        if (chunks.length != ARRAY_SIZE) {
            throw new IllegalArgumentException("Expected to have " + ARRAY_SIZE + " chunks, but got " + chunks.length);
        }
    }

    public static StabilizedChunks create(TileEntityDimensionalStabilizer stabilizer) {
        byte[] chunksToLoad = new byte[ARRAY_SIZE];
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