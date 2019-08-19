package mekanism.common;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class MekanismSounds {

    public static SoundEvent BEEP;
    public static SoundEvent CLICK;
    public static SoundEvent DING;
    public static SoundEvent ERROR;
    public static SoundEvent GAS_MASK;
    public static SoundEvent HYDRAULIC;
    public static SoundEvent POP;
    public static SoundEvent SUCCESS;
    public static SoundEvent CJ_EASTER_EGG;

    public static void register(IForgeRegistry<SoundEvent> registry) {
        BEEP = registerSound(registry, "etc.beep");
        CLICK = registerSound(registry, "etc.click");
        DING = registerSound(registry, "etc.ding");
        ERROR = registerSound(registry, "etc.error");
        GAS_MASK = registerSound(registry, "etc.gasmask");
        HYDRAULIC = registerSound(registry, "etc.hydraulic");
        POP = registerSound(registry, "etc.pop");
        SUCCESS = registerSound(registry, "etc.success");
        CJ_EASTER_EGG = registerSound(registry, "etc.cj");
    }

    public static SoundEvent registerSound(IForgeRegistry<SoundEvent> registry, String soundName) {
        ResourceLocation soundID = new ResourceLocation(Mekanism.MODID, soundName);
        SoundEvent event = new SoundEvent(soundID).setRegistryName(soundID);
        registry.register(event);
        return event;
    }
}