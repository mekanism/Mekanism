package mekanism.common.block.attribute;

import java.util.function.Supplier;
import mekanism.common.base.holiday.HolidayManager;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;

public class AttributeSound implements Attribute {

    private final SoundEventRegistryObject<SoundEvent> soundRegistrar;

    public AttributeSound(SoundEventRegistryObject<SoundEvent> soundRegistrar) {
        this.soundRegistrar = soundRegistrar;
    }

    @NotNull
    public Supplier<SoundEvent> getSound() {
        return HolidayManager.filterSound(soundRegistrar);
    }
}