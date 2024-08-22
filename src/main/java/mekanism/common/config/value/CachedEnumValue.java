package mekanism.common.config.value;

import mekanism.common.config.IMekanismConfig;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.TranslatableEnum;

public class CachedEnumValue<T extends Enum<T>> extends CachedConfigValue<T> {

    private CachedEnumValue(IMekanismConfig config, EnumValue<T> internal) {
        super(config, internal);
    }

    //Note: Ensure that we provide a nice translated name for any enum value based configs we have
    public static <T extends Enum<T> & TranslatableEnum> CachedEnumValue<T> wrap(IMekanismConfig config, EnumValue<T> internal) {
        return new CachedEnumValue<>(config, internal);
    }
}