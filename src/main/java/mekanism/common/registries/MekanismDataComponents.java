package mekanism.common.registries;

import com.mojang.serialization.Codec;
import java.util.UUID;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.robit.RobitSkin;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.attachments.BlockData;
import mekanism.common.attachments.FilterAware;
import mekanism.common.attachments.FormulaAttachment;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.attachments.OverflowAware;
import mekanism.common.attachments.StabilizedChunks;
import mekanism.common.attachments.component.AttachedEjector;
import mekanism.common.attachments.component.AttachedSideConfig;
import mekanism.common.attachments.component.UpgradeAware;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedGasTanks;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedInfusionTanks;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedPigmentTanks;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedSlurryTanks;
import mekanism.common.attachments.containers.AttachedEnergyContainers;
import mekanism.common.attachments.containers.AttachedFluidTanks;
import mekanism.common.attachments.containers.AttachedHeatCapacitors;
import mekanism.common.attachments.containers.AttachedInventorySlots;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.qio.DriveContents;
import mekanism.common.attachments.qio.DriveMetadata;
import mekanism.common.attachments.qio.PortableDashboardContents;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.content.gear.ModuleContainer;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.gear.ItemAtomicDisassembler.DisassemblerMode;
import mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode;
import mekanism.common.item.gear.ItemFreeRunners.FreeRunnerMode;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.DataComponentDeferredRegister;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import mekanism.common.tile.laser.TileEntityLaserAmplifier.RedstoneOutput;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class MekanismDataComponents {//TODO - 1.20.5: Organize this class

    private MekanismDataComponents() {
    }

    public static final DataComponentDeferredRegister DATA_COMPONENTS = new DataComponentDeferredRegister(Mekanism.MODID);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ModuleContainer>> MODULE_CONTAINER = DATA_COMPONENTS.simple("module_container",
          builder -> builder.persistent(ModuleContainer.CODEC)
                .networkSynchronized(ModuleContainer.STREAM_CODEC)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<DisassemblerMode>> DISASSEMBLER_MODE = DATA_COMPONENTS.simple("disassembler_mode",
          builder -> builder.persistent(DisassemblerMode.CODEC)
                .networkSynchronized(DisassemblerMode.STREAM_CODEC)
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ConfiguratorMode>> CONFIGURATOR_MODE = DATA_COMPONENTS.simple("configurator_mode",
          builder -> builder.persistent(ConfiguratorMode.CODEC)
                .networkSynchronized(ConfiguratorMode.STREAM_CODEC)
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FlamethrowerMode>> FLAMETHROWER_MODE = DATA_COMPONENTS.simple("flamethrower_mode",
          builder -> builder.persistent(FlamethrowerMode.CODEC)
                .networkSynchronized(FlamethrowerMode.STREAM_CODEC)
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FreeRunnerMode>> FREE_RUNNER_MODE = DATA_COMPONENTS.simple("free_runner_mode",
          builder -> builder.persistent(FreeRunnerMode.CODEC)
                .networkSynchronized(FreeRunnerMode.STREAM_CODEC)
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<JetpackMode>> JETPACK_MODE = DATA_COMPONENTS.simple("jetpack_mode",
          builder -> builder.persistent(JetpackMode.CODEC)
                .networkSynchronized(JetpackMode.STREAM_CODEC)
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ContainerEditMode>> EDIT_MODE = DATA_COMPONENTS.simple("edit_mode",
          builder -> builder.persistent(ContainerEditMode.CODEC)
                .networkSynchronized(ContainerEditMode.STREAM_CODEC)
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<GasMode>> DUMP_MODE = DATA_COMPONENTS.simple("dump_mode",
          builder -> builder.persistent(GasMode.CODEC)
                .networkSynchronized(GasMode.STREAM_CODEC)
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<GasMode>> SECONDARY_DUMP_MODE = DATA_COMPONENTS.simple("secondary_dump_mode",
          builder -> builder.persistent(GasMode.CODEC)
                .networkSynchronized(GasMode.STREAM_CODEC)
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<RedstoneControl>> REDSTONE_CONTROL = DATA_COMPONENTS.simple("redstone_control",
          builder -> builder.persistent(RedstoneControl.CODEC)
                .networkSynchronized(RedstoneControl.STREAM_CODEC)
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<RedstoneOutput>> REDSTONE_OUTPUT = DATA_COMPONENTS.simple("redstone_output",
          builder -> builder.persistent(RedstoneOutput.CODEC)
                .networkSynchronized(RedstoneOutput.STREAM_CODEC)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SCUBA_TANK_MODE = DATA_COMPONENTS.registerBoolean("scuba_tank_mode", false);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ELECTRIC_BOW_MODE = DATA_COMPONENTS.registerBoolean("electric_bow_mode", false);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> BUCKET_MODE = DATA_COMPONENTS.registerBoolean("bucket_mode", false);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ROTARY_MODE = DATA_COMPONENTS.registerBoolean("rotary_mode", false);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> AUTO = DATA_COMPONENTS.registerBoolean("auto", false);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SORTING = DATA_COMPONENTS.registerBoolean("sorting", false);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> EJECT = DATA_COMPONENTS.registerBoolean("eject", false);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> PULL = DATA_COMPONENTS.registerBoolean("pull", false);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ROUND_ROBIN = DATA_COMPONENTS.registerBoolean("round_robin", false);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SINGLE_ITEM = DATA_COMPONENTS.registerBoolean("single", false);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FUZZY = DATA_COMPONENTS.registerBoolean("fuzzy", false);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SILK_TOUCH = DATA_COMPONENTS.registerBoolean("silk_touch", false);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> INVERSE = DATA_COMPONENTS.registerBoolean("inverse", false);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> INVERSE_REQUIRES_REPLACE = DATA_COMPONENTS.registerBoolean("inverse_replace", false);
    //TODO: Re-evaluate the from_recipe data. For one thing maybe it can get away with being an attachment that is not serializable/able to be copied?
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FROM_RECIPE = DATA_COMPONENTS.registerBoolean("from_recipe", false);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> INSERT_INTO_FREQUENCY = DATA_COMPONENTS.registerBoolean("insert_into_frequency", true);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RADIUS = DATA_COMPONENTS.registerNonNegativeInt("radius", TileEntityDigitalMiner.DEFAULT_RADIUS);
    //TODO - 1.20.4: Better min and max for these?
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MIN_Y = DATA_COMPONENTS.registerInt("min_y", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MAX_Y = DATA_COMPONENTS.registerInt("max_y", TileEntityDigitalMiner.DEFAULT_HEIGHT_RANGE, Integer.MIN_VALUE, Integer.MAX_VALUE);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> DELAY = DATA_COMPONENTS.registerInt("delay", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Long>> LONG_AMOUNT = DATA_COMPONENTS.registerNonNegativeLong("long_amount", 0);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FloatingLong>> MIN_THRESHOLD = DATA_COMPONENTS.registerFloatingLong("min_threshold");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FloatingLong>> MAX_THRESHOLD = DATA_COMPONENTS.registerFloatingLong("max_threshold");

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<UUID>> PERSONAL_STORAGE_ID = DATA_COMPONENTS.registerUUID("storage_id");

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedEnergyContainers>> ENERGY_CONTAINERS = DATA_COMPONENTS.registerContainer("energy_containers", () -> ContainerType.ENERGY);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedInventorySlots>> INVENTORY_SLOTS = DATA_COMPONENTS.registerContainer("inventory_slots", () -> ContainerType.ITEM);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedFluidTanks>> FLUID_TANKS = DATA_COMPONENTS.registerContainer("fluid_tanks", () -> ContainerType.FLUID);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedGasTanks>> GAS_TANKS = DATA_COMPONENTS.registerContainer("gas_tanks", () -> ContainerType.GAS);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedInfusionTanks>> INFUSION_TANKS = DATA_COMPONENTS.registerContainer("infusion_tanks", () -> ContainerType.INFUSION);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedPigmentTanks>> PIGMENT_TANKS = DATA_COMPONENTS.registerContainer("pigment_tanks", () -> ContainerType.PIGMENT);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedSlurryTanks>> SLURRY_TANKS = DATA_COMPONENTS.registerContainer("slurry_tanks", () -> ContainerType.SLURRY);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedHeatCapacitors>> HEAT_CAPACITORS = DATA_COMPONENTS.registerContainer("heat_capacitors", () -> ContainerType.HEAT);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<UUID>> OWNER = DATA_COMPONENTS.registerUUID("owner");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<SecurityMode>> SECURITY = DATA_COMPONENTS.simple("security",
          builder -> builder.persistent(SecurityMode.CODEC)
                .networkSynchronized(SecurityMode.STREAM_CODEC)
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedEjector>> EJECTOR = DATA_COMPONENTS.simple("ejector",
          builder -> builder.persistent(AttachedEjector.CODEC)
                .networkSynchronized(AttachedEjector.STREAM_CODEC)
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedSideConfig>> SIDE_CONFIG = DATA_COMPONENTS.simple("side_config",
          builder -> builder.persistent(AttachedSideConfig.CODEC)
                .networkSynchronized(AttachedSideConfig.STREAM_CODEC)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<EnumColor>> COLOR = DATA_COMPONENTS.simple("color",
          builder -> builder.persistent(EnumColor.CODEC)
                .networkSynchronized(EnumColor.STREAM_CODEC)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FilterAware>> FILTER_AWARE = DATA_COMPONENTS.simple("filters",
          builder -> builder.persistent(FilterAware.CODEC)
                .networkSynchronized(FilterAware.STREAM_CODEC)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<OverflowAware>> OVERFLOW_AWARE = DATA_COMPONENTS.simple("overflow",
          builder -> builder.persistent(OverflowAware.CODEC)
                .networkSynchronized(OverflowAware.STREAM_CODEC)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<DriveMetadata>> DRIVE_METADATA = DATA_COMPONENTS.simple("drive_metadata",
          builder -> builder.persistent(DriveMetadata.CODEC)
                .networkSynchronized(DriveMetadata.STREAM_CODEC)
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<DriveContents>> DRIVE_CONTENTS = DATA_COMPONENTS.simple("drive_contents",
          builder -> builder.persistent(DriveContents.CODEC)
                .networkSynchronized(DriveContents.STREAM_CODEC)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<BlockData>> BLOCK_DATA = DATA_COMPONENTS.simple("block_data",
          builder -> builder.persistent(BlockData.CODEC)
                .networkSynchronized(BlockData.STREAM_CODEC)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Component>> ROBIT_NAME = DATA_COMPONENTS.registerComponent("robit_name");

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ResourceKey<RobitSkin>>> ROBIT_SKIN = DATA_COMPONENTS.registerResourceKey("robit_skin", MekanismAPI.ROBIT_SKIN_REGISTRY_NAME);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> CONFIGURATION_DATA = DATA_COMPONENTS.simple("configuration_data",
          builder -> builder.persistent(CompoundTag.CODEC)
                .networkSynchronized(ByteBufCodecs.TRUSTED_COMPOUND_TAG)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Item>> REPLACE_STACK = DATA_COMPONENTS.simple("replace_stack",
          builder -> builder.persistent(BuiltInRegistries.ITEM.byNameCodec())
                .networkSynchronized(ByteBufCodecs.registry(Registries.ITEM))
    );

    //TODO - 1.20.5: Validate this doesn't have to be optional
    //Note: We can't directly use ItemStack as it needs to override equals and hashcode, but as our only use case converts it to a HashedItem, we just use that
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<HashedItem>> ITEM_TARGET = DATA_COMPONENTS.simple("item_target",
          builder -> builder.persistent(HashedItem.CODEC)
                .networkSynchronized(HashedItem.STREAM_CODEC)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<StabilizedChunks>> STABILIZER_CHUNKS = DATA_COMPONENTS.simple("stabilzer_chunks",
          builder -> builder.persistent(StabilizedChunks.CODEC)
                .networkSynchronized(StabilizedChunks.STREAM_CODEC)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FrequencyAware<TeleporterFrequency>>> TELEPORTER_FREQUENCY = DATA_COMPONENTS.registerFrequencyAware("teleporter_frequency", () -> FrequencyType.TELEPORTER);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FrequencyAware<InventoryFrequency>>> INVENTORY_FREQUENCY = DATA_COMPONENTS.registerFrequencyAware("inventory_frequency", () -> FrequencyType.INVENTORY);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FrequencyAware<QIOFrequency>>> QIO_FREQUENCY = DATA_COMPONENTS.registerFrequencyAware("qio_frequency", () -> FrequencyType.QIO);

    @Nullable//TODO - 1.20.5: Can we make this work without raw types?
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <FREQ extends Frequency> DataComponentType<FrequencyAware<FREQ>> getFrequencyComponent(FrequencyType<FREQ> freq) {
        if (freq == FrequencyType.TELEPORTER) {
            return (DataComponentType) TELEPORTER_FREQUENCY.value();
        } else if (freq == FrequencyType.INVENTORY) {
            return (DataComponentType) INVENTORY_FREQUENCY.value();
        } else if (freq == FrequencyType.QIO) {
            return (DataComponentType) QIO_FREQUENCY.value();
        }
        return null;
    }

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> FREQUENCY_COMPONENT = DATA_COMPONENTS.simple("frequencies",
          builder -> builder.persistent(CompoundTag.CODEC)
                .networkSynchronized(ByteBufCodecs.TRUSTED_COMPOUND_TAG)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<UpgradeAware>> UPGRADES = DATA_COMPONENTS.simple("upgrades",
          builder -> builder.persistent(UpgradeAware.CODEC)
                .networkSynchronized(UpgradeAware.STREAM_CODEC)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FormulaAttachment>> FORMULA_HOLDER = DATA_COMPONENTS.simple("formula",
          builder -> builder.persistent(FormulaAttachment.CODEC)
                .networkSynchronized(FormulaAttachment.STREAM_CODEC)
    );

    //Non-serializable attachments for use in persisting a backing object between multiple capabilities
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<PortableDashboardContents>> QIO_DASHBOARD = DATA_COMPONENTS.simple("qio_dashboard",
          builder -> builder.persistent(PortableDashboardContents.CODEC)
                .networkSynchronized(PortableDashboardContents.STREAM_CODEC)
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<MergedChemicalTank>> CHEMICAL_TANK_CONTENTS_HANDLER = DATA_COMPONENTS.simple("chemical_tank_contents_handler",
          builder -> {
              //TODO - 1.20.5: Figure out how to implement containers
              /*if (holder instanceof ItemStack stack && !stack.isEmpty() && stack.getItem() instanceof ItemBlockChemicalTank tank) {
                  ChemicalTankTier tier = Objects.requireNonNull(tank.getTier(), "Chemical tank tier cannot be null");
                  return MergedChemicalTank.create(
                        new GasTankRateLimitChemicalTank(tier, null),
                        new InfusionTankRateLimitChemicalTank(tier, null),
                        new PigmentTankRateLimitChemicalTank(tier, null),
                        new SlurryTankRateLimitChemicalTank(tier, null)
                  );
              }*/
              return builder.persistent(Codec.unit(() -> null));
          });
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<MergedTank>> GAUGE_DROPPER_CONTENTS_HANDLER = DATA_COMPONENTS.simple("gauge_dropper_contents_handler",
          builder -> {
              //TODO - 1.20.5: Figure out how to implement containers
              /*if (holder instanceof ItemStack stack && stack.is(MekanismItems.GAUGE_DROPPER)) {
                  return MergedTank.create(
                        RateLimitFluidTank.create(MekanismConfig.gear.gaugeDroppedTransferRate, MekanismConfig.gear.gaugeDropperCapacity,
                              BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue),
                        RateLimitGasTank.create(MekanismConfig.gear.gaugeDroppedTransferRate, MekanismConfig.gear.gaugeDropperCapacity,
                              ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrue),
                        RateLimitInfusionTank.create(MekanismConfig.gear.gaugeDroppedTransferRate, MekanismConfig.gear.gaugeDropperCapacity,
                              ChemicalTankBuilder.INFUSION.alwaysTrueBi, ChemicalTankBuilder.INFUSION.alwaysTrueBi, ChemicalTankBuilder.INFUSION.alwaysTrue),
                        RateLimitPigmentTank.create(MekanismConfig.gear.gaugeDroppedTransferRate, MekanismConfig.gear.gaugeDropperCapacity,
                              ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrue),
                        RateLimitSlurryTank.create(MekanismConfig.gear.gaugeDroppedTransferRate, MekanismConfig.gear.gaugeDropperCapacity,
                              ChemicalTankBuilder.SLURRY.alwaysTrueBi, ChemicalTankBuilder.SLURRY.alwaysTrueBi, ChemicalTankBuilder.SLURRY.alwaysTrue)
                  );
              }*/
              return builder.persistent(Codec.unit(() -> null));
          });
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<MergedChemicalTank>> CDC_CONTENTS_HANDLER = DATA_COMPONENTS.simple("cdc_contents_handler",
          builder -> {
              //TODO - 1.20.5: Figure out how to implement containers
              /*if (holder instanceof ItemStack stack && stack.is(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER.asItem())) {
                  return MergedChemicalTank.create(
                        RateLimitGasTank.createBasicItem(TileEntityChemicalDissolutionChamber.MAX_CHEMICAL,
                              ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrue
                        ),
                        RateLimitInfusionTank.createBasicItem(TileEntityChemicalDissolutionChamber.MAX_CHEMICAL,
                              ChemicalTankBuilder.INFUSION.alwaysTrueBi, ChemicalTankBuilder.INFUSION.alwaysTrueBi, ChemicalTankBuilder.INFUSION.alwaysTrue
                        ),
                        RateLimitPigmentTank.createBasicItem(TileEntityChemicalDissolutionChamber.MAX_CHEMICAL,
                              ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrue
                        ),
                        RateLimitSlurryTank.createBasicItem(TileEntityChemicalDissolutionChamber.MAX_CHEMICAL,
                              ChemicalTankBuilder.SLURRY.alwaysTrueBi, ChemicalTankBuilder.SLURRY.alwaysTrueBi, ChemicalTankBuilder.SLURRY.alwaysTrue
                        )
                  );
              }*/
              return builder.persistent(Codec.unit(() -> null));
          });
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<MergedChemicalTank>> CRYSTALLIZER_CONTENTS_HANDLER = DATA_COMPONENTS.simple("crystallizer_contents_handler",
          builder -> {
              //TODO - 1.20.5: Figure out how to implement containers
              /*if (holder instanceof ItemStack stack && stack.is(MekanismBlocks.CHEMICAL_CRYSTALLIZER.asItem())) {
                  return MergedChemicalTank.create(
                        RateLimitGasTank.createBasicItem(TileEntityChemicalCrystallizer.MAX_CHEMICAL,
                              ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrueBi,
                              gas -> MekanismRecipeType.CRYSTALLIZING.getInputCache().containsInput(null, gas)
                        ),
                        RateLimitInfusionTank.createBasicItem(TileEntityChemicalCrystallizer.MAX_CHEMICAL,
                              ChemicalTankBuilder.INFUSION.alwaysTrueBi, ChemicalTankBuilder.INFUSION.alwaysTrueBi,
                              infuseType -> MekanismRecipeType.CRYSTALLIZING.getInputCache().containsInput(null, infuseType)
                        ),
                        RateLimitPigmentTank.createBasicItem(TileEntityChemicalCrystallizer.MAX_CHEMICAL,
                              ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrueBi,
                              pigment -> MekanismRecipeType.CRYSTALLIZING.getInputCache().containsInput(null, pigment)
                        ),
                        RateLimitSlurryTank.createBasicItem(TileEntityChemicalCrystallizer.MAX_CHEMICAL,
                              ChemicalTankBuilder.SLURRY.alwaysTrueBi, ChemicalTankBuilder.SLURRY.alwaysTrueBi,
                              slurry -> MekanismRecipeType.CRYSTALLIZING.getInputCache().containsInput(null, slurry)
                        )
                  );
              }*/
              return builder.persistent(Codec.unit(() -> null));
          });
}