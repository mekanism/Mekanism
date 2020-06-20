package mekanism.client.sound;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.common.DataGenJsonConstants;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SoundBuilder {

    private final ResourceLocation location;
    private float volume = 1;
    private float pitch = 1;
    private int weight = 1;
    private boolean stream;
    private int attenuationDistance = 16;
    private boolean preload;
    private SoundType type = SoundType.SOUND;

    /**
     * @apiNote Access via {@link BaseSoundProvider#createSoundBuilder(ResourceLocation)}
     */
    SoundBuilder(ResourceLocation location) {
        this.location = location;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    /**
     * The volume this sound will be played at. Value is a decimal between 0.0 and 1.0
     *
     * @apiNote Optional, defaults to {@code 1}
     */
    public SoundBuilder volume(float volume) {
        if (volume < 0 || volume > 1) {
            throw new RuntimeException("Error volume for sound: '" + serializeLoc() + "' must be between 0.0 and 1.0 inclusive.");
        }
        this.volume = volume;
        return this;
    }

    /**
     * Plays the pitch at the specified value
     *
     * @apiNote Optional, defaults to {@code 1}
     */
    public SoundBuilder pitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    /**
     * The chance that this sound will be selected to play when this sound event is triggered
     *
     * @apiNote Optional, defaults to {@code 1}
     */
    public SoundBuilder weight(int weight) {
        if (weight < 1) {
            throw new RuntimeException("Error weight for sound: '" + serializeLoc() + "' must be at least 1.");
        }
        this.weight = weight;
        return this;
    }

    /**
     * If this sound should be streamed from its file. It is recommended that this is set for sounds that have a duration longer than a few seconds to avoid lag. Used for
     * all sounds in the "music" and "record" categories (except Note Block sounds), as (almost) all the sounds that belong to those categories are over a minute long.
     * Not setting this allows many more instances of the sound to be ran at the same time while setting it only allows 4 instances (of that type) to be ran at the same
     * time.
     */
    public SoundBuilder stream() {
        this.stream = true;
        return this;
    }

    /**
     * Modify sound reduction rate based on distance. Used by portals, beacons, and conduits.
     *
     * @apiNote Optional, defaults to {@code 16}
     */
    public SoundBuilder attenuationDistance(int attenuationDistance) {
        if (attenuationDistance < 1) {
            throw new RuntimeException("Error attenuation distance for sound: '" + serializeLoc() + "' must be at least 1.");
        }
        this.attenuationDistance = attenuationDistance;
        return this;
    }

    /**
     * If this sound should be loaded when loading the pack instead of when the sound is played. Used by the underwater ambiance.
     */
    public SoundBuilder preload() {
        this.preload = true;
        return this;
    }

    /**
     * Two values are available: {@link SoundType#SOUND} and {@link SoundType#EVENT}; {@link SoundType#SOUND} plays from the name of the file, while {@link
     * SoundType#EVENT} plays from an already defined event.
     *
     * @apiNote Optional, defaults to {@link SoundType#SOUND}
     */
    public SoundBuilder type(SoundType type) {
        this.type = type;
        return this;
    }

    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        boolean hasSettings = false;
        if (volume != 1) {
            jsonObject.addProperty(DataGenJsonConstants.VOLUME, volume);
            hasSettings = true;
        }
        if (pitch != 1) {
            jsonObject.addProperty(DataGenJsonConstants.PITCH, pitch);
            hasSettings = true;
        }
        if (weight != 1) {
            jsonObject.addProperty(DataGenJsonConstants.WEIGHT, weight);
            hasSettings = true;
        }
        if (stream) {
            jsonObject.addProperty(DataGenJsonConstants.STREAM, true);
            hasSettings = true;
        }
        if (attenuationDistance != 16) {
            jsonObject.addProperty(DataGenJsonConstants.ATTENUATION_DISTANCE, attenuationDistance);
            hasSettings = true;
        }
        if (preload) {
            jsonObject.addProperty(DataGenJsonConstants.PRELOAD, true);
            hasSettings = true;
        }
        if (type != SoundType.SOUND) {
            jsonObject.addProperty(DataGenJsonConstants.TYPE, type.value);
            hasSettings = true;
        }
        if (hasSettings) {
            jsonObject.addProperty(DataGenJsonConstants.NAME, serializeLoc());
            return jsonObject;
        }
        return new JsonPrimitive(serializeLoc());
    }

    private String serializeLoc() {
        if (location.getNamespace().equals("minecraft")) {
            return location.getPath();
        }
        return location.toString();
    }

    public enum SoundType {
        SOUND("sound"),
        EVENT("event");

        private final String value;

        SoundType(String value) {
            this.value = value;
        }
    }
}