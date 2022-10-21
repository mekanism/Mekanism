package mekanism.client.sound;

import java.util.function.UnaryOperator;
import mekanism.api.text.ILangEntry;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import org.jetbrains.annotations.NotNull;

public abstract class BaseSoundProvider extends SoundDefinitionsProvider {

    private final String modid;

    protected BaseSoundProvider(DataGenerator gen, ExistingFileHelper existingFileHelper, String modid) {
        super(gen, modid, existingFileHelper);
        this.modid = modid;
    }

    @NotNull
    @Override
    public String getName() {
        return super.getName() + ": " + modid;
    }

    protected void addSoundEventWithSubtitle(SoundEventRegistryObject<?> soundEventRO, String path) {
        addSoundEventWithSubtitle(soundEventRO, path, UnaryOperator.identity());
    }

    protected void addSoundEventWithSubtitle(SoundEventRegistryObject<?> soundEventRO, String path, UnaryOperator<SoundDefinition.Sound> soundModifier) {
        addSoundEvent(soundEventRO, path, definition -> definition.subtitle(soundEventRO.getTranslationKey()), soundModifier);
    }

    protected void addSoundEvent(SoundEventRegistryObject<?> soundEventRO, String path, ILangEntry subtitle) {
        addSoundEvent(soundEventRO, path, definition -> definition.subtitle(subtitle.getTranslationKey()), UnaryOperator.identity());
    }

    protected void addSoundEvent(SoundEventRegistryObject<?> soundEventRO, String path, UnaryOperator<SoundDefinition> definitionModifier,
          UnaryOperator<SoundDefinition.Sound> soundModifier) {
        add(soundEventRO.get(), definitionModifier.apply(definition()).with(soundModifier.apply(sound(new ResourceLocation(modid, path)))));
    }
}