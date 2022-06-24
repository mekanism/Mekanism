package mekanism.common.block.attribute;

import mekanism.common.base.HolidayManager;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;

public class AttributeSound implements Attribute {

    private final SoundEventRegistryObject<SoundEvent> soundRegistrar;

    public AttributeSound(SoundEventRegistryObject<SoundEvent> soundRegistrar) {
        this.soundRegistrar = soundRegistrar;
    }

    @NotNull
    public SoundEvent getSoundEvent() {
        return HolidayManager.filterSound(soundRegistrar).get();
    }
}