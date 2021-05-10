package mekanism.common.content.gear;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleConfigData;
import mekanism.api.text.ILangEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

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

    public ITextComponent getDescription() {
        return description.translate();
    }

    public ModuleConfigData<TYPE> getData() {
        return data;
    }

    @Nonnull
    @Override
    public TYPE get() {
        return data.get();
    }

    @Override
    public void set(@Nonnull TYPE val) {
        set(val, null);
    }

    public void set(@Nonnull TYPE val, Consumer<ItemStack> callback) {
        Objects.requireNonNull(val, "Value cannot be null.");
        data.set(val);
        // validity checks
        for (Module<?> m : ModuleHelper.INSTANCE.loadAll(module.getContainer())) {
            // disable other exclusive modules
            if (name.equals(Module.ENABLED_KEY) && val == Boolean.TRUE && module.getData().isExclusive()) {
                if (m.getData().isExclusive() && m.getData() != module.getData()) {
                    m.setDisabledForce();
                }
            }
            // turn off mode change handling for other modules
            if (name.equals(Module.HANDLE_MODE_CHANGE_KEY) && val == Boolean.TRUE && module.handlesModeChange()) {
                if (m.handlesModeChange() && m.getData() != module.getData()) {
                    m.setModeHandlingDisabledForce();
                }
            }
        }
        // finally, save this specific module with the callback (to send a packet)
        module.save(callback);
    }

    public void read(CompoundNBT tag) {
        if (tag.contains(name)) {
            data.read(name, tag);
        }
    }

    public void write(CompoundNBT tag) {
        data.write(name, tag);
    }

    @Nonnull
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

        @Nonnull
        @Override
        public Boolean get() {
            return isConfigEnabled() && super.get();
        }

        public boolean isConfigEnabled() {
            return isConfigEnabled.getAsBoolean();
        }
    }
}