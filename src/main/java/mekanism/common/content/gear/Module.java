package mekanism.common.content.gear;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.config.ModuleConfig;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
@MethodsReturnNonnullByDefault
public final class Module<MODULE extends ICustomModule<MODULE>> implements IModule<MODULE> {

    private record InstalledData<MODULE extends ICustomModule<MODULE>>(ModuleData<MODULE> data, int installed) {

        private static final Codec<InstalledData<?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              MekanismAPI.MODULE_REGISTRY.byNameCodec().fieldOf(NBTConstants.TYPE).forGetter(InstalledData::data),
              ExtraCodecs.POSITIVE_INT.fieldOf(NBTConstants.AMOUNT).forGetter(InstalledData::installed)
        ).apply(instance, InstalledData::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, InstalledData<?>> STREAM_CODEC = StreamCodec.composite(
              ByteBufCodecs.registry(MekanismAPI.MODULE_REGISTRY_NAME), InstalledData::data,
              ByteBufCodecs.VAR_INT, InstalledData::installed,
              InstalledData::new
        );

        public Module<MODULE> create(List<ModuleConfig<?>> configs) {
            return new Module<>(data, installed, configs);
        }

        public MapCodec<List<ModuleConfig<?>>> configCodecs() {
            return data.configCodecs(installed).optionalFieldOf(NBTConstants.CONFIG, data.defaultConfigs(installed));
        }

        public StreamCodec<RegistryFriendlyByteBuf, List<ModuleConfig<?>>> configStreamCodecs() {
            return data.configStreamCodecs(installed);
        }
    }

    public static final Codec<Module<?>> CODEC = InstalledData.CODEC.dispatch(
          module -> new InstalledData<>(module.getData(), module.getInstalledCount()),
          installedData -> RecordCodecBuilder.mapCodec(instance -> instance.group(
                installedData.configCodecs().forGetter(Module::getConfigs)
          ).apply(instance, installedData::create))
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, Module<?>> STREAM_CODEC = InstalledData.STREAM_CODEC.dispatch(
          module -> new InstalledData<>(module.getData(), module.getInstalledCount()),
          installedData -> installedData.configStreamCodecs().map(installedData::create, Module::getConfigs)
    );

    private final Map<String, ModuleConfig<?>> configItemsByName = new HashMap<>();
    private final List<ModuleConfig<?>> configItems;

    private final ModuleData<MODULE> data;
    private final MODULE customModule;
    private final boolean enabled;
    private final boolean handleModeChange;
    private final boolean renderHUD;
    private final int installed;

    Module(ModuleData<MODULE> data, int installed) {
        this(data, installed, data.defaultConfigs(installed));
    }

    Module(ModuleData<MODULE> data, int installed, List<ModuleConfig<?>> configItems) {
        this.data = data;
        this.installed = installed;
        this.configItems = configItems;
        for (ModuleConfig<?> configItem : this.configItems) {
            configItemsByName.put(configItem.name(), configItem);
        }
        this.enabled = this.getBooleanConfigOrFalse(ModuleConfig.ENABLED_KEY);
        this.handleModeChange = getBooleanConfigOrFalse(ModuleConfig.HANDLES_MODE_CHANGE_KEY);
        this.renderHUD = getBooleanConfigOrFalse(ModuleConfig.RENDER_HUD_KEY);
        this.customModule = data.create(this);
    }

    @Override
    public MODULE getCustomInstance() {
        return customModule;
    }

    public void tick(IModuleContainer moduleContainer, ItemStack stack, Player player) {
        if (isEnabled()) {
            if (player.level().isClientSide()) {
                customModule.tickClient(this, moduleContainer, stack, player);
            } else {
                customModule.tickServer(this, moduleContainer, stack, player);
            }
        }
    }

    @Nullable
    @Override
    public IEnergyContainer getEnergyContainer(ItemStack stack) {
        return StorageUtils.getEnergyContainer(stack, 0);
    }

    @Override
    public FloatingLong getContainerEnergy(ItemStack stack) {
        IEnergyContainer energyContainer = getEnergyContainer(stack);
        return energyContainer == null ? FloatingLong.ZERO : energyContainer.getEnergy();
    }

    @Override
    public boolean hasEnoughEnergy(ItemStack stack, FloatingLongSupplier energySupplier) {
        return hasEnoughEnergy(stack, energySupplier.get());
    }

    @Override
    public boolean hasEnoughEnergy(ItemStack stack, FloatingLong cost) {
        return cost.isZero() || getContainerEnergy(stack).greaterOrEqual(cost);
    }

    @Override
    public boolean canUseEnergy(LivingEntity wearer, ItemStack stack, FloatingLong energy) {
        //Note: This is subtly different than how useEnergy does it so that we can get to useEnergy when in creative
        return canUseEnergy(wearer, stack, energy, false);
    }

    @Override
    public boolean canUseEnergy(LivingEntity wearer, ItemStack stack, FloatingLong energy, boolean ignoreCreative) {
        return canUseEnergy(wearer, getEnergyContainer(stack), energy, ignoreCreative);
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
    public FloatingLong useEnergy(LivingEntity wearer, ItemStack stack, FloatingLong energy) {
        return useEnergy(wearer, stack, energy, true);
    }

    @Override
    public FloatingLong useEnergy(LivingEntity wearer, ItemStack stack, FloatingLong energy, boolean freeCreative) {
        return useEnergy(wearer, getEnergyContainer(stack), energy, freeCreative);
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

    @Override
    public ModuleData<MODULE> getData() {
        return data;
    }

    @Nullable
    @Override
    public <TYPE> ModuleConfig<TYPE> getConfig(String name) {
        //TODO - 1.20.5: Do we want to allow passing in the type to validate that the type is correct?
        return (ModuleConfig<TYPE>) configItemsByName.get(name);
    }

    public List<ModuleConfig<?>> getConfigs() {
        return configItems;
    }

    @Override
    public int getInstalledCount() {
        return installed;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    Module<MODULE> withReplacedInstallCount(HolderLookup.Provider provider, int installed) {
        RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
        //TODO - 1.20.5: Re-evaluate this
        CompoundTag tag = (CompoundTag) Module.CODEC.encodeStart(registryOps, this).getOrThrow();
        tag.putInt(NBTConstants.AMOUNT, installed);
        return (Module<MODULE>) Module.CODEC.decode(registryOps, tag).getOrThrow().getFirst();
    }

    Module<MODULE> withReplacedConfig(ModuleConfig<?> config) {
        return withReplacedConfig(config, false);
    }

    //throws IllegalArgumentException
    //throws IllegalStateException
    <CONFIG> Module<MODULE> withReplacedConfig(ModuleConfig<CONFIG> config, boolean fromPacket) {
        for (int i = 0; i < configItems.size(); i++) {
            ModuleConfig<?> storedConfig = configItems.get(i);
            if (storedConfig.name().equals(config.name())) {
                if (storedConfig.get().equals(config.get())) {
                    //Nothing changed
                    return this;
                } else if (fromPacket) {
                    //Note: This cast is theoretically not unsafe as when reading from the packet we validate
                    // that the type is what we expect it to be. To be safe though we double-check the classes
                    if (storedConfig.getClass() != config.getClass()) {
                        //Invalid, don't apply the change
                        throw new IllegalStateException("Config " + config.name() + "'s Class " + config.getClass().getSimpleName() + "  did not match " + storedConfig.getClass().getSimpleName());
                    }
                    //Ensure we sanitize it and that it actually has the correct range applied and the client
                    // didn't just lie about how many are installed in order to get a higher value set
                    config = ((ModuleConfig<CONFIG>) storedConfig).with(config.get());
                }
                List<ModuleConfig<?>> copiedConfigs = new ArrayList<>(configItems);
                copiedConfigs.set(i, config);
                return new Module<>(data, installed, List.copyOf(copiedConfigs));
            }
        }
        throw new IllegalStateException("Could not find an existing config with name: " + config.name());
    }

    public void addHUDStrings(Player player, IModuleContainer moduleContainer, ItemStack stack, List<Component> list) {
        if (renderHUD) {
            customModule.addHUDStrings(this, moduleContainer, stack, player, list::add);
        }
    }

    public void addHUDElements(Player player, IModuleContainer moduleContainer, ItemStack stack, List<IHUDElement> list) {
        if (renderHUD) {
            customModule.addHUDElements(this, moduleContainer, stack, player, list::add);
        }
    }

    boolean handlesModeChangeRaw() {
        return handleModeChange;
    }

    @Override
    public boolean handlesModeChange() {
        return handleModeChange && (isEnabled() || customModule.canChangeModeWhenDisabled(this));
    }

    @Override
    public boolean handlesRadialModeChange() {
        if (getConfig(ModuleConfig.HANDLES_MODE_CHANGE_KEY) == null) {
            return false;
        }
        return isEnabled() || customModule.canChangeRadialModeWhenDisabled(this);
    }

    @Override
    public boolean handlesAnyModeChange() {
        if (getConfig(ModuleConfig.HANDLES_MODE_CHANGE_KEY) == null) {
            return false;
        }
        return isEnabled() || handleModeChange && customModule.canChangeModeWhenDisabled(this) || customModule.canChangeRadialModeWhenDisabled(this);
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
    public void toggleEnabled(IModuleContainer moduleContainer, ItemStack stack, Player player, Component modeName) {
        Component message;
        if (enabled) {//Going from enabled to disabled
            message = MekanismLang.GENERIC_STORED.translate(modeName, EnumColor.DARK_RED, MekanismLang.MODULE_DISABLED_LOWER);
        } else {//Going from disabled to enabled
            message = MekanismLang.GENERIC_STORED.translate(modeName, EnumColor.BRIGHT_GREEN, MekanismLang.MODULE_ENABLED_LOWER);
        }
        player.sendSystemMessage(MekanismUtils.logFormat(message));
        ((ModuleContainer) moduleContainer).toggleEnabled(stack, data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Module<?> module = (Module<?>) o;
        return installed == module.installed && Objects.equals(configItems, module.configItems) && Objects.equals(data, module.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configItems, data, installed);
    }
}
