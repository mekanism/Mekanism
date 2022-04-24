package mekanism.additions.common.registries;

import mekanism.additions.common.MekanismAdditions;
import mekanism.common.registration.impl.SoundEventDeferredRegister;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import net.minecraft.sounds.SoundEvent;

public class AdditionsSounds {

    private AdditionsSounds() {
    }

    public static final SoundEventDeferredRegister SOUND_EVENTS = new SoundEventDeferredRegister(MekanismAdditions.MODID);

    public static final SoundEventRegistryObject<SoundEvent> POP = SOUND_EVENTS.register("entity.balloon.pop");
}