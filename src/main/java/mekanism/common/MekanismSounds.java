package mekanism.common;

import mekanism.common.registration.impl.SoundEventDeferredRegister;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import net.minecraft.util.SoundEvent;

public final class MekanismSounds {

    public static final SoundEventDeferredRegister SOUND_EVENTS = new SoundEventDeferredRegister(Mekanism.MODID);

    public static final SoundEventRegistryObject<SoundEvent> BEEP = SOUND_EVENTS.register("etc.beep");
    public static final SoundEventRegistryObject<SoundEvent> CLICK = SOUND_EVENTS.register("etc.click");
    public static final SoundEventRegistryObject<SoundEvent> DING = SOUND_EVENTS.register("etc.ding");
    public static final SoundEventRegistryObject<SoundEvent> ERROR = SOUND_EVENTS.register("etc.error");
    public static final SoundEventRegistryObject<SoundEvent> GAS_MASK = SOUND_EVENTS.register("etc.gasmask");
    public static final SoundEventRegistryObject<SoundEvent> HYDRAULIC = SOUND_EVENTS.register("etc.hydraulic");
    //TODO: If the pop sound is only used by the balloon in additions, it should be moved out of main and into additions
    public static final SoundEventRegistryObject<SoundEvent> POP = SOUND_EVENTS.register("etc.pop");
    public static final SoundEventRegistryObject<SoundEvent> SUCCESS = SOUND_EVENTS.register("etc.success");
    public static final SoundEventRegistryObject<SoundEvent> CJ_EASTER_EGG = SOUND_EVENTS.register("etc.cj");
}