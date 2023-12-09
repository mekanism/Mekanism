package mekanism.common.tile.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;

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
        return getBlockPos();
    }
}