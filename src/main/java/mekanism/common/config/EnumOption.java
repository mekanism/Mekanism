package mekanism.common.config;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * Created by Thiakil on 15/03/2019.
 */
@ParametersAreNonnullByDefault
public class EnumOption<T extends Enum<T>> extends Option<EnumOption<T>> {

    private final T defaultValue;
    private final Class<T> enumClass;
    private final T[] enumValues;
    private T value;

    @SuppressWarnings("unchecked")
    EnumOption(BaseConfig owner, String category, String key, T defaultValue, @Nullable String comment) {
        super(owner, category, key, comment);
        this.defaultValue = Objects.requireNonNull(defaultValue);
        this.value = defaultValue;
        this.enumClass = (Class<T>) defaultValue.getClass();
        this.enumValues = enumClass.getEnumConstants();
    }

    EnumOption(BaseConfig owner, String category, String key, T defaultValue) {
        this(owner, category, key, defaultValue, null);
    }

    public T val() {
        return value;
    }

    public void set(T value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    protected void load(Configuration config) {
        final Property prop = config.get(this.category, this.key, this.defaultValue.name(), this.comment);

        prop.setRequiresMcRestart(this.requiresGameRestart);
        prop.setRequiresWorldRestart(this.requiresWorldRestart);

        this.value = Enum.valueOf(this.enumClass, prop.getString());
    }

    @Override
    protected void write(ByteBuf buf) {
        buf.writeInt(this.value.ordinal());
    }

    @Override
    protected void read(ByteBuf buf) {
        this.value = this.enumValues[buf.readInt()];
    }
}
