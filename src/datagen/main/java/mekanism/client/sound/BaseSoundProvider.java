package mekanism.client.sound;

import javax.annotation.Nonnull;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

public abstract class BaseSoundProvider extends SoundDefinitionsProvider {

    private final String modid;

    protected BaseSoundProvider(DataGenerator gen, ExistingFileHelper existingFileHelper, String modid) {
        super(gen, modid, existingFileHelper);
        this.modid = modid;
    }

    @Nonnull
    @Override
    public String getName() {
        return super.getName() + ": " + modid;
    }

    protected void add(SoundEventRegistryObject<?> soundEventRO, SoundDefinition definition) {
        add(soundEventRO.get(), definition);
    }

    protected void addSoundEvent(SoundEventRegistryObject<?> soundEventRO, ResourceLocation location) {
        add(soundEventRO, definition().with(sound(location)));
    }

    protected void addSoundEventWithSubtitle(SoundEventRegistryObject<?> soundEventRO, ResourceLocation location, int attenuationDistance) {
        add(soundEventRO, definition(soundEventRO).with(sound(location).attenuationDistance(attenuationDistance)));
    }

    protected void addSoundEventWithSubtitle(SoundEventRegistryObject<?> soundEventRO, ResourceLocation location) {
        add(soundEventRO, definition(soundEventRO).with(sound(location)));
    }

    protected void addSoundEvent(SoundEventRegistryObject<?> soundEventRO, ResourceLocation location, ILangEntry subtitle) {
        add(soundEventRO, definition(subtitle).with(sound(location)));
    }

    protected static SoundDefinition definition(IHasTranslationKey subtitle) {
        return SoundDefinition.definition().subtitle(subtitle.getTranslationKey());
    }
}