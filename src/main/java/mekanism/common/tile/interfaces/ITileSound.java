package mekanism.common.tile.interfaces;

import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;

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

    default SoundSource getSoundCategory() {
        return SoundSource.BLOCKS;
    }

    default BlockPos getSoundPos() {
        return getTilePos();
    }
}