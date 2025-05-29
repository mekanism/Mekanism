package mekanism.api.gear.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable class representing an enum based module config (name and enum value).
 *
 * @param <TYPE> Type of the data stored by this config.
 *
 * @since 10.6.0
 */
@NothingNullByDefault
public class ModuleEnumConfig<TYPE extends Enum<TYPE> & IHasTextComponent> extends ModuleConfig<TYPE> {

    /**
     * Codec for (de)serializing enum module configs, with no limits on what options are valid.
     *
     * @param enumCodec Codec for (de)serializing the enum value.
     * @param <TYPE>    Type of the data stored by this config.
     */
    public static <TYPE extends Enum<TYPE> & IHasTextComponent> Codec<ModuleEnumConfig<TYPE>> codec(Codec<TYPE> enumCodec) {
        return RecordCodecBuilder.create(instance -> baseCodec(instance)
              .and(enumCodec.fieldOf(SerializationConstants.VALUE).forGetter(ModuleConfig::get))
              .apply(instance, ModuleEnumConfig::new));
    }

    /**
     * Codec for (de)serializing enum module configs, limited to the specified range of values.
     *
     * @param enumCodec       Codec for (de)serializing the enum value.
     * @param enumClass       Enum class
     * @param selectableCount The number of selectable elements.
     * @param <TYPE>          Type of the data stored by this config.
     *
     * @throws IllegalArgumentException If selectableCount is less than one, or greater than the number of elements in the enum.
     * @implNote If selectedCount is equal to the number of elements in the enum, this acts as if {@link #codec(Codec)} was called instead.
     */
    public static <TYPE extends Enum<TYPE> & IHasTextComponent> Codec<ModuleEnumConfig<TYPE>> codec(Codec<TYPE> enumCodec, Class<TYPE> enumClass, int selectableCount) {
        if (selectableCount <= 0) {
            throw new IllegalArgumentException("Invalid selectableCount, there must be at least one element that is selectable.");
        }
        //We calculate and then capture this so that we can have a single list instance used
        List<TYPE> enumConstants = getEnumConstants(enumClass, selectableCount);
        if (enumConstants == null) {
            //Don't bother validating the index as we can select any of the values
            return codec(enumCodec);
        }
        return RecordCodecBuilder.create(instance -> baseCodec(instance)
              .and(enumCodec.validate(value -> value.ordinal() < selectableCount ? DataResult.success(value) :
                                               DataResult.error(() -> "Invalid value" + value.name() + ", it is out of range of the selectable values."))
                    .fieldOf(SerializationConstants.VALUE).forGetter(ModuleConfig::get))
              .apply(instance, (name, value) -> new ModuleEnumConfig<>(name, value, enumConstants)));
    }

    /**
     * Stream codec for encoding and decoding enum module configs, with no limits on what options are valid, over the network.
     *
     * @param enumCodec Stream codec for encoding and decoding the enum value.
     * @param <TYPE>    Type of the data stored by this config.
     */
    public static <BUF extends ByteBuf, TYPE extends Enum<TYPE> & IHasTextComponent> StreamCodec<BUF, ModuleEnumConfig<TYPE>> streamCodec(StreamCodec<BUF, TYPE> enumCodec) {
        return StreamCodec.composite(
              ResourceLocation.STREAM_CODEC, ModuleConfig::name,
              enumCodec, ModuleConfig::get,
              ModuleEnumConfig::new
        );
    }

    /**
     * Stream codec for encoding and decoding enum module configs, limited to the specified range of values, over the network
     *
     * @param enumCodec       Stream codec for encoding and decoding the enum value.
     * @param enumClass       Enum class
     * @param selectableCount The number of selectable elements.
     * @param <TYPE>          Type of the data stored by this config.
     *
     * @throws IllegalArgumentException If selectableCount is less than one, or greater than the number of elements in the enum.
     * @implNote If selectedCount is equal to the number of elements in the enum, this acts as if {@link #streamCodec(StreamCodec)} was called instead.
     */
    public static <BUF extends ByteBuf, TYPE extends Enum<TYPE> & IHasTextComponent> StreamCodec<BUF, ModuleEnumConfig<TYPE>> streamCodec(StreamCodec<BUF, TYPE> enumCodec,
          Class<TYPE> enumClass, int selectableCount) {
        if (selectableCount <= 0) {
            throw new IllegalArgumentException("Invalid selectableCount, there must be at least one element that is selectable.");
        }
        //We calculate and then capture this so that we can have a single list instance used
        List<TYPE> enumConstants = getEnumConstants(enumClass, selectableCount);
        if (enumConstants == null) {
            //Don't bother validating the index as we can select any of the values
            return streamCodec(enumCodec);
        }
        return StreamCodec.composite(
              ResourceLocation.STREAM_CODEC, ModuleConfig::name,
              enumCodec, ModuleConfig::get,
              (name, value) -> new ModuleEnumConfig<>(name, value, enumConstants)
        );
    }

    /**
     * Creates a new enum module config with the given name, value, and no limits on what options are valid.
     *
     * @param name   Name of the config option.
     * @param value  Value of the config option.
     * @param <TYPE> Type of the data stored by this config.
     */
    public static <TYPE extends Enum<TYPE> & IHasTextComponent> ModuleEnumConfig<TYPE> create(ResourceLocation name, TYPE value) {
        return new ModuleEnumConfig<>(name, value);
    }

    /**
     * Creates a new enum module config with the given name, value, and is limited to the specified range of values.
     *
     * @param name            Name of the config option.
     * @param value           Value of the config option.
     * @param selectableCount The number of selectable elements.
     * @param <TYPE>          Type of the data stored by this config.
     *
     * @implNote If selectedCount is equal to the number of elements in the enum, this acts as if {@link #create(ResourceLocation, Enum)} was called instead.
     */
    public static <TYPE extends Enum<TYPE> & IHasTextComponent> ModuleEnumConfig<TYPE> createBounded(ResourceLocation name, TYPE value, int selectableCount) {
        if (selectableCount <= 0) {
            throw new IllegalArgumentException("Invalid selectableCount, there must be at least one element that is selectable.");
        } else if (value.ordinal() >= selectableCount) {
            throw new IllegalArgumentException("Invalid value, it is out of range of the selectable values.");
        }
        Class<TYPE> enumClass = value.getDeclaringClass();
        List<TYPE> enumConstants = getEnumConstants(enumClass, selectableCount);
        if (enumConstants == null) {
            return create(name, value);
        }
        return new ModuleEnumConfig<>(name, value, enumConstants);
    }

    @Nullable
    private static <TYPE extends Enum<TYPE> & IHasTextComponent> List<TYPE> getEnumConstants(Class<TYPE> enumClass, int selectableCount) {
        TYPE[] constants = enumClass.getEnumConstants();
        if (constants.length < selectableCount) {
            throw new IllegalArgumentException("Selectable count is larger than the number of elements in " + enumClass.getSimpleName());
        } else if (constants.length == selectableCount) {
            return null;
        }
        return List.of(constants).subList(0, selectableCount);
    }

    private final List<TYPE> enumConstants;
    private final TYPE value;

    private ModuleEnumConfig(ResourceLocation name, TYPE value) {
        this(name, value, List.of(value.getDeclaringClass().getEnumConstants()));
    }

    private ModuleEnumConfig(ResourceLocation name, TYPE value, List<TYPE> enumConstants) {
        super(name);
        this.value = value;
        this.enumConstants = enumConstants;
    }

    @Override
    public StreamCodec<FriendlyByteBuf, ModuleConfig<TYPE>> namedStreamCodec(ResourceLocation name) {
        return NeoForgeStreamCodecs.enumCodec(value.getDeclaringClass()).map(
              value -> new ModuleEnumConfig<>(name, value, enumConstants),
              ModuleConfig::get
        );
    }

    @Override
    public TYPE get() {
        return value;
    }

    /**
     * {@return an immutable list of enum constants that this config supports}
     */
    public List<TYPE> getEnumConstants() {
        return enumConstants;
    }

    @Override
    public ModuleEnumConfig<TYPE> with(TYPE value) {
        Objects.requireNonNull(value, "Value cannot be null.");
        if (value.ordinal() >= enumConstants.size()) {
            throw new IllegalArgumentException("Invalid value, it is out of range of the selectable values.");
        } else if (this.value == value) {
            return this;
        }
        return new ModuleEnumConfig<>(name(), value);
    }

    /**
     * Creates a new immutable enum module config object that has a value equal to the nth enum value.
     *
     * @param index Enum index of the desired value.
     *
     * @return A new module config using the value with the specific index.
     *
     * @throws IllegalArgumentException If the specified value is not valid in the range of constants this config supports (used for invalid packets)
     */
    public ModuleEnumConfig<TYPE> with(int index) {
        if (index < 0 || index >= enumConstants.size()) {
            throw new IllegalArgumentException("Invalid value, it is out of range of the selectable values.");
        } else if (value.ordinal() == index) {
            return this;
        }
        return new ModuleEnumConfig<>(name(), enumConstants.get(index));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!super.equals(o)) {
            return false;
        }
        ModuleEnumConfig<?> other = (ModuleEnumConfig<?>) o;
        //Validate the value is the same and we have the same number of selectable values
        return value == other.value && enumConstants.size() == other.enumConstants.size();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + Integer.hashCode(enumConstants.size());
        return result;
    }
}