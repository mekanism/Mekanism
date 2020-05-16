package mekanism.common.tile.interfaces;

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