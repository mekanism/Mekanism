package mekanism.api.block;

import javax.annotation.Nonnull;
import net.minecraft.util.SoundEvent;

public interface IBlockSound {

    @Nonnull
    SoundEvent getSoundEvent();
}