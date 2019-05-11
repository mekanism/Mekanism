package mekanism.common.config;

import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.config.options.Option;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

@ParametersAreNonnullByDefault
public class TypeConfigManager<T extends Enum<T>> extends Option<TypeConfigManager<T>> {

    private EnumSet<T> config;
    private Supplier<List<T>> validValuesSupplier;
    private Function<T, String> nameSupplier;

    TypeConfigManager(BaseConfig owner, String category, Class<T> enumClazz, Supplier<List<T>> validValuesSupplier, Function<T, String> nameSupplier) {
        super(owner, category, "", null);//key unused
        this.validValuesSupplier = validValuesSupplier;
        this.nameSupplier = nameSupplier;
        this.config = EnumSet.noneOf(enumClazz);
    }

    public boolean isEnabled(@Nullable T type) {
        return config.contains(type);
    }

    public void setEntry(T type, boolean enabled) {
        if (enabled) {
            config.add(type);
        } else {
            config.remove(type);
        }
    }

    /**
     * Get the enum constant from a name. Used in recipes, allowed to be non-cached
     *
     * @param name JSON supplied name
     *
     * @return the found enum constant or null
     */
    @Nullable
    public T typeFromName(String name) {
        for (T type : validValuesSupplier.get()) {
            if (nameSupplier.apply(type).equals(name)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public void load(Configuration config) {
        for (T type : validValuesSupplier.get()) {
            String typeName = nameSupplier.apply(type);
            final Property property = config.get(this.category, typeName + "Enabled", true,
                  "Allow " + typeName + " to be used/crafted. Requires game restart to fully take effect.");
            property.setRequiresWorldRestart(true);
            setEntry(type, property.getBoolean());
        }
    }

    @Override
    public void write(ByteBuf buf) {
        for (T type : validValuesSupplier.get()) {
            buf.writeBoolean(config.contains(type));
        }
    }

    @Override
    public void read(ByteBuf buf) {
        config.clear();
        for (T type : validValuesSupplier.get()) {
            if (buf.readBoolean()) {
                config.add(type);
            }
        }
    }
}