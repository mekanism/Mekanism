package mekanism.common.config.options;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.config.BaseConfig;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * Created by Thiakil on 15/03/2019.
 */
@ParametersAreNonnullByDefault
public class EnumOption<T extends Enum<T>> extends Option<EnumOption<T>> {

    private T value;
    private final T defaultValue;
    private final Class<T> enumClass;
    private final T[] enumValues;

    @SuppressWarnings("unchecked")
    public EnumOption(BaseConfig owner, String category, String key, T defaultValue, @Nullable String comment) {
        super(owner, category, key, comment);
        this.defaultValue = Objects.requireNonNull(defaultValue);
        this.value = defaultValue;
        this.enumClass = (Class<T>) defaultValue.getClass();
        this.enumValues = enumClass.getEnumConstants();
    }

    public EnumOption(BaseConfig owner, String category, String key, T defaultValue) {
        this(owner, category, key, defaultValue, null);
    }

    public T val() {
        return value;
    }

    public void set(T value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public void load(Configuration config) {
        final Property prop = config.get(this.category, this.key, this.defaultValue.name(), this.comment);

        prop.setRequiresMcRestart(this.requiresGameRestart);
        prop.setRequiresWorldRestart(this.requiresWorldRestart);

        this.value = Enum.valueOf(this.enumClass, prop.getString());
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(this.value.ordinal());
    }

    @Override
    public void read(ByteBuf buf) {
        this.value = this.enumValues[buf.readInt()];
    }
}