package mekanism.common.tile.interfaces;

import mekanism.common.base.IRedstoneControl;

public interface ITileRedstone extends IRedstoneControl {

    @Override
    default boolean canPulse() {
        return false;
    }

    default void onPowerChange() {
    }
}