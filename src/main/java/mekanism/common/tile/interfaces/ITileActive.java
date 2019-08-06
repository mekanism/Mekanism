package mekanism.common.tile.interfaces;

import mekanism.common.base.IActiveState;

public interface ITileActive extends IActiveState {

    @Override
    default boolean renderUpdate() {
        return false;
    }

    @Override
    default boolean lightUpdate() {
        return false;
    }
}