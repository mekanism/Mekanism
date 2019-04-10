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

        //TODO: Special loading for enums, such as to default to RF and also allow aliases

        /*
        String s = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "EnergyType", "RF", "Displayed energy type in Mekanism GUIs.",
                    new String[]{"J", "RF", "EU", "T"})
              .getString().trim().toLowerCase();

        switch (s) {
            case "joules":
                general.energyUnit = EnergyType.J;
                break;
            case "eu":
            case "ic2":
                general.energyUnit = EnergyType.EU;
                break;
            case "tesla":
                general.energyUnit = EnergyType.T;
                break;
            default:
                general.energyUnit = EnergyType.RF;
        }

        s = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "Temperature Units", "K",
                    "Displayed temperature unit in Mekanism GUIs.", new String[]{"K", "C", "R", "F"})
              .getString();

        if (s != null) {
            if (s.trim().equalsIgnoreCase("k") || s.trim().equalsIgnoreCase("kelvin")) {
                general.tempUnit = TempType.K;
            } else if (s.trim().equalsIgnoreCase("c") || s.trim().equalsIgnoreCase("celsius") || s.trim()
                  .equalsIgnoreCase("centigrade")) {
                general.tempUnit = TempType.C;
            } else if (s.trim().equalsIgnoreCase("r") || s.trim().equalsIgnoreCase("rankine")) {
                general.tempUnit = TempType.R;
            } else if (s.trim().equalsIgnoreCase("f") || s.trim().equalsIgnoreCase("fahrenheit")) {
                general.tempUnit = TempType.F;
            } else if (s.trim().equalsIgnoreCase("a") || s.trim().equalsIgnoreCase("ambient") || s.trim()
                  .equalsIgnoreCase("stp")) {
                general.tempUnit = TempType.STP;
            }
        }
         */
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