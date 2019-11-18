package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;

public class SoundEventRegistryObject<SOUND extends SoundEvent> extends WrappedRegistryObject<SOUND> {

    public SoundEventRegistryObject(RegistryObject<SOUND> registryObject) {
        super(registryObject);
    }

    @Nonnull
    public SOUND getSoundEvent() {
        return get();
    }
}