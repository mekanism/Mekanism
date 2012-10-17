package railcraft.common.api.core;

/**
 * This immutable class represents a point in the Minecraft world,
 * while taking into account the possibility of coordinates in different dimensions.
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public class WorldCoordinate
{

    /**
     * The dimension
     */
    public final int dimension;
    /**
     * x-Coord
     */
    public final int x;
    /**
     * y-Coord
     */
    public final int y;
    /**
     * z-Coord
     */
    public final int z;

    /**
     * Creates a new WorldCoordinate
     * @param dimension
     * @param i
     * @param j
     * @param k
     */
    public WorldCoordinate(int dimension, int i, int j, int k)
    {
        this.dimension = dimension;
        x = i;
        y = j;
        z = k;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final WorldCoordinate other = (WorldCoordinate)obj;
        if(this.dimension != other.dimension) {
            return false;
        }
        if(this.x != other.x) {
            return false;
        }
        if(this.y != other.y) {
            return false;
        }
        if(this.z != other.z) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 13 * hash + this.dimension;
        hash = 13 * hash + this.x;
        hash = 13 * hash + this.y;
        hash = 13 * hash + this.z;
        return hash;
    }
}
