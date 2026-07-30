package mekanism.common.registries;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.robit.RobitSkin;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.api.upgrade.Upgrade;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.component.BlockData;
import mekanism.common.component.ConfigurationData;
import mekanism.common.component.FilterAware;
import mekanism.common.component.FormulaComponent;
import mekanism.common.component.FrequencyAware;
import mekanism.common.component.LockData;
import mekanism.common.component.OverflowAware;
import mekanism.common.component.PasteBucketConsumption;
import mekanism.common.component.SpecializedTransporter;
import mekanism.common.component.StabilizedChunks;
import mekanism.common.component.component.AttachedEjector;
import mekanism.common.component.component.AttachedSideConfig;
import mekanism.common.component.component.UpgradeAware;
import mekanism.common.component.containers.heat.HeatCapacitorData;
import mekanism.common.component.containers.resource.AttachedResources;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.qio.DriveContents;
import mekanism.common.component.qio.DriveMetadata;
import mekanism.common.component.qio.PortableDashboardContents;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleContainer;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.gear.ItemAtomicDisassembler.DisassemblerMode;
import mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode;
import mekanism.common.item.interfaces.IChemicalItem;
import mekanism.common.item.interfaces.IFluidItem;
import mekanism.common.item.interfaces.IFreeRunnerItem.FreeRunnerMode;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.lib.security.SecurityUtils;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.DataComponentDeferredRegister;
import mekanism.common.tier.BinTier;
import mekanism.common.tier.CableTier;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tier.PipeTier;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import mekanism.common.tile.laser.TileEntityLaserAmplifier.RedstoneOutput;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.ItemTooltipUtils;
import mekanism.common.util.ItemTooltipUtils.TooltipHideType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.TextUtils;
import mekanism.common.util.text.UpgradeDisplay;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterTooltipAppendersEvent;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public class MekanismDataComponents {

    private MekanismDataComponents() {
    }

    private static final DataComponentDeferredRegister DATA_COMPONENTS = new DataComponentDeferredRegister(Mekanism.MODID);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Component>> DESCRIPTION = DATA_COMPONENTS.registerComponent("description");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Unit>> DETAILS = DATA_COMPONENTS.registerUnit("details");

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ModuleContainer>> MODULE_CONTAINER = DATA_COMPONENTS.simple("module_container",
          builder -> builder.persistent(ModuleContainer.CODEC)
                .networkSynchronized(ModuleContainer.STREAM_CODEC)
                .cacheEncoding()
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Long>> ATTACHED_ENERGY = DATA_COMPONENTS.registerNonNegativeLong("energy");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedResources<ItemResource>>> ATTACHED_ITEMS = DATA_COMPONENTS
          .registerAttachedContents("items", LargeResourceStack.ITEM_HELPER);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedResources<FluidResource>>> ATTACHED_FLUIDS = DATA_COMPONENTS
          .registerAttachedContents("fluids", LargeResourceStack.FLUID_HELPER);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedResources<ChemicalResource>>> ATTACHED_CHEMICALS = DATA_COMPONENTS
          .registerAttachedContents("chemicals", LargeResourceStack.CHEMICAL_HELPER);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<HeatCapacitorData>> ATTACHED_HEAT = DATA_COMPONENTS.simple("heat",
          builder -> builder.persistent(HeatCapacitorData.CODEC)
                .networkSynchronized(HeatCapacitorData.STREAM_CODEC)
                .cacheEncoding()
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<BinTier>> BIN_TIER = DATA_COMPONENTS.simple("tier/bin",
          BinTier.CODEC, BinTier.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<CableTier>> CABLE_TIER = DATA_COMPONENTS.simple("tier/cable",
          CableTier.CODEC, CableTier.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ChemicalTankTier>> CHEMICAL_TANK_TIER = DATA_COMPONENTS.simple("tier/chemical_tank",
          ChemicalTankTier.CODEC, ChemicalTankTier.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ConductorTier>> CONDUCTOR_TIER = DATA_COMPONENTS.simple("tier/conductor",
          ConductorTier.CODEC, ConductorTier.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<EnergyCubeTier>> ENERGY_CUBE_TIER = DATA_COMPONENTS.simple("tier/energy_cube",
          EnergyCubeTier.CODEC, EnergyCubeTier.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FactoryTier>> FACTORY_TIER = DATA_COMPONENTS.simple("tier/factory",
          FactoryTier.CODEC, FactoryTier.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FactoryType>> FACTORY_TYPE = DATA_COMPONENTS.simple("factory_type",
          FactoryType.CODEC, FactoryType.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FluidTankTier>> FLUID_TANK_TIER = DATA_COMPONENTS.simple("tier/fluid_tank",
          FluidTankTier.CODEC, FluidTankTier.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<InductionCellTier>> INDUCTION_CELL_TIER = DATA_COMPONENTS.simple("tier/induction_cell",
          InductionCellTier.CODEC, InductionCellTier.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<InductionProviderTier>> INDUCTION_PROVIDER_TIER = DATA_COMPONENTS.simple("tier/induction_provider",
          InductionProviderTier.CODEC, InductionProviderTier.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<PipeTier>> PIPE_TIER = DATA_COMPONENTS.simple("tier/pipe",
          PipeTier.CODEC, PipeTier.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<TransporterTier>> TRANSPORTER_TIER = DATA_COMPONENTS.simple("tier/transporter",
          TransporterTier.CODEC, TransporterTier.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<SpecializedTransporter>> SPECIALIZED_TRANSPORTER = DATA_COMPONENTS.simple("specialized_transporter",
          SpecializedTransporter.CODEC, SpecializedTransporter.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<TubeTier>> TUBE_TIER = DATA_COMPONENTS.simple("tier/tube",
          TubeTier.CODEC, TubeTier.STREAM_CODEC
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<PasteBucketConsumption>> NUTRITIONAL_PASTE_CONSUMPTION = DATA_COMPONENTS.simple("paste_consumption",
          PasteBucketConsumption.CODEC, PasteBucketConsumption.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Unit>> WASTE_DECAY = DATA_COMPONENTS.registerUnit("waste_decay");

    //TODO - 26.2: Do we want to rename these components to: mode/<type>
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<DisassemblerMode>> DISASSEMBLER_MODE = DATA_COMPONENTS.simple("disassembler_mode",
          DisassemblerMode.CODEC, DisassemblerMode.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ConfiguratorMode>> CONFIGURATOR_MODE = DATA_COMPONENTS.simple("configurator_mode",
          ConfiguratorMode.CODEC, ConfiguratorMode.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FlamethrowerMode>> FLAMETHROWER_MODE = DATA_COMPONENTS.simple("flamethrower_mode",
          FlamethrowerMode.CODEC, FlamethrowerMode.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FreeRunnerMode>> FREE_RUNNER_MODE = DATA_COMPONENTS.simple("free_runner_mode",
          FreeRunnerMode.CODEC, FreeRunnerMode.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<JetpackMode>> JETPACK_MODE = DATA_COMPONENTS.simple("jetpack_mode",
          JetpackMode.CODEC, JetpackMode.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ContainerEditMode>> EDIT_MODE = DATA_COMPONENTS.simple("edit_mode",
          ContainerEditMode.CODEC, ContainerEditMode.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<GasMode>> DUMP_MODE = DATA_COMPONENTS.simple("dump_mode",
          GasMode.CODEC, GasMode.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<GasMode>> SECONDARY_DUMP_MODE = DATA_COMPONENTS.simple("secondary_dump_mode",
          GasMode.CODEC, GasMode.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<RedstoneControl>> REDSTONE_CONTROL = DATA_COMPONENTS.simple("redstone_control",
          RedstoneControl.CODEC, RedstoneControl.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<RedstoneOutput>> REDSTONE_OUTPUT = DATA_COMPONENTS.simple("redstone_output",
          RedstoneOutput.CODEC, RedstoneOutput.STREAM_CODEC
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Holder<Upgrade>>> UPGRADE_TYPE = DATA_COMPONENTS.simple("upgrade_type",
          Upgrade.CODEC, Upgrade.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Holder<ModuleData<?>>>> MODULE_TYPE = DATA_COMPONENTS.simple("module_type",
          MekanismRegistries.MODULES.holderByNameCodec(), ByteBufCodecs.holderRegistry(MekanismRegistries.Keys.MODULES)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> DEFAULT_MANUALLY_SELECTED = DATA_COMPONENTS.registerBoolean("default_manually_selected");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SCUBA_TANK_MODE = DATA_COMPONENTS.registerBoolean("scuba_tank_mode");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ELECTRIC_BOW_MODE = DATA_COMPONENTS.registerBoolean("electric_bow_mode");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> BUCKET_MODE = DATA_COMPONENTS.registerBoolean("bucket_mode");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ROTARY_MODE = DATA_COMPONENTS.registerBoolean("rotary_mode");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> AUTO = DATA_COMPONENTS.registerBoolean("auto");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SORTING = DATA_COMPONENTS.registerBoolean("sorting");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> EJECT = DATA_COMPONENTS.registerBoolean("eject");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> PULL = DATA_COMPONENTS.registerBoolean("pull");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ROUND_ROBIN = DATA_COMPONENTS.registerBoolean("round_robin");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SINGLE_ITEM = DATA_COMPONENTS.registerBoolean("single");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FUZZY = DATA_COMPONENTS.registerBoolean("fuzzy");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SILK_TOUCH = DATA_COMPONENTS.registerBoolean("silk_touch");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> INVERSE = DATA_COMPONENTS.registerBoolean("inverse");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> INVERSE_REQUIRES_REPLACE = DATA_COMPONENTS.registerBoolean("inverse_replace");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> INSERT_INTO_FREQUENCY = DATA_COMPONENTS.registerBoolean("insert_into_frequency");

    //TODO: Re-evaluate this transient component
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Unit>> FROM_RECIPE = DATA_COMPONENTS.simple("from_recipe",
          builder -> builder.networkSynchronized(Unit.STREAM_CODEC));

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RADIUS = DATA_COMPONENTS.registerNonNegativeInt("radius");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MIN_Y = DATA_COMPONENTS.registerInt("min_y");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MAX_Y = DATA_COMPONENTS.registerInt("max_y");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<OverflowAware>> OVERFLOW_AWARE = DATA_COMPONENTS.simple("overflow",
          builder -> builder.persistent(OverflowAware.CODEC)
                .networkSynchronized(OverflowAware.STREAM_CODEC)
                .cacheEncoding()
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Item>> REPLACE_STACK = DATA_COMPONENTS.simple("replace_stack",
          BuiltInRegistries.ITEM.byNameCodec(), ByteBufCodecs.registry(Registries.ITEM)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> DELAY = DATA_COMPONENTS.registerInt("delay");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MIN_THRESHOLD = DATA_COMPONENTS.registerNonNegativeInt("min_threshold");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MAX_THRESHOLD = DATA_COMPONENTS.registerNonNegativeInt("max_threshold");

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENERGY_USAGE = DATA_COMPONENTS.registerNonNegativeInt("energy_usage");

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Long>> LONG_AMOUNT = DATA_COMPONENTS.registerNonNegativeLong("long_amount");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ItemResource>> ITEM_TARGET = DATA_COMPONENTS.simple("item_target",
          ItemResource.OPTIONAL_CODEC.orElse(
                (Consumer<String>) error -> Mekanism.logger.error("Failed to load item target: {}", error),
                ItemResource.EMPTY
          ), ItemResource.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<DriveMetadata>> DRIVE_METADATA = DATA_COMPONENTS.simple("drive_metadata",
          DriveMetadata.CODEC, DriveMetadata.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<DriveContents>> DRIVE_CONTENTS = DATA_COMPONENTS.simple("drive_contents",
          builder -> builder.persistent(DriveContents.CODEC)
                .networkSynchronized(DriveContents.STREAM_CODEC)
                .cacheEncoding()
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<StabilizedChunks>> STABILIZER_CHUNKS = DATA_COMPONENTS.simple("stabilzer_chunks",
          StabilizedChunks.CODEC, StabilizedChunks.STREAM_CODEC
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Component>> ROBIT_NAME = DATA_COMPONENTS.registerComponent("robit_name");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ResourceKey<RobitSkin>>> ROBIT_SKIN = DATA_COMPONENTS.registerResourceKey("robit_skin", MekanismRegistries.Keys.ROBIT_SKINS);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<UUID>> PERSONAL_STORAGE_ID = DATA_COMPONENTS.registerUUID("storage_id");

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<LockData>> LOCK = DATA_COMPONENTS.simple("lock",
          LockData.CODEC, LockData.STREAM_CODEC
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FormulaComponent>> FORMULA_HOLDER = DATA_COMPONENTS.simple("formula",
          FormulaComponent.CODEC, FormulaComponent.STREAM_CODEC
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ConfigurationData>> CONFIGURATION_DATA = DATA_COMPONENTS.simple("configuration_data",
          ConfigurationData.CODEC, ConfigurationData.STREAM_CODEC
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<BlockData>> BLOCK_DATA = DATA_COMPONENTS.simple("block_data",
          BlockData.CODEC, BlockData.STREAM_CODEC
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<EnumColor>> COLOR = DATA_COMPONENTS.simple("color",
          EnumColor.CODEC, EnumColor.STREAM_CODEC
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<UUID>> OWNER = DATA_COMPONENTS.registerUUID("owner");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<SecurityMode>> SECURITY = DATA_COMPONENTS.simple("security",
          SecurityMode.CODEC, SecurityMode.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedEjector>> EJECTOR = DATA_COMPONENTS.simple("ejector",
          builder -> builder.persistent(AttachedEjector.CODEC)
                .networkSynchronized(AttachedEjector.STREAM_CODEC)
                .cacheEncoding()
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedSideConfig>> SIDE_CONFIG = DATA_COMPONENTS.simple("side_config",
          builder -> builder.persistent(AttachedSideConfig.CODEC)
                .networkSynchronized(AttachedSideConfig.STREAM_CODEC)
                .cacheEncoding()
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<UpgradeAware>> UPGRADES = DATA_COMPONENTS.simple("upgrades",
          builder -> builder.persistent(UpgradeAware.CODEC)
                .networkSynchronized(UpgradeAware.STREAM_CODEC)
                .cacheEncoding()
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FilterAware>> FILTER_AWARE = DATA_COMPONENTS.simple("filters",
          builder -> builder.persistent(FilterAware.CODEC)
                .networkSynchronized(FilterAware.STREAM_CODEC)
                .cacheEncoding()
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FrequencyAware<TeleporterFrequency>>> TELEPORTER_FREQUENCY = DATA_COMPONENTS.registerFrequencyAware("teleporter_frequency", () -> FrequencyTypes.TELEPORTER);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FrequencyAware<InventoryFrequency>>> INVENTORY_FREQUENCY = DATA_COMPONENTS.registerFrequencyAware("inventory_frequency", () -> FrequencyTypes.INVENTORY);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FrequencyAware<QIOFrequency>>> QIO_FREQUENCY = DATA_COMPONENTS.registerFrequencyAware("qio_frequency", () -> FrequencyTypes.QIO);

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <FREQ extends Frequency> DataComponentType<FrequencyAware<FREQ>> getFrequencyComponent(FrequencyType<FREQ> freq) {
        if (freq == FrequencyTypes.TELEPORTER) {
            return (DataComponentType) TELEPORTER_FREQUENCY.value();
        } else if (freq == FrequencyTypes.INVENTORY) {
            return (DataComponentType) INVENTORY_FREQUENCY.value();
        } else if (freq == FrequencyTypes.QIO) {
            return (DataComponentType) QIO_FREQUENCY.value();
        }
        return null;
    }

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<PortableDashboardContents>> QIO_DASHBOARD = DATA_COMPONENTS.simple("qio_dashboard",
          builder -> builder.persistent(PortableDashboardContents.CODEC)
                .networkSynchronized(PortableDashboardContents.STREAM_CODEC)
                .cacheEncoding()
    );

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
        modEventBus.addListener(RegisterTooltipAppendersEvent.class, MekanismDataComponents::registerTooltipAppenders);
    }

    private static void registerTooltipAppenders(RegisterTooltipAppendersEvent event) {
        event.registerComponentAppenderBefore(ROBIT_NAME, ROBIT_SKIN.get(), ItemTooltipUtils.createTrivialAppender(ROBIT_NAME, name ->
              MekanismLang.ROBIT_NAME.translateColored(EnumColor.INDIGO, EnumColor.GRAY, name)
        ));
        event.registerComponentAppenderBefore(ROBIT_SKIN, OWNER.get(), ItemTooltipUtils.createTrivialAppender(ROBIT_SKIN, skin ->
              MekanismLang.ROBIT_SKIN.translateColored(EnumColor.INDIGO, EnumColor.GRAY, RobitSkin.getTranslatedName(skin))
        ));
        event.registerComponentAppenderBefore(OWNER, SECURITY.get(), ItemTooltipUtils.createSimpleAppender(OWNER, (_, owner, _, _, player, _, builder) ->
              builder.accept(OwnerDisplay.of(player, owner).getTextComponent())));
        event.registerComponentAppenderBeforeAll(SECURITY, ItemTooltipUtils.createSimpleAppender(SECURITY, (stack, security, context, _, player, _, builder) -> {
            if (stack.is(MekanismBlocks.SECURITY_DESK.getItemHolder())) {
                //Note: We manually override this as we don't want to display the security mode for the security desk as while it technically
                // has one in reality it is always private
                return;
            }
            //TODO - 26.2: Re-evaluate this and how we bypass the capabilities for it
            boolean override = false;
            if (MekanismConfig.general.allowProtection.get()) {
                UUID owner = stack.get(OWNER);
                if (owner != null) {
                    Level level = context.level();
                    if (level == null && player != null) {
                        level = player.level();
                    }
                    //If we can't determine if the tooltip is being gotten on the client side, just assume it is
                    SecurityData data = SecurityUtils.get().getData(owner, level == null || level.isClientSide());
                    if (data.override() && ISecurityUtils.INSTANCE.moreRestrictive(security, data.mode())) {
                        //If our frequency's data is set to override, and it is more restrictive than the current mode,
                        // return the data for our frequency
                        security = data.mode();
                        override = true;
                    }
                }
            } else {
                security = SecurityMode.PUBLIC;
            }
            builder.accept(MekanismLang.SECURITY.translateColored(EnumColor.GRAY, security));
            if (override) {
                builder.accept(MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED));
            }
        }));

        event.registerComponentAppenderAfter(INVENTORY_FREQUENCY, DataComponents.MAP_ID, ItemTooltipUtils.createSimpleAppender(INVENTORY_FREQUENCY, FrequencyAware.TOOLTIP_PROVIDER));
        event.registerComponentAppenderAfter(TELEPORTER_FREQUENCY, INVENTORY_FREQUENCY.get(), ItemTooltipUtils.createSimpleAppender(TELEPORTER_FREQUENCY, FrequencyAware.TOOLTIP_PROVIDER));
        event.registerComponentAppenderAfter(QIO_FREQUENCY, TELEPORTER_FREQUENCY.get(), ItemTooltipUtils.createSimpleAppender(QIO_FREQUENCY, FrequencyAware.TOOLTIP_PROVIDER));

        event.registerComponentAppenderAfter(BLOCK_DATA, DataComponents.BLOCK_ENTITY_DATA, ItemTooltipUtils.createComponentAppender(BLOCK_DATA));

        event.registerComponentAppenderAfter(MODULE_CONTAINER, DataComponents.ENCHANTMENTS, ItemTooltipUtils.createSimpleAppender(MODULE_CONTAINER, (_, container, context, _, player, flag, builder) -> {
            if (ItemTooltipUtils.shouldDisplayDetails(context, player, flag)) {
                Collection<Module<?>> modules = container.modules();
                if (modules.isEmpty()) {
                    builder.accept(MekanismLang.MODULE_INSTALLED_NONE.translateColored(EnumColor.DARK_RED));
                } else {
                    builder.accept(MekanismLang.MODULE_INSTALLED_LIST.translateColored(EnumColor.BRIGHT_GREEN));
                    for (IModule<?> module : modules) {
                        ModuleData<?> data = module.getUntypedData();
                        Component line;
                        if (module.getInstalledCount() > 1) {
                            Component amount = MekanismLang.GENERIC_FRACTION.translate(module.getInstalledCount(), data.getMaxStackSize());
                            line = MekanismLang.GENERIC_TRANSFER.translate(data, amount);
                        } else {
                            line = MekanismLang.GENERIC_LIST.translate(data);
                        }
                        builder.accept(line);
                    }
                }
            } else {
                builder.accept(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
            }
        }));
        event.registerComponentAppenderAfter(MODULE_TYPE, MODULE_CONTAINER.get(), ItemTooltipUtils.createSimpleAppender(MODULE_TYPE, (_, moduleType, context, _, player, flag, builder) -> {
            ModuleData<?> data = moduleType.value();
            builder.accept(MekanismLang.TOOLTIP_MODULE_TYPE.translateColored(EnumColor.PURPLE, EnumColor.DARK_AQUA, data));
            builder.accept(MekanismLang.MODULE_STACKABLE.translateColored(EnumColor.GRAY, EnumColor.AQUA, data.getMaxStackSize()));
            builder.accept(TextComponentUtil.translate(data.getDescriptionTranslationKey()));
            if (ItemTooltipUtils.shouldDisplayDetails(context, player, flag)) {
                builder.accept(MekanismLang.MODULE_SUPPORTED.translateColored(EnumColor.BRIGHT_GREEN));
                for (Item item : IModuleHelper.INSTANCE.getSupportedItems(moduleType)) {
                    builder.accept(MekanismLang.GENERIC_LIST.translate(item.getName(item.getDefaultInstance())));
                }
                Set<ModuleData<?>> conflicting = IModuleHelper.INSTANCE.getConflicting(moduleType);
                if (!conflicting.isEmpty()) {
                    builder.accept(MekanismLang.MODULE_CONFLICTING.translateColored(EnumColor.RED));
                    for (ModuleData<?> module : conflicting) {
                        builder.accept(MekanismLang.GENERIC_LIST.translate(module));
                    }
                }
            } else {
                builder.accept(MekanismLang.HOLD_FOR_SUPPORTED_ITEMS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
            }
        }));
        event.registerComponentAppenderAfter(UPGRADES, MODULE_TYPE.get(), ItemTooltipUtils.createSimpleAppender(UPGRADES, (_, upgradeAware, _, _, _, _, builder) -> {
            for (ObjectIterator<Object2IntMap.Entry<Holder<Upgrade>>> iterator = Object2IntMaps.fastIterator(upgradeAware.upgrades()); iterator.hasNext(); ) {
                Object2IntMap.Entry<Holder<Upgrade>> entry = iterator.next();
                builder.accept(UpgradeDisplay.of(entry.getKey().value(), entry.getIntValue()).getTextComponent());
            }
        }));
        event.registerComponentAppenderAfter(UPGRADE_TYPE, UPGRADES.get(), ItemTooltipUtils.createSimpleAppender(UPGRADE_TYPE, (_, upgradeType, _, _, _, _, builder) -> {
            Upgrade upgrade = upgradeType.value();
            builder.accept(MekanismLang.TOOLTIP_UPGRADE_TYPE.translateColored(EnumColor.PURPLE, upgrade.textColor(), upgrade));
            builder.accept(MekanismLang.TOOLTIP_UPGRADE_MAX_INSTALLED.translateColored(EnumColor.GRAY, EnumColor.AQUA, upgrade.max()));
            builder.accept(upgrade.description());
        }));

        event.registerComponentAppenderAfter(CONFIGURATION_DATA, DataComponents.CONTAINER, ItemTooltipUtils.createComponentAppender(CONFIGURATION_DATA));
        event.registerComponentAppenderAfter(DRIVE_METADATA, CONFIGURATION_DATA.get(), ItemTooltipUtils.createComponentAppender(DRIVE_METADATA));
        event.registerComponentAppenderAfter(FACTORY_TYPE, DRIVE_METADATA.get(), ItemTooltipUtils.createComponentAppender(FACTORY_TYPE));
        event.registerComponentAppenderAfter(FORMULA_HOLDER, FACTORY_TYPE.get(), ItemTooltipUtils.createComponentAppender(FORMULA_HOLDER));
        //Transmitters
        event.registerComponentAppenderAfter(CABLE_TIER, FORMULA_HOLDER.get(), ItemTooltipUtils.createComponentAppender(CABLE_TIER));
        event.registerComponentAppenderAfter(CONDUCTOR_TIER, CABLE_TIER.get(), ItemTooltipUtils.createComponentAppender(CONDUCTOR_TIER));
        event.registerComponentAppenderAfter(PIPE_TIER, CONDUCTOR_TIER.get(), ItemTooltipUtils.createComponentAppender(PIPE_TIER));
        event.registerComponentAppenderAfter(TUBE_TIER, PIPE_TIER.get(), ItemTooltipUtils.createComponentAppender(TUBE_TIER));
        event.registerComponentAppenderAfter(TRANSPORTER_TIER, TUBE_TIER.get(), ItemTooltipUtils.createComponentAppender(TRANSPORTER_TIER));
        event.registerComponentAppenderAfter(SPECIALIZED_TRANSPORTER, TRANSPORTER_TIER.get(), ItemTooltipUtils.createComponentAppender(SPECIALIZED_TRANSPORTER));

        event.registerComponentAppenderAfter(INDUCTION_PROVIDER_TIER, SPECIALIZED_TRANSPORTER.get(), ItemTooltipUtils.createComponentAppender(INDUCTION_PROVIDER_TIER));
        event.registerComponentAppenderAfter(INDUCTION_CELL_TIER, INDUCTION_PROVIDER_TIER.get(), ItemTooltipUtils.createComponentAppender(INDUCTION_CELL_TIER));
        event.registerComponentAppenderAfter(ATTACHED_ENERGY, INDUCTION_CELL_TIER.get(), ItemTooltipUtils.createSimpleAppender(ATTACHED_ENERGY, (stack, energy, _, _, _, _, builder) -> {
            EnergyHandler energyHandler = ContainerType.ENERGY.getCapOrUnexposed(ItemAccessUtils.sideEffectFreeAccess(stack));
            EnergyDisplay display = energyHandler == null ? EnergyDisplay.of(energy) : EnergyDisplay.of(energyHandler);
            builder.accept(MekanismLang.STORED_ENERGY.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY, display));
        }));
        event.registerComponentAppenderAfter(ENERGY_CUBE_TIER, ATTACHED_ENERGY.get(), ItemTooltipUtils.createComponentAppender(ENERGY_CUBE_TIER));

        event.registerComponentAppenderAfter(WASTE_DECAY, ENERGY_CUBE_TIER.get(), ItemTooltipUtils.createSimpleAppender(WASTE_DECAY, (_, _, _, _, _, _, builder) -> {
            builder.accept(MekanismLang.CAPACITY_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(MekanismConfig.general.radioactiveWasteBarrelMaxChemical.get())));
            int ticks = MekanismConfig.general.radioactiveWasteBarrelProcessTicks.get();
            int decayAmount = MekanismConfig.general.radioactiveWasteBarrelDecayAmount.get();
            if (decayAmount == 0 || ticks == 1) {
                builder.accept(MekanismLang.WASTE_BARREL_DECAY_RATE.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(decayAmount)));
            } else {
                //Show decay rate to four decimals with no trailing zeros (but without decimals if it divides evenly)
                builder.accept(MekanismLang.WASTE_BARREL_DECAY_RATE.translateColored(EnumColor.INDIGO, EnumColor.GRAY,
                      TextUtils.format(UnitDisplayUtils.roundDecimals(decayAmount / (double) ticks, 4))));
                builder.accept(MekanismLang.WASTE_BARREL_DECAY_RATE_ACTUAL.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(decayAmount),
                      EnumColor.GRAY, TextUtils.format(ticks)));
            }
        }));
        event.registerComponentAppenderAfter(ATTACHED_CHEMICALS, WASTE_DECAY.get(), ItemTooltipUtils.createSimpleAppender(ATTACHED_CHEMICALS, (stack, chemicals, _, _, _, _, builder) -> {
            if (stack.getItem() instanceof IChemicalItem chemicalItem && !chemicals.hasNonEmptyContents()) {
                //Empty chemical items display their expected type
                builder.accept(MekanismLang.GENERIC_STORED_MB.translateColored(EnumColor.ORANGE, TextComponentUtil.translate(Chemical.getTranslationKey(chemicalItem.getChemicalType())), EnumColor.GRAY, 0));
            } else {
                boolean isGaugeDropper = stack.is(MekanismItems.GAUGE_DROPPER);
                if (isGaugeDropper && !chemicals.hasNonEmptyContents()) {
                    //builder.accept(MekanismLang.EMPTY.translateColored(EnumColor.DARK_RED));
                    AttachedResources<FluidResource> fluids = stack.get(ATTACHED_FLUIDS);
                    if (fluids != null && !fluids.hasNonEmptyContents()) {
                        //If there is no chemical nor fluid, render that the gauge dropper is empty
                        builder.accept(MekanismLang.EMPTY.translateColored(EnumColor.DARK_RED));
                    }
                    //If the chemicals are empty, but the fluids aren't, let the fluid component handle rendering what is stored
                } else {
                    //TODO: Improve the check for here and in attached fluids for merged tanks?
                    ContainerType.CHEMICAL.addStoredResource(chemicals, builder, MekanismLang.NO_CHEMICAL, EnumColor.ORANGE, stack.get(CHEMICAL_TANK_TIER),
                          isGaugeDropper ? MekanismLang.CHEMICAL : null, !isGaugeDropper && stack.has(ATTACHED_FLUIDS), MekanismLang.STORED_CHEMICALS);
                }
            }
        }));
        event.registerComponentAppenderAfter(CHEMICAL_TANK_TIER, ATTACHED_CHEMICALS.get(), ItemTooltipUtils.createComponentAppender(CHEMICAL_TANK_TIER));

        event.registerComponentAppenderAfter(ATTACHED_FLUIDS, CHEMICAL_TANK_TIER.get(), ItemTooltipUtils.createSimpleAppender(ATTACHED_FLUIDS, (stack, fluids, _, _, _, _, builder) -> {
            if (stack.getItem() instanceof IFluidItem fluidItem && !fluids.hasNonEmptyContents()) {
                //Empty fluid items (such as canteens) have custom rendering for their tooltips to show it as the type it is able to store
                builder.accept(MekanismLang.GENERIC_STORED_MB.translateColored(EnumColor.PINK, FluidResource.of(fluidItem.getFluidType()), EnumColor.GRAY, 0));
            } else {
                boolean isGaugeDropper = stack.is(MekanismItems.GAUGE_DROPPER);
                if (isGaugeDropper && !fluids.hasNonEmptyContents()) {
                    //Let the chemical rendering handle displaying the empty gauge dropper tooltip if there is no fluid
                } else {
                    ContainerType.FLUID.addStoredResource(fluids, builder, MekanismLang.NO_FLUID_TOOLTIP, EnumColor.PINK, stack.get(FLUID_TANK_TIER),
                          isGaugeDropper ? MekanismLang.LIQUID : null, !isGaugeDropper && stack.has(ATTACHED_CHEMICALS), MekanismLang.STORED_FLUID);
                }
            }
        }));
        event.registerComponentAppenderAfter(FLUID_TANK_TIER, ATTACHED_FLUIDS.get(), ItemTooltipUtils.createComponentAppender(FLUID_TANK_TIER));

        event.registerComponentAppenderAfter(LOCK, FLUID_TANK_TIER.get(), ItemTooltipUtils.createComponentAppender(LOCK));
        event.registerComponentAppenderAfter(ATTACHED_ITEMS, LOCK.get(), ItemTooltipUtils.createSimpleAppender(ATTACHED_ITEMS, (stack, items, _, _, _, _, builder) -> {
            BinTier binTier = stack.get(BIN_TIER);
            if (binTier == null) {
                builder.accept(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(items.hasNonEmptyContents(), true)));
            } else {
                if (items.hasNonEmptyContents()) {
                    //Note: We ignore if it has multiple stacks attached, as we assume it does not
                    LargeResourceStack<ItemResource> contents = items.get(0);
                    builder.accept(MekanismLang.STORING.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY, contents.resource()));
                    if (binTier.isCreative()) {
                        builder.accept(MekanismLang.ITEM_AMOUNT.translateColored(EnumColor.PURPLE, EnumColor.GRAY, MekanismLang.INFINITE));
                    } else {
                        builder.accept(MekanismLang.ITEM_AMOUNT.translateColored(EnumColor.PURPLE, EnumColor.GRAY, TextUtils.format(contents.amount())));
                    }
                } else {
                    builder.accept(MekanismLang.EMPTY.translateColored(EnumColor.DARK_RED));
                }
            }
        }));
        event.registerComponentAppenderAfter(BIN_TIER, ATTACHED_ITEMS.get(), ItemTooltipUtils.createComponentAppender(BIN_TIER));

         //Modes
        event.registerComponentAppenderBefore(BUCKET_MODE, CONFIGURATOR_MODE.get(), ItemTooltipUtils.createTrivialAppender(BUCKET_MODE, mode ->
              MekanismLang.BUCKET_MODE.translateColored(EnumColor.INDIGO, YesNo.of(mode, true))
        ));
        event.registerComponentAppenderBefore(CONFIGURATOR_MODE, DISASSEMBLER_MODE.get(), ItemTooltipUtils.createComponentAppender(CONFIGURATOR_MODE));
        event.registerComponentAppenderBefore(DISASSEMBLER_MODE, ELECTRIC_BOW_MODE.get(), ItemTooltipUtils.createComponentAppender(DISASSEMBLER_MODE));
        event.registerComponentAppenderBefore(ELECTRIC_BOW_MODE, FLAMETHROWER_MODE.get(), ItemTooltipUtils.createTrivialAppender(ELECTRIC_BOW_MODE, mode ->
              MekanismLang.FIRE_MODE.translateColored(EnumColor.PINK, OnOff.of(mode))
        ));
        event.registerComponentAppenderBefore(FLAMETHROWER_MODE, FREE_RUNNER_MODE.get(), ItemTooltipUtils.createComponentAppender(FLAMETHROWER_MODE));
        event.registerComponentAppenderBefore(FREE_RUNNER_MODE, JETPACK_MODE.get(), ItemTooltipUtils.createComponentAppender(FREE_RUNNER_MODE));
        event.registerComponentAppenderBefore(JETPACK_MODE, SCUBA_TANK_MODE.get(), ItemTooltipUtils.createComponentAppender(JETPACK_MODE));
        event.registerComponentAppenderBefore(SCUBA_TANK_MODE, DataComponents.ATTRIBUTE_MODIFIERS, ItemTooltipUtils.createTrivialAppender(SCUBA_TANK_MODE, mode ->
              MekanismLang.FLOWING.translateColored(EnumColor.GRAY, YesNo.of(mode, true))
        ));

        event.registerComponentAppenderAfterAll(DETAILS, ItemTooltipUtils.createSimpleAppender(DETAILS, TooltipHideType.DESCRIPTION, (_, _, context, _, player, flag, builder) -> {
            if (!ItemTooltipUtils.shouldDisplayDetails(context, player, flag)) {
                builder.accept(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
            }
        }));
        event.registerComponentAppenderAfter(DESCRIPTION, DETAILS.get(), ItemTooltipUtils.createSimpleAppender(DESCRIPTION, TooltipHideType.NONE, (_, description, context, _, player, flag, builder) -> {
            if (ItemTooltipUtils.shouldDisplayDescription(context, player, flag)) {
                builder.accept(description);
            } else {
                builder.accept(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, EnumColor.AQUA, MekanismKeyHandler.descriptionKey.getTranslatedKeyMessage()));
            }
        }));
    }
}