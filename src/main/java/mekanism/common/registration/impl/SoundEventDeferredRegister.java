package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class SoundEventDeferredRegister extends WrappedDeferredRegister<SoundEvent> {

    //We need to store the modid because the deferred register doesn't let you get the modid back out
    private final String modid;

    public SoundEventDeferredRegister(String modid) {
        super(modid, ForgeRegistries.SOUND_EVENTS);
        this.modid = modid;
    }

    public SoundEventRegistryObject<SoundEvent> register(String name) {
        return register(name, () -> new SoundEvent(new ResourceLocation(modid, name)), SoundEventRegistryObject::new);
    }
}