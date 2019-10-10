package mekanism.common.tile.interfaces;

import mekanism.common.base.IRedstoneControl;

public interface ITileRedstone extends IRedstoneControl {

    default boolean supportsRedstone() {
        return true;
    }

    @Override
    default boolean canPulse() {
        return false;
    }

    default void onPowerChange() {
    }
}