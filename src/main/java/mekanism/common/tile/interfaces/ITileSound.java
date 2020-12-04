package mekanism.common.tile.interfaces;

import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

public interface ITileSound extends ITileWrapper {

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

    default BlockPos getSoundPos() {
        return getTilePos();
    }
}