package mekanism.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Chunk3D - an extension of ChunkPos that also takes in account the dimension the chunk is in.
 */
public class Chunk3D extends ChunkPos {

    public final ResourceKey<Level> dimension;

    /**
     * Creates a Chunk3D from the defined chunk x, chunk z, and dimension values.
     *
     * @param dimension Dimension ID
     * @param x         Chunk X coordinate
     * @param z         Chunk Z coordinate
     */
    public Chunk3D(ResourceKey<Level> dimension, int x, int z) {
        super(x, z);
        this.dimension = dimension;
    }

    /**
     * Creates a Chunk3D from the defined chunk position, and dimension values.
     *
     * @param dimension Dimension ID
     * @param chunkPos  Long representation of the chunk position
     *
     * @since 10.3.2
     */
    public Chunk3D(ResourceKey<Level> dimension, long chunkPos) {
        this(dimension, getX(chunkPos), getZ(chunkPos));
    }

    /**
     * Creates a Chunk3D from the defined chunk position, and dimension values.
     *
     * @param dimension Dimension ID
     * @param chunkPos  Chunk position
     */
    public Chunk3D(ResourceKey<Level> dimension, ChunkPos chunkPos) {
        this(dimension, chunkPos.x, chunkPos.z);
    }

    /**
     * Creates a Chunk3D based on the positional information of the given global position.
     *
     * @param pos Position
     */
    public Chunk3D(GlobalPos pos) {
        this(pos.dimension(), SectionPos.blockToSectionCoord(pos.pos().getX()), SectionPos.blockToSectionCoord(pos.pos().getZ()));
    }

    /**
     * Calculates the set of chunks in a given radius around this chunk.
     *
     * @param chunkRadius Radius in chunks.
     *
     * @return Set of chunks in the given radius centered on this chunk.
     */
    public Set<Chunk3D> expand(int chunkRadius) {
        if (chunkRadius < 0) {
            throw new IllegalArgumentException("Chunk radius cannot be negative.");
        } else if (chunkRadius == 1) {
            return Collections.singleton(this);
        }
        Set<Chunk3D> ret = new HashSet<>();
        for (int i = x - chunkRadius; i <= x + chunkRadius; i++) {
            for (int j = z - chunkRadius; j <= z + chunkRadius; j++) {
                ret.add(new Chunk3D(dimension, i, j));
            }
        }
        return ret;
    }

    @NotNull
    @Override
    public String toString() {
        return "[Chunk3D: " + x + ", " + z + ", dim=" + dimension.location() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Chunk3D other && other.x == x && other.z == z && other.dimension == dimension;
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + x;
        code = 31 * code + z;
        code = 31 * code + dimension.hashCode();
        return code;
    }
}