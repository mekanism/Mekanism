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
 * Immutable class representing a color based module config (name and int value).
 *
 * @since 10.6.0
 */
@NothingNullByDefault
public class ModuleColorConfig extends ModuleConfig<Integer> {

    /**
     * Codec for (de)serializing ARGB based color module configs.
     */
    public static final Codec<ModuleColorConfig> ARGB_CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance)
          .and(Codec.INT.fieldOf(SerializationConstants.VALUE).forGetter(ModuleConfig::get))
          .apply(instance, ModuleColorConfig::argb));
    /**
     * Stream codec for encoding and decoding ARGB based color module configs over the network.
     */
    public static final StreamCodec<ByteBuf, ModuleColorConfig> ARGB_STREAM_CODEC = StreamCodec.composite(
          ResourceLocation.STREAM_CODEC, ModuleConfig::name,
          //Note: We don't do var-int as we include alpha data
          ByteBufCodecs.INT, ModuleConfig::get,
          ModuleColorConfig::argb
    );
    /**
     * Codec for (de)serializing RGB based color module configs.
     */
    public static final Codec<ModuleColorConfig> RGB_CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance)
          .and(Codec.INT.fieldOf(SerializationConstants.VALUE).forGetter(ModuleConfig::get))
          //If we don't handle alpha make sure we do have the alpha component present
          .apply(instance, ModuleColorConfig::rgb));
    /**
     * Stream codec for encoding and decoding RGB based color module configs over the network.
     */
    public static final StreamCodec<ByteBuf, ModuleColorConfig> RGB_STREAM_CODEC = StreamCodec.composite(
          ResourceLocation.STREAM_CODEC, ModuleConfig::name,
          //Note: We can use var int here and just not send the alpha data over the network
          ByteBufCodecs.VAR_INT, module -> module.get() & 0x00FFFFFF,
          ModuleColorConfig::rgb
    );

    /**
     * Creates a new {@link ModuleColorConfig} that supports alpha and has a default value of white ({@code 0xFFFFFFFF}).
     *
     * @implNote Color format is ARGB.
     */
    public static ModuleColorConfig argb(ResourceLocation name) {
        return argb(name, 0xFFFFFFFF);
    }

    /**
     * Creates a new {@link ModuleColorConfig} that supports alpha and has the given default color.
     *
     * @param defaultColor Default color.
     *
     * @implNote Color format is ARGB.
     */
    public static ModuleColorConfig argb(ResourceLocation name, int defaultColor) {
        return new ModuleColorConfig(name, true, defaultColor);
    }

    /**
     * Creates a new {@link ModuleColorConfig} that doesn't support alpha and has a default value of white ({@code 0xFFFFFFFF}).
     *
     * @implNote Color format is ARGB with the alpha component being locked to {@code 0xFF}.
     */
    public static ModuleColorConfig rgb(ResourceLocation name) {
        return rgb(name, 0xFFFFFFFF);
    }

    /**
     * Creates a new {@link ModuleColorConfig} that doesn't support alpha and has the given default color.
     *
     * @param defaultColor Default color.
     *
     * @implNote Color format is ARGB with the alpha component being locked to {@code 0xFF}.
     */
    public static ModuleColorConfig rgb(ResourceLocation name, int defaultColor) {
        //If we don't handle alpha make sure we do have the alpha component present
        return new ModuleColorConfig(name, false, defaultColor);
    }

    private final boolean supportsAlpha;
    private final int value;

    private ModuleColorConfig(ResourceLocation name, boolean supportsAlpha, int value) {
        super(name);
        this.supportsAlpha = supportsAlpha;
        //If we don't handle alpha make sure we do have the alpha component present though
        this.value = this.supportsAlpha ? value : value | 0xFF000000;
    }

    /**
     * Gets whether this module config supports alpha, if it does not the color returned will fully opaque.
     *
     * @return {@code true} if this data can handle alpha.
     */
    public boolean supportsAlpha() {
        return supportsAlpha;
    }

    @Override
    public StreamCodec<ByteBuf, ModuleConfig<Integer>> namedStreamCodec(ResourceLocation name) {
        if (supportsAlpha) {
            //Note: We don't do varint as we include alpha data
            return ByteBufCodecs.INT.map(val -> argb(name, val), ModuleConfig::get);
        }
        //Note: We can use var int here and just not send the alpha data over the network
        return ByteBufCodecs.VAR_INT.map(val -> rgb(name, val), module -> module.get() & 0x00FFFFFF);
    }

    /**
     * @implNote If this config does not support alpha, the alpha component is locked to {@code 0xFF} instead of being missing.
     */
    @Override
    public Integer get() {
        return value;
    }

    @Override
    public ModuleColorConfig with(Integer value) {
        Objects.requireNonNull(value, "Value cannot be null.");
        int sanitizedValue = this.supportsAlpha ? value : value | 0xFF000000;
        return this.value == sanitizedValue ? this : new ModuleColorConfig(name(), supportsAlpha, sanitizedValue);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!super.equals(o)) {
            return false;
        }
        ModuleColorConfig other = (ModuleColorConfig) o;
        return supportsAlpha == other.supportsAlpha && value == other.value;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Boolean.hashCode(supportsAlpha);
        result = 31 * result + value;
        return result;
    }
}