package mekanism.common.tile.interfaces;

import mekanism.common.base.IEnergyWrapper;

public interface ITileElectric extends IEnergyWrapper {

    default boolean isElectric() {
        return true;
    }

    default double getNeededEnergy() {
        return getMaxEnergy() - getEnergy();
    }
}