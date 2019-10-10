package mekanism.common.tile.interfaces;

import mekanism.common.base.IEnergyWrapper;

public interface ITileElectric extends IEnergyWrapper {

    default boolean isElectric() {
        return true;
    }

    default double getNeededEnergy() {
        return getMaxEnergy() - getEnergy();
    }

    /**
     * Gets the scaled energy level for the GUI.
     *
     * @param i - multiplier
     *
     * @return scaled energy
     */
    default int getScaledEnergyLevel(int i) {
        return (int) (getEnergy() * i / getMaxEnergy());
    }
}