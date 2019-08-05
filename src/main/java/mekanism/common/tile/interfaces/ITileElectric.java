package mekanism.common.tile.interfaces;

import mekanism.common.base.IEnergyWrapper;

public interface ITileElectric extends IEnergyWrapper {

    default double getNeededEnergy() {
        return getMaxEnergy() - getEnergy();
    }
}