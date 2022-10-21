package mekanism.common.content.gear;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
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
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.ModuleConfigItem.DisableableModuleConfigItem;
import mekanism.common.util.ItemDataUtils;
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

    private final List<ModuleConfigItem<?>> configItems = new ArrayList<>();

    private final ModuleData<MODULE> data;
    private final ItemStack container;
    private final MODULE customModule;

    private ModuleConfigItem<Boolean> enabled;
    private ModuleConfigItem<Boolean> handleModeChange;
    private ModuleConfigItem<Boolean> renderHUD;

    private int installed = 1;

    public Module(ModuleData<MODULE> data, ItemStack container) {
        this.data = data;
        this.container = container;
        this.customModule = data.get();
    }

    @Override
    public MODULE getCustomInstance() {
        return customModule;
    }

    public void init() {
        enabled = addConfigItem(new ModuleConfigItem<>(this, ENABLED_KEY, MekanismLang.MODULE_ENABLED, new ModuleBooleanData(!data.isDisabledByDefault())) {
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
                if (value && (handlesModeChange() || data.getExclusiveFlags() != 0)) {
                    for (Module<?> m : ModuleHelper.INSTANCE.loadAll(getContainer())) {
                        if (data != m.getData()) {
                            // disable other exclusive modules
                            if (m.getData().isExclusive(data.getExclusiveFlags())) {
                                m.setDisabledForce(callback != null);
                            }
                            // turn off mode change handling for other modules
                            if (handlesModeChange() && m.handlesModeChange()) {
                                m.setModeHandlingDisabledForce();
                            }
                        }
                    }
                }
            }
        });
        if (data.handlesModeChange()) {
            handleModeChange = addConfigItem(new ModuleConfigItem<>(this, "handleModeChange", MekanismLang.MODULE_HANDLE_MODE_CHANGE, new ModuleBooleanData()) {
                @Override
                protected void checkValidity(@NotNull Boolean value, @Nullable Runnable callback) {
                    //If the mode change is being enabled, and we handle mode changes
                    if (value && handlesModeChange()) {
                        for (Module<?> m : ModuleHelper.INSTANCE.loadAll(getContainer())) {
                            // turn off mode change handling for other modules
                            if (data != m.getData() && m.handlesModeChange()) {
                                m.setModeHandlingDisabledForce();
                            }
                        }
                    }
                }
            });
        }
        if (data.rendersHUD()) {
            renderHUD = addConfigItem(new ModuleConfigItem<>(this, "renderHUD", MekanismLang.MODULE_RENDER_HUD, new ModuleBooleanData()));
        }
        customModule.init(this, new ModuleConfigItemCreator() {
            @Override
            public <TYPE> IModuleConfigItem<TYPE> createConfigItem(String name, ILangEntry description, ModuleConfigData<TYPE> data) {
                return addConfigItem(new ModuleConfigItem<>(Module.this, name, description, data));
            }

            @Override
            public IModuleConfigItem<Boolean> createDisableableConfigItem(String name, ILangEntry description, boolean def, BooleanSupplier isConfigEnabled) {
                return addConfigItem(new DisableableModuleConfigItem(Module.this, name, description, def, isConfigEnabled));
            }
        });
    }

    private <T> ModuleConfigItem<T> addConfigItem(ModuleConfigItem<T> item) {
        configItems.add(item);
        return item;
    }

    public void tick(Player player) {
        if (isEnabled()) {
            if (player.level.isClientSide()) {
                customModule.tickClient(this, player);
            } else {
                customModule.tickServer(this, player);
            }
        }
    }

    @Nullable
    @Override
    public IEnergyContainer getEnergyContainer() {
        return StorageUtils.getEnergyContainer(getContainer(), 0);
    }

    @Override
    public FloatingLong getContainerEnergy() {
        IEnergyContainer energyContainer = getEnergyContainer();
        return energyContainer == null ? FloatingLong.ZERO : energyContainer.getEnergy();
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

    public void read(CompoundTag nbt) {
        if (nbt.contains(NBTConstants.AMOUNT, Tag.TAG_INT)) {
            installed = nbt.getInt(NBTConstants.AMOUNT);
        }
        init();
        for (ModuleConfigItem<?> item : configItems) {
            item.read(nbt);
        }
    }

    /**
     * Save this module on the container ItemStack. Will create proper NBT structure if it does not yet exist.
     *
     * @param callback - will run after the NBT data is saved
     */
    public void save(@Nullable Runnable callback) {
        CompoundTag modulesTag = ItemDataUtils.getOrAddCompound(container, NBTConstants.MODULES);
        String registryName = data.getRegistryName().toString();
        CompoundTag nbt = modulesTag.getCompound(registryName);
        nbt.putInt(NBTConstants.AMOUNT, installed);
        for (ModuleConfigItem<?> item : configItems) {
            item.write(nbt);
        }
        //If the modules tag doesn't contain a match then we are on a new entry and have to make sure to add it
        modulesTag.put(registryName, nbt);

        if (callback != null) {
            callback.run();
        }
    }

    @Override
    public ModuleData<MODULE> getData() {
        return data;
    }

    @Override
    public int getInstalledCount() {
        return installed;
    }

    public void setInstalledCount(int installed) {
        this.installed = installed;
    }

    @Override
    public boolean isEnabled() {
        return enabled.get();
    }

    public void setDisabledForce(boolean hasCallback) {
        if (isEnabled()) {
            enabled.getData().set(false);
            save(null);
            //Manually call state changed as we bypassed the check we injected into set if we are on the server
            // we use the implementation detail about whether there was a callback to determine if it was on the
            // server or not
            if (!hasCallback) {
                customModule.onEnabledStateChange(this);
            }
        }
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    public List<ModuleConfigItem<?>> getConfigItems() {
        return configItems;
    }

    public void addHUDStrings(Player player, List<Component> list) {
        customModule.addHUDStrings(this, player, list::add);
    }

    public void addHUDElements(Player player, List<IHUDElement> list) {
        customModule.addHUDElements(this, player, list::add);
    }

    public void addRadialModes(@NotNull ItemStack stack, Consumer<NestedRadialMode> adder) {
        customModule.addRadialModes(this, stack, adder);
    }

    @Nullable
    public <M extends IRadialMode> M getMode(@NotNull ItemStack stack, RadialData<M> radialData) {
        return customModule.getMode(this, stack, radialData);
    }

    public <M extends IRadialMode> boolean setMode(@NotNull Player player, @NotNull ItemStack stack, RadialData<M> radialData, M mode) {
        return customModule.setMode(this, player, stack, radialData, mode);
    }

    @Nullable
    public Component getModeScrollComponent(ItemStack stack) {
        return customModule.getModeScrollComponent(this, stack);
    }

    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, boolean displayChangeMessage) {
        customModule.changeMode(this, player, stack, shift, displayChangeMessage);
    }

    @Override
    public boolean handlesModeChange() {
        return data.handlesModeChange() && handleModeChange.get() && (isEnabled() || customModule.canChangeModeWhenDisabled(this));
    }

    @Override
    public boolean handlesRadialModeChange() {
        return data.handlesModeChange() && (isEnabled() || customModule.canChangeRadialModeWhenDisabled(this));
    }

    public boolean handlesAnyModeChange() {
        if (data.handlesModeChange()) {
            return isEnabled() || handleModeChange.get() && customModule.canChangeModeWhenDisabled(this) || customModule.canChangeRadialModeWhenDisabled(this);
        }
        return false;
    }

    public void setModeHandlingDisabledForce() {
        if (data.handlesModeChange()) {
            handleModeChange.getData().set(false);
            save(null);
        }
    }

    @Override
    public boolean renderHUD() {
        return data.rendersHUD() && renderHUD.get();
    }

    public void onAdded(boolean first) {
        for (Module<?> module : ModuleHelper.INSTANCE.loadAll(getContainer())) {
            if (module.getData() != getData()) {
                // disable other exclusive modules if this is an exclusive module, as this one will now be active
                if (getData().isExclusive(module.getData().getExclusiveFlags())) {
                    module.setDisabledForce(false);
                }
                if (handlesModeChange() && module.handlesModeChange()) {
                    module.setModeHandlingDisabledForce();
                }
            }
        }
        customModule.onAdded(this, first);
    }

    public void onRemoved(boolean last) {
        customModule.onRemoved(this, last);
    }

    @Override
    public void displayModeChange(Player player, Component modeName, IHasTextComponent mode) {
        player.sendSystemMessage(MekanismUtils.logFormat(MekanismLang.MODULE_MODE_CHANGE.translate(modeName, EnumColor.INDIGO, mode)));
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
}
