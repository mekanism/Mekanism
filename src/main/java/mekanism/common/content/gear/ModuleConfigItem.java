package mekanism.common.content.gear;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleConfigData;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.api.text.ILangEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModuleConfigItem<TYPE> implements IModuleConfigItem<TYPE> {

    protected final ModuleData<?> moduleType;
    private final String name;
    private final ILangEntry description;
    private final ModuleConfigData<TYPE> data;

    public ModuleConfigItem(ModuleData<?> moduleType, String name, ILangEntry description, ModuleConfigData<TYPE> data) {
        this.moduleType = moduleType;
        this.name = name;
        this.description = description;
        this.data = data;
    }

    public Component getDescription() {
        return description.translate();
    }

    public ModuleConfigData<TYPE> getData() {
        return data;
    }

    @NotNull
    @Override
    public TYPE get() {
        return data.get();
    }

    @Override
    public void set(@NotNull TYPE val) {
        set(val, null);
    }

    public void set(@NotNull TYPE val, @Nullable Runnable callback) {
        Objects.requireNonNull(val, "Value cannot be null.");
        data.set(val);
        // perform any validity checks such as disabling conflicting modules
        checkValidity(val, callback);
        // finally, run the callback (to send a packet)
        if (callback != null) {
            callback.run();
        }
    }

    protected void checkValidity(@NotNull TYPE val, @Nullable Runnable callback) {
    }

    public boolean matches(IModuleDataProvider<?> moduleType, String name) {
        return this.moduleType == moduleType.getModuleData() && getName().equals(name);
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    public static class DisableableModuleConfigItem extends ModuleConfigItem<Boolean> {

        private final BooleanSupplier isConfigEnabled;

        public DisableableModuleConfigItem(ModuleData<?> moduleType, String name, ILangEntry description, boolean def, BooleanSupplier isConfigEnabled) {
            super(moduleType, name, description, new ModuleBooleanData(def));
            this.isConfigEnabled = isConfigEnabled;
        }

        @NotNull
        @Override
        public Boolean get() {
            return isConfigEnabled() && super.get();
        }

        public boolean isConfigEnabled() {
            return isConfigEnabled.getAsBoolean();
        }
    }
}