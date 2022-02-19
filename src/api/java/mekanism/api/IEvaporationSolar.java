package mekanism.api;

/**
 * Expose this as a capability on your TileEntity to allow for the tile to be able to heat up a Thermal Evaporation Plant.
 *
 * @author aidancbrady
 */
public interface IEvaporationSolar {

    /**
     * Checks if this tile is able to see the sun.
     *
     * @return {@code true} if the solar can see the sun.
     */
    boolean canSeeSun();
}