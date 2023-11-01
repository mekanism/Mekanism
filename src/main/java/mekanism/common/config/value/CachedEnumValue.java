package mekanism.common.config.value;

import mekanism.common.config.IMekanismConfig;
import net.neoforged.neoforge.common.NeoForgeConfigSpec.EnumValue;

public class CachedEnumValue<T extends Enum<T>> extends CachedConfigValue<T> {

    private CachedEnumValue(IMekanismConfig config, EnumValue<T> internal) {
        super(config, internal);
    }

    public static <T extends Enum<T>> CachedEnumValue<T> wrap(IMekanismConfig config, EnumValue<T> internal) {
        return new CachedEnumValue<>(config, internal);
    }
}