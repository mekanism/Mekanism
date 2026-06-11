package mekanism.common.tile.interfaces;

import net.minecraft.world.level.LevelReader;

public interface ITileRedstone extends IRedstoneControl {

    default boolean supportsRedstone() {
        return true;
    }

    @Override
    default boolean supportsMode(RedstoneControl mode) {
        return mode != RedstoneControl.PULSE;
    }

    default void onPowerChange(LevelReader level) {
    }
}