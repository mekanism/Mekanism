package mekanism.common.content.gear;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleConfigData;
import mekanism.api.text.ILangEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModuleConfigItem<TYPE> implements IModuleConfigItem<TYPE> {

    private final Module<?> module;
    private final String name;
    private final ILangEntry description;
    private final ModuleConfigData<TYPE> data;

    public ModuleConfigItem(Module<?> module, String name, ILangEntry description, ModuleConfigData<TYPE> data) {
        this.module = module;
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
        // validity checks
        for (Module<?> m : ModuleHelper.INSTANCE.loadAll(module.getContainer())) {
            boolean checkModeState;
            if (name.equals(Module.ENABLED_KEY) && val == Boolean.TRUE) {
                // disable other exclusive modules
                if (m.getData() != module.getData() && m.getData().isExclusive(module.getData().getExclusiveFlags())) {
                    m.setDisabledForce(callback != null);
                }
                //If enabled state of the module changes, recheck about mode changes
                checkModeState = true;
            } else {
                checkModeState = name.equals(Module.HANDLE_MODE_CHANGE_KEY) && val == Boolean.TRUE;
            }
            // turn off mode change handling for other modules
            if (checkModeState && module.handlesModeChange()) {
                if (m.handlesModeChange() && m.getData() != module.getData()) {
                    m.setModeHandlingDisabledForce();
                }
            }
        }
        // finally, save this specific module with the callback (to send a packet)
        module.save(callback);
    }

    public void read(CompoundTag tag) {
        if (tag.contains(name)) {
            data.read(name, tag);
        }
    }

    public void write(CompoundTag tag) {
        data.write(name, tag);
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    public static class DisableableModuleConfigItem extends ModuleConfigItem<Boolean> {

        private final BooleanSupplier isConfigEnabled;

        public DisableableModuleConfigItem(Module<?> module, String name, ILangEntry description, boolean def, BooleanSupplier isConfigEnabled) {
            super(module, name, description, new ModuleBooleanData(def));
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