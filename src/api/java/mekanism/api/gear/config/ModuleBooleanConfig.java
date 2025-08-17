package mekanism.api.gear.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Immutable class representing a boolean module config (name and boolean value).
 *
 * @since 10.6.0
 */
@NothingNullByDefault
public class ModuleBooleanConfig extends ModuleConfig<Boolean> {

    /**
     * Codec for (de)serializing boolean module configs.
     */
    public static final Codec<ModuleBooleanConfig> CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance)
          .and(Codec.BOOL.fieldOf(SerializationConstants.VALUE).forGetter(ModuleConfig::get))
          .apply(instance, ModuleBooleanConfig::new));
    /**
     * Stream codec for encoding and decoding boolean module configs over the network.
     */
    public static final StreamCodec<ByteBuf, ModuleBooleanConfig> STREAM_CODEC = StreamCodec.composite(
          ResourceLocation.STREAM_CODEC, ModuleConfig::name,
          ByteBufCodecs.BOOL, ModuleConfig::get,
          ModuleBooleanConfig::new
    );

    /**
     * Creates a new boolean module config with the given name, and value.
     *
     * @param name   Name of the config option.
     * @param value  Value of the config option.
     */
    public static ModuleBooleanConfig create(ResourceLocation name, boolean value) {
        return new ModuleBooleanConfig(name, value);
    }

    private final boolean value;

    protected ModuleBooleanConfig(ResourceLocation name, boolean value) {
        super(name);
        this.value = value;
    }

    @Override
    public StreamCodec<ByteBuf, ModuleConfig<Boolean>> namedStreamCodec(ResourceLocation name) {
        return ByteBufCodecs.BOOL.map(val -> new ModuleBooleanConfig(name, val), ModuleConfig::get);
    }

    @Override
    public Boolean get() {
        return value;
    }

    @Override
    public ModuleBooleanConfig with(Boolean value) {
        Objects.requireNonNull(value, "Value cannot be null.");
        return this.value == value ? this : new ModuleBooleanConfig(name(), value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!super.equals(o)) {
            return false;
        }
        return value == ((ModuleBooleanConfig) o).value;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Boolean.hashCode(value);
    }
}