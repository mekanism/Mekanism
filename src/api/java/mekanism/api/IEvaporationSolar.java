package mekanism.api;

/**
 * Implement this class in a TileEntity if you wish for it to be able to heat up a Thermal Evaporation Plant.
 *
 * @author aidancbrady
 */
public interface IEvaporationSolar {

    boolean canSeeSun();
}