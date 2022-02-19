package mekanism.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/**
 * Chunk3D - an extension of ChunkPos that also takes in account the dimension the chunk is in.
 */
public class Chunk3D extends ChunkPos {

    public final RegistryKey<World> dimension;

    /**
     * Creates a Chunk3D from the defined chunk x, chunk z, and dimension values.
     *
     * @param x         Chunk X coordinate
     * @param z         Chunk Z coordinate
     * @param dimension Dimension ID
     */
    public Chunk3D(int x, int z, RegistryKey<World> dimension) {
        super(x, z);
        this.dimension = dimension;
    }

    /**
     * Creates a Chunk3D based on the positional information of the given Coord4D.
     *
     * @param coord Coordinate
     */
    public Chunk3D(Coord4D coord) {
        this(coord.getX() >> 4, coord.getZ() >> 4, coord.dimension);
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
                ret.add(new Chunk3D(i, j, dimension));
            }
        }
        return ret;
    }

    @Nonnull
    @Override
    public String toString() {
        return "[Chunk3D: " + x + ", " + z + ", dim=" + dimension.location() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Chunk3D && ((Chunk3D) obj).x == x && ((Chunk3D) obj).z == z && ((Chunk3D) obj).dimension == dimension;
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