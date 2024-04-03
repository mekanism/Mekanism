package mekanism.common.content.gear;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleConfigData;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.ModuleConfigItem.DisableableModuleConfigItem;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
@MethodsReturnNonnullByDefault
public final class Module<MODULE extends ICustomModule<MODULE>> implements IModule<MODULE> {

    public static final String ENABLED_KEY = "enabled";

    private final Map<String, ModuleConfigItem<?>> configItems = new LinkedHashMap<>();
    private final Collection<ModuleConfigItem<?>> configItemsView = Collections.unmodifiableCollection(configItems.values());

    private final ModuleData<MODULE> data;
    private final ModuleContainer container;
    private final MODULE customModule;

    private ModuleConfigItem<Boolean> enabled;
    private ModuleConfigItem<Boolean> handleModeChange;
    private ModuleConfigItem<Boolean> renderHUD;

    private int installed = 1;

    Module(ModuleData<MODULE> data, ModuleContainer container) {
        this.data = data;
        this.container = container;
        this.customModule = data.get();
    }

    @Override
    public MODULE getCustomInstance() {
        return customModule;
    }

    private void reInit() {
        //TODO: Improve how we handle re-init as this isn't the cleanest way of doing it
        CompoundTag configData = save();
        //Note: After saving the configs we need to clear the list of them before we can read and re-initialize them
        configItems.clear();
        readConfigItems(configData);
    }

    private void init() {
        enabled = addConfigItem(new ModuleConfigItem<>(this.data, ENABLED_KEY, MekanismLang.MODULE_ENABLED, new ModuleBooleanData(!data.isDisabledByDefault())) {
            @Override
            public void set(@NotNull Boolean val, @Nullable Runnable callback) {
                //Custom override of set to see if it changed and if so notify the custom module of that fact
                boolean wasEnabled = get();
                super.set(val, callback);
                //Note: This isn't the best but given we only call set for enabled from within Mekanism, we can use the
                // implementation detail that if the callback is null we are on the client side so if it isn't null then
                // we can assume it is server side
                if (callback == null && wasEnabled != get()) {
                    customModule.onEnabledStateChange(Module.this);
                }
            }

            @Override
            protected void checkValidity(@NotNull Boolean value, @Nullable Runnable callback) {
                //If enabled state of the module changes, recheck about mode changes and exclusivity flags
                // but only if this module can handle mode changes or has any exclusive flags set
                if (value && (handlesModeChange() || moduleType.getExclusiveFlags() != 0)) {
                    disableOtherExclusives(callback != null);
                }
            }
        });
        if (data.handlesModeChange()) {
            handleModeChange = addConfigItem(new ModuleConfigItem<>(this.data, "handleModeChange", MekanismLang.MODULE_HANDLE_MODE_CHANGE,
                  new ModuleBooleanData(!data.isModeChangeDisabledByDefault())) {
                @Override
                protected void checkValidity(@NotNull Boolean value, @Nullable Runnable callback) {
                    //If the mode change is being enabled, and we handle mode changes
                    if (value && handlesModeChange()) {
                        // turn off mode change handling for other modules
                        otherModules().filter(IModule::handlesModeChange)
                              .forEach(Module::setModeHandlingDisabledForce);
                    }
                }
            });
        }
        if (data.rendersHUD()) {
            renderHUD = addConfigItem(new ModuleConfigItem<>(this.data, "renderHUD", MekanismLang.MODULE_RENDER_HUD, new ModuleBooleanData()));
        }
        customModule.init(this, new ModuleConfigItemCreator() {
            @Override
            public <TYPE> IModuleConfigItem<TYPE> createConfigItem(String name, ILangEntry description, ModuleConfigData<TYPE> data) {
                return addConfigItem(new ModuleConfigItem<>(Module.this.data, name, description, data));
            }

            @Override
            public IModuleConfigItem<Boolean> createDisableableConfigItem(String name, ILangEntry description, boolean def, BooleanSupplier isConfigEnabled) {
                return addConfigItem(new DisableableModuleConfigItem(Module.this.data, name, description, def, isConfigEnabled));
            }
        });
    }

    private <T> ModuleConfigItem<T> addConfigItem(ModuleConfigItem<T> item) {
        configItems.put(item.getName(), item);
        return item;
    }

    public void tick(Player player) {
        if (isEnabled()) {
            if (player.level().isClientSide()) {
                customModule.tickClient(this, player);
            } else {
                customModule.tickServer(this, player);
            }
        }
    }

    @Nullable
    @Override
    public IEnergyContainer getEnergyContainer() {
        return StorageUtils.getEnergyContainer(getContainerStack(), 0);
    }

    @Override
    public FloatingLong getContainerEnergy() {
        IEnergyContainer energyContainer = getEnergyContainer();
        return energyContainer == null ? FloatingLong.ZERO : energyContainer.getEnergy();
    }

    @Override
    public boolean hasEnoughEnergy(FloatingLongSupplier energySupplier) {
        return hasEnoughEnergy(energySupplier.get());
    }

    @Override
    public boolean hasEnoughEnergy(FloatingLong cost) {
        return cost.isZero() || getContainerEnergy().greaterOrEqual(cost);
    }

    @Override
    public boolean canUseEnergy(LivingEntity wearer, FloatingLong energy) {
        //Note: This is subtly different than how useEnergy does it so that we can get to useEnergy when in creative
        return canUseEnergy(wearer, energy, false);
    }

    @Override
    public boolean canUseEnergy(LivingEntity wearer, FloatingLong energy, boolean ignoreCreative) {
        return canUseEnergy(wearer, getEnergyContainer(), energy, ignoreCreative);
    }

    @Override
    public boolean canUseEnergy(LivingEntity wearer, @Nullable IEnergyContainer energyContainer, FloatingLong energy, boolean ignoreCreative) {
        if (energyContainer != null && !wearer.isSpectator()) {
            //Don't check spectators in general
            if (!ignoreCreative || !(wearer instanceof Player player) || !player.isCreative()) {
                return energyContainer.extract(energy, Action.SIMULATE, AutomationType.MANUAL).equals(energy);
            }
        }
        return false;
    }

    @Override
    public FloatingLong useEnergy(LivingEntity wearer, FloatingLong energy) {
        return useEnergy(wearer, energy, true);
    }

    @Override
    public FloatingLong useEnergy(LivingEntity wearer, FloatingLong energy, boolean freeCreative) {
        return useEnergy(wearer, getEnergyContainer(), energy, freeCreative);
    }

    @Override
    public FloatingLong useEnergy(LivingEntity wearer, @Nullable IEnergyContainer energyContainer, FloatingLong energy, boolean freeCreative) {
        if (energyContainer != null) {
            //Use from spectators if this is called due to the various edge cases that exist for when things are calculated manually
            if (!freeCreative || !(wearer instanceof Player player) || MekanismUtils.isPlayingMode(player)) {
                return energyContainer.extract(energy, Action.EXECUTE, AutomationType.MANUAL);
            }
        }
        return FloatingLong.ZERO;
    }

    void read(CompoundTag nbt) {
        if (nbt.contains(NBTConstants.AMOUNT, Tag.TAG_INT)) {
            installed = nbt.getInt(NBTConstants.AMOUNT);
        }
        readConfigItems(nbt);
    }

    private void readConfigItems(CompoundTag nbt) {
        init();
        for (String key : nbt.getAllKeys()) {
            if (!key.equals(NBTConstants.AMOUNT)) {
                ModuleConfigItem<?> configItem = getConfigItem(key);
                if (configItem != null) {
                    configItem.getData().read(key, nbt);
                }
            }
        }
    }

    /**
     * Save this module to a compound tag with the proper structure.
     */
    CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(NBTConstants.AMOUNT, installed);
        for (Entry<String, ModuleConfigItem<?>> entry : configItems.entrySet()) {
            entry.getValue().getData().write(entry.getKey(), nbt);
        }
        return nbt;
    }

    @Override
    public ModuleData<MODULE> getData() {
        return data;
    }

    @Override
    public int getInstalledCount() {
        return installed;
    }

    @Override
    public boolean isEnabled() {
        return enabled.get();
    }

    public void setDisabledForce(boolean hasCallback) {
        if (isEnabled()) {
            enabled.getData().set(false);
            //Manually call state changed as we bypassed the check we injected into set if we are on the server
            // we use the implementation detail about whether there was a callback to determine if it was on the
            // server or not
            if (!hasCallback) {
                customModule.onEnabledStateChange(this);
            }
        }
    }

    private void disableOtherExclusives(boolean forceDisable) {
        int exclusiveFlags = data.getExclusiveFlags();
        otherModules().forEach(module -> {
            // disable other exclusive modules if this is an exclusive module, as this one will now be active
            if (module.getData().isExclusive(exclusiveFlags)) {
                module.setDisabledForce(forceDisable);
            }
            if (handlesModeChange() && module.handlesModeChange()) {
                module.setModeHandlingDisabledForce();
            }
        });
    }

    @Override
    public ItemStack getContainerStack() {
        return getContainer().container;
    }

    @Override
    public ModuleContainer getContainer() {
        return container;
    }

    /**
     * @return Other installed modules.
     */
    private Stream<Module<?>> otherModules() {
        return container.modules().stream()
              .filter(module -> module.getData() != getData());
    }

    @Nullable
    @Override
    public ModuleConfigItem<?> getConfigItem(String name) {
        return configItems.get(name);
    }

    @Nullable
    public <TYPE extends ModuleConfigData<?>> TYPE getConfigItemData(String name, Class<TYPE> dataType) {
        ModuleConfigItem<?> configItem = getConfigItem(name);
        if (configItem != null && dataType.isInstance(configItem.getData())) {
            return dataType.cast(configItem.getData());
        }
        return null;
    }

    public Collection<ModuleConfigItem<?>> getConfigItems() {
        return configItemsView;
    }

    public void addHUDStrings(Player player, List<Component> list) {
        customModule.addHUDStrings(this, player, list::add);
    }

    public void addHUDElements(Player player, List<IHUDElement> list) {
        customModule.addHUDElements(this, player, list::add);
    }

    @Override
    public boolean handlesModeChange() {
        return data.handlesModeChange() && handleModeChange.get() && (isEnabled() || customModule.canChangeModeWhenDisabled(this));
    }

    @Override
    public boolean handlesRadialModeChange() {
        return data.handlesModeChange() && (isEnabled() || customModule.canChangeRadialModeWhenDisabled(this));
    }

    @Override
    public boolean handlesAnyModeChange() {
        if (data.handlesModeChange()) {
            return isEnabled() || handleModeChange.get() && customModule.canChangeModeWhenDisabled(this) || customModule.canChangeRadialModeWhenDisabled(this);
        }
        return false;
    }

    public void setModeHandlingDisabledForce() {
        if (data.handlesModeChange()) {
            handleModeChange.getData().set(false);
        }
    }

    @Override
    public boolean renderHUD() {
        return data.rendersHUD() && renderHUD.get();
    }

    /**
     * @param wasFirst  if this is the first set of modules being added.
     * @param toInstall Number of modules to try and install.
     *
     * @return number installed
     */
    int add(boolean wasFirst, int toInstall) {
        toInstall = Math.min(toInstall, getData().getMaxStackSize());
        //Note: We don't have to save it and mutate the stack as attachments are auto saved
        disableOtherExclusives(false);
        if (!wasFirst || toInstall > 1) {
            //If we weren't the first module being added (or we are adding many at once) we need to reinitialize the config items on this module
            // the first module can be skipped when we are only installing a single item as the normal init process will have it in the correct state
            // We also need to increment the count if it wasn't the first module
            if (wasFirst) {
                installed = toInstall;
            } else {
                //Clamp based on how many modules we have room to add
                toInstall = Math.min(toInstall, getData().getMaxStackSize() - installed);
                installed += toInstall;
            }
            reInit();
        }
        customModule.onAdded(this, wasFirst);
        return toInstall;
    }

    /**
     * @return was last module.
     */
    boolean remove(int toRemove) {
        //Theoretically we are only calling this within the max stack size, but double check
        toRemove = Math.min(toRemove, getData().getMaxStackSize());
        installed -= toRemove;
        boolean wasLast = installed == 0;
        if (!wasLast) {
            //If we weren't the last module being removed we need to reinitialize the config items on this module
            // we can skip the last module as this object no longer matters and can be GCd once it is removed
            reInit();
        }
        //Note: We don't have to save it and mutate the stack as attachments are auto saved
        customModule.onRemoved(this, wasLast);
        return wasLast;
    }

    @Override
    public void displayModeChange(Player player, Component modeName, IHasTextComponent mode) {
        Component modeComponent = mode.getTextComponent();
        if (modeComponent.getStyle().getColor() != null) {
            player.sendSystemMessage(MekanismUtils.logFormat(MekanismLang.MODULE_MODE_CHANGE.translate(modeName, modeComponent)));
        } else {
            player.sendSystemMessage(MekanismUtils.logFormat(MekanismLang.MODULE_MODE_CHANGE.translate(modeName, EnumColor.INDIGO, modeComponent)));
        }
    }

    @Override
    public void toggleEnabled(Player player, Component modeName) {
        enabled.set(!isEnabled());
        Component message;
        if (isEnabled()) {
            message = MekanismLang.GENERIC_STORED.translate(modeName, EnumColor.BRIGHT_GREEN, MekanismLang.MODULE_ENABLED_LOWER);
        } else {
            message = MekanismLang.GENERIC_STORED.translate(modeName, EnumColor.DARK_RED, MekanismLang.MODULE_DISABLED_LOWER);
        }
        player.sendSystemMessage(MekanismUtils.logFormat(message));
    }

    @Override
    public boolean isCompatible(IModule<?> o) {
        if (o == this) {
            return true;
        }
        //Note: The data comparison is technically already validated by when the ModuleContainers are compared,
        // but we check it here anyway for consistency and to provide a more accurate return value if an addon calls this method
        if (installed != o.getInstalledCount() || data != o.getData() || !(o instanceof Module<?> other) || configItems.size() != other.configItems.size()) {
            return false;
        }
        for (ModuleConfigItem<?> configItem : getConfigItems()) {
            ModuleConfigItem<?> otherConfigItem = other.getConfigItem(configItem.getName());
            if (otherConfigItem == null) {
                return false;
            } else if (configItem != otherConfigItem && !configItem.getData().isCompatible(otherConfigItem.getData())) {
                return false;
            }
        }
        return true;
    }
}
