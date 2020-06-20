package mekanism.client.sound;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.text.ILangEntry;
import mekanism.common.DataGenJsonConstants;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SoundEventBuilder {

    public static SoundEventBuilder create(SoundEventRegistryObject<?> soundEventRO) {
        return new SoundEventBuilder(soundEventRO);
    }

    private final String path;
    private boolean replace;
    @Nullable
    private String translationKey;
    private final Map<ResourceLocation, SoundBuilder> soundBuilders = new HashMap<>();

    private SoundEventBuilder(SoundEventRegistryObject<?> soundEventRO) {
        path = soundEventRO.getSoundEvent().getRegistryName().getPath();
    }

    public String getPath() {
        return path;
    }

    /**
     * Only used in resource packs, but included here for completeness. If the sounds listed in sounds should replace the sounds listed in the default sounds.json for
     * this sound event.
     */
    public SoundEventBuilder replace() {
        this.replace = true;
        return this;
    }

    /**
     * Will be translated as the subtitle of the sound if Show Subtitles is enabled in game.
     *
     * @apiNote Optional
     */
    public SoundEventBuilder subtitle(ILangEntry langEntry) {
        this.translationKey = Objects.requireNonNull(langEntry).getTranslationKey();
        return this;
    }

    /**
     * Helper method for {@link #addSounds(SoundBuilder...)}, for when all our sound options are the default
     */
    public SoundEventBuilder addSounds(Function<ResourceLocation, SoundBuilder> builderFunction, ResourceLocation... locations) {
        for (ResourceLocation location : locations) {
            addSounds(builderFunction.apply(location));
        }
        return this;
    }

    public SoundEventBuilder addSounds(SoundBuilder... soundBuilders) {
        for (SoundBuilder soundBuilder : soundBuilders) {
            ResourceLocation location = soundBuilder.getLocation();
            if (this.soundBuilders.containsKey(location)) {
                throw new RuntimeException("Sound '" + location + "' has already been added to this sound event (" + getPath() + "). Increase the weight on the sound instead.");
            }
            this.soundBuilders.put(location, soundBuilder);
        }
        return this;
    }

    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        if (replace) {
            jsonObject.addProperty(DataGenJsonConstants.REPLACE, true);
        }
        if (translationKey != null) {
            jsonObject.addProperty(DataGenJsonConstants.SUBTITLE, translationKey);
        }
        if (!soundBuilders.isEmpty()) {
            JsonArray sounds = new JsonArray();
            for (SoundBuilder soundBuilder : soundBuilders.values()) {
                sounds.add(soundBuilder.toJson());
            }
            jsonObject.add(DataGenJsonConstants.SOUNDS, sounds);
        }
        return jsonObject;
    }
}