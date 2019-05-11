package mekanism.api.energy;

import net.minecraft.util.EnumFacing;

/**
 * Implement this if your TileEntity can output energy.
 *
 * @author AidanBrady
 */
public interface IStrictEnergyOutputter {

    /**
     * Pulls a certain amount of energy from this outputter.
     *
     * @param amount   - amount to pull
     * @param simulate - if the operation should be simulated
     *
     * @return energy sent
     */
    double pullEnergy(EnumFacing side, double amount, boolean simulate);

    /**
     * Whether or not this tile entity can output energy on a specific side.
     *
     * @param side - side to check
     *
     * @return if the tile entity outputs energy
     */
    boolean canOutputEnergy(EnumFacing side);
}