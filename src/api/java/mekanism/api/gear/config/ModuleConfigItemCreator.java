package mekanism.api.gear.config;

import java.util.function.BooleanSupplier;
import mekanism.api.text.ILangEntry;

/**
 * Helper to create {@link IModuleConfigItem}s.
 */
public interface ModuleConfigItemCreator {

    /**
     * Creates a config item for the given {@link ModuleConfigData}.
     *
     * @param name        Name.
     * @param description Description.
     * @param data        Config data.
     * @param <TYPE>      Type of the config data. See {@link ModuleConfigData} for the current restrictions on the types of supported data.
     *
     * @return A new {@link IModuleConfigItem}.
     */
    <TYPE> IModuleConfigItem<TYPE> createConfigItem(String name, ILangEntry description, ModuleConfigData<TYPE> data);

    /**
     * Creates a boolean config item that is disableable via a boolean supplier.
     *
     * @param name            Name.
     * @param description     Description.
     * @param def             Default value.
     * @param isConfigEnabled Boolean supplier representing if this config item is currently enabled or disabled. Returns {@code true} for enabled.
     *
     * @return A new {@link IModuleConfigItem}.
     */
    IModuleConfigItem<Boolean> createDisableableConfigItem(String name, ILangEntry description, boolean def, BooleanSupplier isConfigEnabled);
}