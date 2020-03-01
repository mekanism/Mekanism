package mekanism.api.block;

import javax.annotation.Nonnull;
import net.minecraft.util.SoundEvent;

public interface IBlockSound {

    @Nonnull
    SoundEvent getSoundEvent();
    
    // TODO remove once we move from interface capabilities to type-defined capabilities
    default boolean hasSound() {
        return true;
    }
}