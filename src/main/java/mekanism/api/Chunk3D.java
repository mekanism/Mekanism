package mekanism.api;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * Chunk3D - an integer-based way to keep track of and perform operations on chunks in a Minecraft-based environment.
 * This also takes in account the dimension the chunk is in.
 *
 * @author aidancbrady
 */
public class Chunk3D {

    public int dimensionId;

    public int x;
    public int z;

    /**
     * Creates a Chunk3D object from the given x and z coordinates, as well as a dimension.
     *
     * @param x - chunk x location
     * @param z - chunk z location
     * @param dimension - the dimension this Chunk3D is in
     */
    public Chunk3D(int x, int z, int dimension) {
        this.x = x;
        this.z = z;

        dimensionId = dimension;
    }

    /**
     * Creates a Chunk3D from an entity based on it's location and dimension.
     *
     * @param entity - the entity to get the Chunk3D object from
     */
    public Chunk3D(Entity entity) {
        x = ((int) entity.posX) >> 4;
        z = ((int) entity.posZ) >> 4;

        dimensionId = entity.dimension;
    }

    /**
     * Creates a Chunk3D from a Coord4D based on it's coordinates and dimension.
     *
     * @param coord - the Coord4D object to get this Chunk3D from
     */
    public Chunk3D(Coord4D coord) {
        x = coord.x >> 4;
        z = coord.z >> 4;

        dimensionId = coord.dimensionId;
    }

    /**
     * Whether or not this chunk exists in the given world.
     *
     * @param world - the world to check in
     * @return if the chunk exists
     */
    public boolean exists(World world) {
        return world.getChunkProvider().getLoadedChunk(x, z) != null;
    }

    /**
     * Gets a Chunk object corresponding to this Chunk3D's coordinates.
     *
     * @param world - the world to get the Chunk object from
     * @return the corresponding Chunk object
     */
    public Chunk getChunk(World world) {
        return world.getChunk(x, z);
    }

    /**
     * Returns this Chunk3D in the Minecraft-based ChunkCoordIntPair format.
     *
     * @return this Chunk3D as a ChunkCoordIntPair
     */
    public ChunkPos getPos() {
        return new ChunkPos(x, z);
    }

    @Override
    public Chunk3D clone() {
        return new Chunk3D(x, z, dimensionId);
    }

    @Override
    public String toString() {
        return "[Chunk3D: " + x + ", " + z + ", dim=" + dimensionId + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Chunk3D &&
              ((Chunk3D) obj).x == x &&
              ((Chunk3D) obj).z == z &&
              ((Chunk3D) obj).dimensionId == dimensionId;
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + x;
        code = 31 * code + z;
        code = 31 * code + dimensionId;
        return code;
    }
}
