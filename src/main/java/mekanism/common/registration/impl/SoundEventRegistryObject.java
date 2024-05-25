package mekanism.common.registration.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;

@NothingNullByDefault
public class SoundEventRegistryObject<SOUND extends SoundEvent> extends MekanismDeferredHolder<SoundEvent, SOUND> implements IHasTranslationKey {

    private final String translationKey;

    public SoundEventRegistryObject(ResourceKey<SoundEvent> key) {
        super(key);
        translationKey = Util.makeDescriptionId("sound_event", getId());
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }
}