package mekanism.client.sound;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import mekanism.api.text.ILangEntry;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

public abstract class BaseSoundProvider implements IDataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, SoundEventBuilder> soundEventBuilders = new LinkedHashMap<>();
    private final ExistingFileHelper existingFileHelper;
    private final DataGenerator gen;
    private final String modid;

    protected BaseSoundProvider(DataGenerator gen, ExistingFileHelper existingFileHelper, String modid) {
        this.gen = gen;
        this.modid = modid;
        this.existingFileHelper = existingFileHelper;
    }

    @Nonnull
    @Override
    public String getName() {
        return "Sounds: " + modid;
    }

    protected abstract void addSoundEvents();

    protected SoundBuilder createSoundBuilder(ResourceLocation location) {
        Preconditions.checkArgument(existingFileHelper.exists(location, ResourcePackType.CLIENT_RESOURCES, ".ogg", "sounds"),
              "Sound %s does not exist in any known resource pack", location);
        return new SoundBuilder(location);
    }

    protected void addSoundEvent(SoundEventBuilder soundEventBuilder) {
        String path = soundEventBuilder.getPath();
        if (soundEventBuilders.containsKey(path)) {
            throw new RuntimeException("Sound event '" + path + "' has already been added.");
        }
        soundEventBuilders.put(path, soundEventBuilder);
    }

    /**
     * Helper method for {@link #addSoundEvent(SoundEventBuilder)}, for when we just have a single sound
     */
    protected void addSoundEvent(SoundEventRegistryObject<?> soundEventRO, ResourceLocation location) {
        addSoundEvent(SoundEventBuilder.create(soundEventRO).addSounds(createSoundBuilder(location)));
    }

    /**
     * Helper method for {@link #addSoundEvent(SoundEventBuilder)}, for when we just have a single sound and want make it have a subtitle
     */
    protected void addSoundEventWithSubtitle(SoundEventRegistryObject<?> soundEventRO, ResourceLocation location) {
        addSoundEvent(SoundEventBuilder.create(soundEventRO).subtitle(soundEventRO).addSounds(createSoundBuilder(location)));
    }

    /**
     * Helper method for {@link #addSoundEvent(SoundEventBuilder)}, for when we just have a single sound and a subtitle
     */
    protected void addSoundEvent(SoundEventRegistryObject<?> soundEventRO, ResourceLocation location, ILangEntry subtitle) {
        addSoundEvent(SoundEventBuilder.create(soundEventRO).subtitle(subtitle).addSounds(createSoundBuilder(location)));
    }

    @Override
    public void act(@Nonnull DirectoryCache cache) {
        soundEventBuilders.clear();
        addSoundEvents();
        if (!soundEventBuilders.isEmpty()) {
            JsonObject jsonObject = new JsonObject();
            for (Entry<String, SoundEventBuilder> entry : soundEventBuilders.entrySet()) {
                jsonObject.add(entry.getKey(), entry.getValue().toJson());
            }
            try {
                IDataProvider.save(GSON, cache, jsonObject, gen.getOutputFolder().resolve("assets/" + modid + "/sounds.json"));
            } catch (IOException e) {
                throw new RuntimeException("Couldn't save sounds.json for mod: " + modid, e);
            }
        }
    }
}