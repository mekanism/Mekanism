package mekanism.common.registration.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.ILangEntry;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@NothingNullByDefault
public class SoundEventRegistryObject<SOUND extends SoundEvent> extends WrappedRegistryObject<SoundEvent, SOUND> implements ILangEntry {

    private final String translationKey;

    public SoundEventRegistryObject(DeferredHolder<SoundEvent, SOUND> registryObject) {
        super(registryObject);
        translationKey = Util.makeDescriptionId("sound_event", this.registryObject.getId());
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }
}