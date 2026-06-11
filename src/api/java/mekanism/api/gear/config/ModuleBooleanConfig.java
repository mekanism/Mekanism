package mekanism.api.gear.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/// Immutable class representing a boolean module config (name and boolean value).
///
/// @since 10.6.0
public class ModuleBooleanConfig extends ModuleConfig<Boolean> {

    /// Codec for (de)serializing boolean module configs.
    public static final Codec<ModuleBooleanConfig> CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance)
          .and(Codec.BOOL.fieldOf(SerializationConstants.VALUE).forGetter(ModuleConfig::get))
          .apply(instance, ModuleBooleanConfig::new));
    /// Stream codec for encoding and decoding boolean module configs over the network.
    public static final StreamCodec<ByteBuf, ModuleBooleanConfig> STREAM_CODEC = StreamCodec.composite(
          Identifier.STREAM_CODEC, ModuleConfig::name,
          ByteBufCodecs.BOOL, ModuleConfig::get,
          ModuleBooleanConfig::new
    );

    /// Creates a new boolean module config with the given name, and value.
    ///
    /// @param name  Name of the config option.
    /// @param value Value of the config option.
    public static ModuleBooleanConfig create(Identifier name, boolean value) {
        return new ModuleBooleanConfig(name, value);
    }

    private final boolean value;

    protected ModuleBooleanConfig(Identifier name, boolean value) {
        super(name);
        this.value = value;
    }

    @Override
    public StreamCodec<ByteBuf, ModuleConfig<Boolean>> namedStreamCodec(Identifier name) {
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
    public boolean equals(@Nullable Object o) {
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