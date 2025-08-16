package mekanism.common.registries;

import java.util.Optional;
import java.util.UUID;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.robit.RobitSkin;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.attachments.BlockData;
import mekanism.common.attachments.FilterAware;
import mekanism.common.attachments.FormulaAttachment;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.attachments.LockData;
import mekanism.common.attachments.OverflowAware;
import mekanism.common.attachments.StabilizedChunks;
import mekanism.common.attachments.component.AttachedEjector;
import mekanism.common.attachments.component.AttachedSideConfig;
import mekanism.common.attachments.component.UpgradeAware;
import mekanism.common.attachments.containers.chemical.AttachedChemicals;
import mekanism.common.attachments.containers.energy.AttachedEnergy;
import mekanism.common.attachments.containers.fluid.AttachedFluids;
import mekanism.common.attachments.containers.heat.AttachedHeat;
import mekanism.common.attachments.containers.item.AttachedItems;
import mekanism.common.attachments.qio.DriveContents;
import mekanism.common.attachments.qio.DriveMetadata;
import mekanism.common.attachments.qio.PortableDashboardContents;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.content.gear.ModuleContainer;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.gear.ItemAtomicDisassembler.DisassemblerMode;
import mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode;
import mekanism.common.item.interfaces.IFreeRunnerItem.FreeRunnerMode;
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
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class MekanismDataComponents {

    private MekanismDataComponents() {
    }

    public static final DataComponentDeferredRegister DATA_COMPONENTS = new DataComponentDeferredRegister(Mekanism.MODID);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ModuleContainer>> MODULE_CONTAINER = DATA_COMPONENTS.simple("module_container",
          builder -> builder.persistent(ModuleContainer.CODEC)
                .networkSynchronized(ModuleContainer.STREAM_CODEC)
                .cacheEncoding()
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedEnergy>> ATTACHED_ENERGY = DATA_COMPONENTS.simple("energy",
          builder -> builder.persistent(AttachedEnergy.CODEC)
                .networkSynchronized(AttachedEnergy.STREAM_CODEC)
                .cacheEncoding()
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedItems>> ATTACHED_ITEMS = DATA_COMPONENTS.simple("items",
          builder -> builder.persistent(AttachedItems.CODEC)
                .networkSynchronized(AttachedItems.STREAM_CODEC)
                .cacheEncoding()
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedFluids>> ATTACHED_FLUIDS = DATA_COMPONENTS.simple("fluids",
          builder -> builder.persistent(AttachedFluids.CODEC)
                .networkSynchronized(AttachedFluids.STREAM_CODEC)
                .cacheEncoding()
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedChemicals>> ATTACHED_CHEMICALS = DATA_COMPONENTS.simple("chemicals",
          builder -> builder.persistent(AttachedChemicals.CODEC)
                .networkSynchronized(AttachedChemicals.STREAM_CODEC)
                .cacheEncoding()
    );

    static {//TODO - 1.22: remove backcompat
        DATA_COMPONENTS.addAlias(Mekanism.rl("gases"), Mekanism.rl(ATTACHED_CHEMICALS.getName()));
        DATA_COMPONENTS.addAlias(Mekanism.rl("infuse_types"), Mekanism.rl(ATTACHED_CHEMICALS.getName()));
        DATA_COMPONENTS.addAlias(Mekanism.rl("pigments"), Mekanism.rl(ATTACHED_CHEMICALS.getName()));
        DATA_COMPONENTS.addAlias(Mekanism.rl("slurries"), Mekanism.rl(ATTACHED_CHEMICALS.getName()));
    }

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedHeat>> ATTACHED_HEAT = DATA_COMPONENTS.simple("heat_data",
          builder -> builder.persistent(AttachedHeat.CODEC)
                .networkSynchronized(AttachedHeat.STREAM_CODEC)
                .cacheEncoding()
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
    //TODO: Re-evaluate the from_recipe data. For one thing maybe it can get away with being an attachment that is not serializable/able to be copied?
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FROM_RECIPE = DATA_COMPONENTS.registerBoolean("from_recipe");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> INSERT_INTO_FREQUENCY = DATA_COMPONENTS.registerBoolean("insert_into_frequency");

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RADIUS = DATA_COMPONENTS.registerNonNegativeInt("radius");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MIN_Y = DATA_COMPONENTS.registerInt("min_y");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MAX_Y = DATA_COMPONENTS.registerInt("max_y");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<OverflowAware>> OVERFLOW_AWARE = DATA_COMPONENTS.simple("overflow",
          builder -> builder.persistent(OverflowAware.CODEC)
                .networkSynchronized(OverflowAware.STREAM_CODEC)
                .cacheEncoding()
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Item>> REPLACE_STACK = DATA_COMPONENTS.simple("replace_stack",
          builder -> builder.persistent(BuiltInRegistries.ITEM.byNameCodec())
                .networkSynchronized(ByteBufCodecs.registry(Registries.ITEM))
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> DELAY = DATA_COMPONENTS.registerInt("delay");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Long>> MIN_THRESHOLD = DATA_COMPONENTS.registerNonNegativeLong("min_threshold");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Long>> MAX_THRESHOLD = DATA_COMPONENTS.registerNonNegativeLong("max_threshold");

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Long>> ENERGY_USAGE = DATA_COMPONENTS.registerNonNegativeLong("energy_usage");

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Long>> LONG_AMOUNT = DATA_COMPONENTS.registerNonNegativeLong("long_amount");
    //Note: We can't directly use ItemStack as it needs to override equals and hashcode, but as our only use case converts it to a HashedItem, we just use that
    // We don't add this by default to the redstone adapter, so that the default state is there is no target set
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Optional<HashedItem>>> ITEM_TARGET = DATA_COMPONENTS.simple("item_target",
          builder -> builder.persistent(ExtraCodecs.optionalEmptyMap(HashedItem.CODEC)
                .promotePartial(error -> Mekanism.logger.error("Failed to load item target: {}", error))
                .orElse(Optional.empty())
          ).networkSynchronized(ByteBufCodecs.optional(HashedItem.STREAM_CODEC))
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<DriveMetadata>> DRIVE_METADATA = DATA_COMPONENTS.simple("drive_metadata",
          builder -> builder.persistent(DriveMetadata.CODEC)
                .networkSynchronized(DriveMetadata.STREAM_CODEC)
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<DriveContents>> DRIVE_CONTENTS = DATA_COMPONENTS.simple("drive_contents",
          builder -> builder.persistent(DriveContents.CODEC)
                .networkSynchronized(DriveContents.STREAM_CODEC)
                .cacheEncoding()
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<StabilizedChunks>> STABILIZER_CHUNKS = DATA_COMPONENTS.simple("stabilzer_chunks",
          builder -> builder.persistent(StabilizedChunks.CODEC)
                .networkSynchronized(StabilizedChunks.STREAM_CODEC)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Component>> ROBIT_NAME = DATA_COMPONENTS.registerComponent("robit_name");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ResourceKey<RobitSkin>>> ROBIT_SKIN = DATA_COMPONENTS.registerResourceKey("robit_skin", MekanismAPI.ROBIT_SKIN_REGISTRY_NAME);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<UUID>> PERSONAL_STORAGE_ID = DATA_COMPONENTS.registerUUID("storage_id");

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<LockData>> LOCK = DATA_COMPONENTS.simple("lock",
          builder -> builder.persistent(LockData.CODEC)
                .networkSynchronized(LockData.STREAM_CODEC)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FormulaAttachment>> FORMULA_HOLDER = DATA_COMPONENTS.simple("formula",
          builder -> builder.persistent(FormulaAttachment.CODEC)
                .networkSynchronized(FormulaAttachment.STREAM_CODEC)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> CONFIGURATION_DATA = DATA_COMPONENTS.simple("configuration_data",
          builder -> builder.persistent(CompoundTag.CODEC)
                .networkSynchronized(ByteBufCodecs.TRUSTED_COMPOUND_TAG)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<BlockData>> BLOCK_DATA = DATA_COMPONENTS.simple("block_data",
          builder -> builder.persistent(BlockData.CODEC)
                .networkSynchronized(BlockData.STREAM_CODEC)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<EnumColor>> COLOR = DATA_COMPONENTS.simple("color",
          builder -> builder.persistent(EnumColor.CODEC)
                .networkSynchronized(EnumColor.STREAM_CODEC)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<UUID>> OWNER = DATA_COMPONENTS.registerUUID("owner");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<SecurityMode>> SECURITY = DATA_COMPONENTS.simple("security",
          builder -> builder.persistent(SecurityMode.CODEC)
                .networkSynchronized(SecurityMode.STREAM_CODEC)
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

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FrequencyAware<TeleporterFrequency>>> TELEPORTER_FREQUENCY = DATA_COMPONENTS.registerFrequencyAware("teleporter_frequency", () -> FrequencyType.TELEPORTER);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FrequencyAware<InventoryFrequency>>> INVENTORY_FREQUENCY = DATA_COMPONENTS.registerFrequencyAware("inventory_frequency", () -> FrequencyType.INVENTORY);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FrequencyAware<QIOFrequency>>> QIO_FREQUENCY = DATA_COMPONENTS.registerFrequencyAware("qio_frequency", () -> FrequencyType.QIO);

    @Nullable
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

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<PortableDashboardContents>> QIO_DASHBOARD = DATA_COMPONENTS.simple("qio_dashboard",
          builder -> builder.persistent(PortableDashboardContents.CODEC)
                .networkSynchronized(PortableDashboardContents.STREAM_CODEC)
                .cacheEncoding()
    );
}