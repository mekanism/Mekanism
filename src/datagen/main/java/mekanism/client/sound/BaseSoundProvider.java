package mekanism.client.sound;

import java.util.function.UnaryOperator;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;
import org.jetbrains.annotations.NotNull;

public abstract class BaseSoundProvider extends SoundDefinitionsProvider {

    private final String modid;

    protected BaseSoundProvider(PackOutput output, String modid) {
        super(output, modid);
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

    protected void addSoundEvent(SoundEventRegistryObject<?> soundEventRO, String path, IHasTranslationKey subtitle) {
        addSoundEvent(soundEventRO, path, definition -> definition.subtitle(subtitle.getTranslationKey()), UnaryOperator.identity());
    }

    protected void addSoundEvent(SoundEventRegistryObject<?> soundEventRO, String path, UnaryOperator<SoundDefinition> definitionModifier,
          UnaryOperator<SoundDefinition.Sound> soundModifier) {
        add(soundEventRO.get(), definitionModifier.apply(definition()).with(soundModifier.apply(sound(Identifier.fromNamespaceAndPath(modid, path)))));
    }
}