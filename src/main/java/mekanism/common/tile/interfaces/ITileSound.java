package mekanism.common.tile.interfaces;

import net.minecraft.util.SoundCategory;

public interface ITileSound {

    default boolean hasSound() {
        return true;
    }

    default float getInitialVolume() {
        return 1.0F;
    }

    default float getVolume() {
        return 1.0F;
    }

    default SoundCategory getSoundCategory() {
        return SoundCategory.BLOCKS;
    }
}