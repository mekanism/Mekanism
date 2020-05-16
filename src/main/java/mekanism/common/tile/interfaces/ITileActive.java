package mekanism.common.tile.interfaces;

public interface ITileActive extends IActiveState {

    default boolean isActivatable() {
        return true;
    }

    @Override
    default boolean renderUpdate() {
        return false;
    }

    @Override
    default boolean lightUpdate() {
        return false;
    }
}