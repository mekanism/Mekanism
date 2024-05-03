package mekanism.api.gear.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Objects;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//TODO - 1.20.5: Docs
@NothingNullByDefault
public class ModuleEnumConfig<TYPE extends Enum<TYPE> & IHasTextComponent> extends ModuleConfig<TYPE> {

    public static <TYPE extends Enum<TYPE> & IHasTextComponent> Codec<ModuleEnumConfig<TYPE>> codec(Codec<TYPE> enumCodec) {
        return RecordCodecBuilder.create(instance -> baseCodec(instance)
              .and(enumCodec.fieldOf(NBTConstants.VALUE).forGetter(ModuleConfig::get))
              .apply(instance, ModuleEnumConfig::new));
    }

    public static <TYPE extends Enum<TYPE> & IHasTextComponent> Codec<ModuleEnumConfig<TYPE>> codec(Codec<TYPE> enumCodec, Class<TYPE> enumClass, int selectableCount) {
        if (selectableCount <= 0) {
            throw new IllegalArgumentException("Invalid selectableCount, there must be at least one element that is selectable.");
        }
        //We calculate and then capture this so that we can have a single list instance used
        List<TYPE> enumConstants = getEnumConstants(enumClass, selectableCount);
        return RecordCodecBuilder.create(instance -> baseCodec(instance)
              .and(enumCodec.validate(value -> value.ordinal() < selectableCount ? DataResult.success(value) :
                                               DataResult.error(() -> "Invalid value" + value.name() + ", it is out of range of the selectable values."))
                    .fieldOf(NBTConstants.VALUE).forGetter(ModuleConfig::get))
              .apply(instance, (name, value) -> new ModuleEnumConfig<>(name, value, enumConstants)));
    }

    public static <BUF extends ByteBuf, TYPE extends Enum<TYPE> & IHasTextComponent> StreamCodec<BUF, ModuleEnumConfig<TYPE>> streamCodec(StreamCodec<BUF, TYPE> enumCodec) {
        return StreamCodec.composite(
              ByteBufCodecs.STRING_UTF8, ModuleConfig::name,
              enumCodec, ModuleConfig::get,
              ModuleEnumConfig::new
        );
    }

    public static <BUF extends ByteBuf, TYPE extends Enum<TYPE> & IHasTextComponent> StreamCodec<BUF, ModuleEnumConfig<TYPE>> streamCodec(StreamCodec<BUF, TYPE> enumCodec,
          Class<TYPE> enumClass, int selectableCount) {
        if (selectableCount <= 0) {
            throw new IllegalArgumentException("Invalid selectableCount, there must be at least one element that is selectable.");
        }
        //We calculate and then capture this so that we can have a single list instance used
        List<TYPE> enumConstants = getEnumConstants(enumClass, selectableCount);
        return StreamCodec.composite(
              ByteBufCodecs.STRING_UTF8, ModuleConfig::name,
              enumCodec, ModuleConfig::get,
              (name, value) -> new ModuleEnumConfig<>(name, value, enumConstants)
        );
    }

    public static <TYPE extends Enum<TYPE> & IHasTextComponent> ModuleEnumConfig<TYPE> create(String name, TYPE value) {
        return new ModuleEnumConfig<>(name, value);
    }

    public static <TYPE extends Enum<TYPE> & IHasTextComponent> ModuleEnumConfig<TYPE> createBounded(String name, TYPE value, int selectableCount) {
        if (selectableCount <= 0) {
            throw new IllegalArgumentException("Invalid selectableCount, there must be at least one element that is selectable.");
        } else if (value.ordinal() >= selectableCount) {
            throw new IllegalArgumentException("Invalid value, it is out of range of the selectable values.");
        }
        Class<TYPE> enumClass = value.getDeclaringClass();
        TYPE[] constants = enumClass.getEnumConstants();
        if (constants.length < selectableCount) {
            throw new IllegalArgumentException("Selectable count is larger than the number of elements in " + enumClass.getSimpleName());
        }
        return new ModuleEnumConfig<>(name, value, getEnumConstants(enumClass, selectableCount));
    }

    private static <TYPE extends Enum<TYPE> & IHasTextComponent> List<TYPE> getEnumConstants(Class<TYPE> enumClass, int selectableCount) {
        TYPE[] constants = enumClass.getEnumConstants();
        if (constants.length < selectableCount) {
            throw new IllegalArgumentException("Selectable count is larger than the number of elements in " + enumClass.getSimpleName());
        }
        if (constants.length == selectableCount) {
            return List.of(constants);
        }
        return List.of(constants).subList(0, selectableCount);
    }

    private final List<TYPE> enumConstants;
    private final TYPE value;

    private ModuleEnumConfig(String name, TYPE value) {
        this(name, value, List.of(value.getDeclaringClass().getEnumConstants()));
    }

    private ModuleEnumConfig(String name, TYPE value, List<TYPE> enumConstants) {
        super(name);
        this.value = value;
        this.enumConstants = enumConstants;
    }

    @Override
    public TYPE get() {
        return value;
    }

    public List<TYPE> getEnumConstants() {
        return enumConstants;
    }

    @Override
    public ModuleEnumConfig<TYPE> with(TYPE value) {
        Objects.requireNonNull(value, "Value cannot be null.");
        if (value.ordinal() >= enumConstants.size()) {
            //TODO - 1.20.5: Do we want an exception or for this to just default to something either being this or the value at index zero?
            throw new IllegalArgumentException("Invalid value, it is out of range of the selectable values.");
        } else if (this.value == value) {
            return this;
        }
        return new ModuleEnumConfig<>(name(), value);
    }

    public ModuleEnumConfig<TYPE> with(int index) {
        if (index < 0 || index >= enumConstants.size()) {
            //TODO - 1.20.5: Do we want an exception or for this to just default to something either being this or the value at index zero?
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
        return Objects.hash(super.hashCode(), value, enumConstants.size());
    }
}