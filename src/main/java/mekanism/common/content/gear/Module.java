package mekanism.common.content.gear;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.MethodsAreNotNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.config.ModuleConfig;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.EnergyUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
@MethodsAreNotNullByDefault
public final class Module<MODULE extends ICustomModule<MODULE>> implements IModule<MODULE> {

    private record InstalledData(Holder<ModuleData<?>> holder, int installed) {

        private static final Codec<InstalledData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              MekanismAPI.MODULE_REGISTRY.holderByNameCodec().fieldOf(SerializationConstants.TYPE).forGetter(InstalledData::holder),
              ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.AMOUNT).forGetter(InstalledData::installed)
        ).apply(instance, InstalledData::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, InstalledData> STREAM_CODEC = StreamCodec.composite(
              ByteBufCodecs.holderRegistry(MekanismAPI.MODULE_REGISTRY_NAME), InstalledData::holder,
              ByteBufCodecs.VAR_INT, InstalledData::installed,
              InstalledData::new
        );

        public Module<?> create(List<ModuleConfig<?>> configs) {
            return new Module<>(holder, installed, configs);
        }

        public MapCodec<List<ModuleConfig<?>>> configCodecs() {
            ModuleData<?> data = holder.value();
            return data.configCodecs(installed).optionalFieldOf(SerializationConstants.CONFIG, data.defaultConfigs(installed));
        }

        public StreamCodec<RegistryFriendlyByteBuf, List<ModuleConfig<?>>> configStreamCodecs() {
            return holder.value().configStreamCodecs(installed);
        }
    }

    public static final Codec<Module<?>> CODEC = InstalledData.CODEC.dispatch(
          module -> new InstalledData(module.getDataHolder(), module.getInstalledCount()),
          installedData -> RecordCodecBuilder.mapCodec(instance -> instance.group(
                installedData.configCodecs().forGetter(Module::getConfigs)
          ).apply(instance, installedData::create))
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, Module<?>> STREAM_CODEC = InstalledData.STREAM_CODEC.dispatch(
          module -> new InstalledData(module.getDataHolder(), module.getInstalledCount()),
          installedData -> installedData.configStreamCodecs().map(installedData::create, Module::getConfigs)
    );

    private final Map<Identifier, ModuleConfig<?>> configItemsByName = new HashMap<>();
    private final List<ModuleConfig<?>> configItems;

    private final Holder<ModuleData<?>> holder;
    private final MODULE customModule;
    private final boolean enabled;
    private final boolean handleModeChange;
    private final boolean renderHUD;
    private final int installed;

    Module(Holder<ModuleData<?>> holder, int installed) {
        this(holder, installed, holder.value().defaultConfigs(installed));
    }

    private Module(Holder<ModuleData<?>> holder, int installed, List<ModuleConfig<?>> configItems) {
        this.holder = holder;
        this.installed = installed;
        this.configItems = configItems;
        for (ModuleConfig<?> configItem : this.configItems) {
            configItemsByName.put(configItem.name(), configItem);
        }
        this.enabled = this.getBooleanConfigOrFalse(ModuleConfig.ENABLED_KEY);
        this.handleModeChange = getBooleanConfigOrFalse(ModuleConfig.HANDLES_MODE_CHANGE_KEY);
        this.renderHUD = getBooleanConfigOrFalse(ModuleConfig.RENDER_HUD_KEY);
        this.customModule = getData().create(this);
    }

    @Override
    public MODULE getCustomInstance() {
        return customModule;
    }

    public void tick(ItemAccess itemAccess, Player player, TransactionContext transaction) {
        if (isEnabled()) {
            if (player.level().isClientSide()) {
                customModule.tickClient(this, itemAccess, player, transaction);
            } else {
                customModule.tickServer(this, itemAccess, player, transaction);
            }
        }
    }

    @Nullable
    @Override
    public EnergyHandler getEnergyHandler(ItemAccess itemAccess) {
        return Capabilities.ENERGY.getCapability(itemAccess);
    }

    @Override
    public boolean hasEnoughEnergy(ItemAccess itemAccess, int energy) {
        if (energy == 0) {
            return true;
        }
        EnergyHandler energyHandler = getEnergyHandler(itemAccess);
        return energyHandler != null && energyHandler.getAmountAsInt() >= energy;
    }

    @Override
    public boolean hasEnoughEnergy(@Nullable LivingEntity wearer, ItemAccess itemAccess, int energy, @Nullable TransactionContext transaction, boolean freeCreative) {
        try (Transaction simulation = Transaction.open(transaction)) {
            return useAllEnergy(wearer, itemAccess, energy, simulation, freeCreative);
        }
    }

    @Override
    public int getEnergyRateLimit(@Nullable LivingEntity wearer, ItemAccess itemAccess, int energyUsage, int rate, @Nullable TransactionContext transaction, boolean freeCreative) {
        if (rate == 0) {
            return 0;
        } else if (freeCreative && wearer instanceof Player player && !MekanismUtils.isPlayingMode(player)) {
            //Energy usage doesn't lower the usage rate
            return rate;
        }
        EnergyHandler energyHandler = getEnergyHandler(itemAccess);
        if (energyHandler == null) {
            return 0;
        }
        try (Transaction simulation = Transaction.open(transaction)) {
            //Calculate the max rate based on how much energy is available and can be extracted
            return EnergyUtils.extractManual(energyHandler, MathUtils.multiplyClamped(rate, energyUsage), simulation) / energyUsage;
        }
    }

    @Override
    public boolean useAllEnergy(@Nullable LivingEntity wearer, ItemAccess itemAccess, int energy, @Nullable TransactionContext transaction, boolean freeCreative) {
        if (energy == 0) {
            //If there is no energy requirement skip looking up the energy handler
            return true;
        } else if (freeCreative && wearer instanceof Player player && !MekanismUtils.isPlayingMode(player)) {
            return true;
        }
        EnergyHandler energyHandler = getEnergyHandler(itemAccess);
        if (energyHandler == null) {
            return false;
        }
        try (Transaction subTransaction = Transaction.open(transaction)) {
            if (EnergyUtils.extractManual(energyHandler, energy, subTransaction) == energy) {
                subTransaction.commit();
                return true;
            }
            return false;
        }
    }

    @Override
    public ModuleData<?> getUntypedData() {
        return holder.value();
    }

    @Override
    public Holder<ModuleData<?>> getDataHolder() {
        return holder;
    }

    @Nullable
    @Override
    public <TYPE> ModuleConfig<TYPE> getConfig(Identifier name) {
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

    Module<MODULE> withReplacedInstallCount(int installed) {
        List<ModuleConfig<?>> moduleConfigs = getUntypedData().defaultConfigs(installed);
        List<ModuleConfig<?>> copiedConfigs = new ArrayList<>(moduleConfigs.size());
        for (ModuleConfig<?> moduleConfig : moduleConfigs) {
            Identifier name = moduleConfig.name();
            ModuleConfig<?> existingConfig = configItemsByName.get(name);
            if (existingConfig == null) {
                copiedConfigs.add(moduleConfig);
            } else if (moduleConfig.getClass() == existingConfig.getClass()) {
                copiedConfigs.add(configWithValue(moduleConfig, existingConfig));
            } else {
                throw new IllegalStateException("Expected module config " + name + " to have the same class regardless of installed count.");
            }
        }
        return new Module<>(holder, installed, List.copyOf(copiedConfigs));
    }

    @SuppressWarnings("unchecked")
    private <CONFIG> ModuleConfig<CONFIG> configWithValue(ModuleConfig<CONFIG> defaultConfig, ModuleConfig<?> existing) {
        try {
            return defaultConfig.with(((ModuleConfig<CONFIG>) existing).get());
        } catch (IllegalArgumentException e) {
            //If the existing value isn't valid for the new config, fallback to the default value
            return defaultConfig;
        }
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
                return new Module<>(holder, installed, List.copyOf(copiedConfigs));
            }
        }
        throw new IllegalStateException("Could not find an existing config with name: " + config.name());
    }

    public <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDStrings(Player player, IModuleContainer moduleContainer, ITEM instance, List<Component> list) {
        if (renderHUD) {
            customModule.addHUDStrings(this, moduleContainer, instance, player, list::add);
        }
    }

    public <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDElements(Player player, IModuleContainer moduleContainer, ITEM instance, List<IHUDElement> list) {
        if (renderHUD) {
            customModule.addHUDElements(this, moduleContainer, instance, player, list::add);
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
            player.sendOverlayMessage(MekanismLang.MODULE_MODE_CHANGE.translate(modeName, modeComponent));
        } else {
            player.sendOverlayMessage(MekanismLang.MODULE_MODE_CHANGE.translate(modeName, EnumColor.INDIGO, modeComponent));
        }
    }

    @Override
    public void replaceModuleConfig(HolderLookup.Provider provider, ItemAccess itemAccess, @Nullable TransactionContext transaction, ModuleConfig<?> config) {
        ModuleContainer moduleContainer = ModuleHelper.get().getModuleContainer(itemAccess.getResource());
        if (moduleContainer != null) {
            moduleContainer.replaceModuleConfig(provider, itemAccess, transaction, holder, config, false);
        } else {
            Mekanism.logger.warn("Tried to change mode for module: {}, but {} was not a module container.", holder.getRegisteredName(), itemAccess.getResource());
        }
    }

    @Override
    public void toggleEnabled(ItemAccess itemAccess, Player player, Component modeName, @Nullable TransactionContext transaction) {
        Component message;
        if (enabled) {//Going from enabled to disabled
            message = MekanismLang.GENERIC_STORED.translate(modeName, EnumColor.DARK_RED, MekanismLang.MODULE_DISABLED_LOWER);
        } else {//Going from disabled to enabled
            message = MekanismLang.GENERIC_STORED.translate(modeName, EnumColor.BRIGHT_GREEN, MekanismLang.MODULE_ENABLED_LOWER);
        }
        player.sendOverlayMessage(message);
        ModuleContainer moduleContainer = ModuleHelper.get().getModuleContainer(itemAccess.getResource());
        if (moduleContainer != null) {
            moduleContainer.toggleEnabled(player.registryAccess(), itemAccess, holder, transaction);
        } else {
            Mekanism.logger.warn("Tried to toggle module: {}, but {} was not a module container.", holder.getRegisteredName(), itemAccess.getResource());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Module<?> module = (Module<?>) o;
        return installed == module.installed && getUntypedData() == module.getUntypedData() && configItems.equals(module.configItems);
    }

    @Override
    public int hashCode() {
        int result = configItems.hashCode();
        result = 31 * result + getUntypedData().hashCode();
        result = 31 * result + installed;
        return result;
    }
}
