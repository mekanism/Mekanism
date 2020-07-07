package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.api.text.ILangEntry;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraftforge.fml.RegistryObject;

public class SoundEventRegistryObject<SOUND extends SoundEvent> extends WrappedRegistryObject<SOUND> implements ILangEntry {

    private final String translationKey;

    public SoundEventRegistryObject(RegistryObject<SOUND> registryObject) {
        super(registryObject);
        translationKey = Util.makeTranslationKey("sound_event", this.registryObject.getId());
    }

    @Nonnull
    public SOUND getSoundEvent() {
        return get();
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }
}