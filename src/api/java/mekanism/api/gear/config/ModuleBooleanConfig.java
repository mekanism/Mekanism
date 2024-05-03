package mekanism.api.gear.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//TODO - 1.20.5: Docs
@NothingNullByDefault
public class ModuleBooleanConfig extends ModuleConfig<Boolean> {

    public static final Codec<ModuleBooleanConfig> CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance)
          .and(Codec.BOOL.fieldOf(NBTConstants.VALUE).forGetter(ModuleConfig::get))
          .apply(instance, ModuleBooleanConfig::new));
    public static final StreamCodec<ByteBuf, ModuleBooleanConfig> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.STRING_UTF8, ModuleConfig::name,
          ByteBufCodecs.BOOL, ModuleConfig::get,
          ModuleBooleanConfig::new
    );

    private final boolean value;

    public ModuleBooleanConfig(String name, boolean value) {
        super(name);
        this.value = value;
    }

    @Override
    public Boolean get() {
        return value;
    }

    @Override
    public ModuleBooleanConfig with(Boolean value) {
        Objects.requireNonNull(value, "Value cannot be null.");
        if (this.value == value) {
            return this;
        }
        return new ModuleBooleanConfig(name(), value);
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
        return Objects.hash(super.hashCode(), value);
    }
}