package mekanism.common.registries;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.attachments.ColoredItem;
import mekanism.common.attachments.FilterAware;
import mekanism.common.attachments.FormulaAttachment;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.attachments.OverflowAware;
import mekanism.common.attachments.UpgradeAware;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedGasTanks;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedInfusionTanks;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedPigmentTanks;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedSlurryTanks;
import mekanism.common.attachments.containers.AttachedEnergyContainers;
import mekanism.common.attachments.containers.AttachedFluidTanks;
import mekanism.common.attachments.containers.AttachedHeatCapacitors;
import mekanism.common.attachments.containers.AttachedInventorySlots;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.security.OwnerObject;
import mekanism.common.attachments.security.OwnerObject.OwnerOnlyObject;
import mekanism.common.attachments.security.SecurityObject;
import mekanism.common.capabilities.chemical.item.ChemicalTankRateLimitChemicalTank.GasTankRateLimitChemicalTank;
import mekanism.common.capabilities.chemical.item.ChemicalTankRateLimitChemicalTank.InfusionTankRateLimitChemicalTank;
import mekanism.common.capabilities.chemical.item.ChemicalTankRateLimitChemicalTank.PigmentTankRateLimitChemicalTank;
import mekanism.common.capabilities.chemical.item.ChemicalTankRateLimitChemicalTank.SlurryTankRateLimitChemicalTank;
import mekanism.common.capabilities.chemical.variable.RateLimitGasTank;
import mekanism.common.capabilities.chemical.variable.RateLimitInfusionTank;
import mekanism.common.capabilities.chemical.variable.RateLimitPigmentTank;
import mekanism.common.capabilities.chemical.variable.RateLimitSlurryTank;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidTank;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.ModuleContainer;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.block.ItemBlockChemicalTank;
import mekanism.common.item.gear.ItemAtomicDisassembler.DisassemblerMode;
import mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode;
import mekanism.common.item.gear.ItemFreeRunners.FreeRunnerMode;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.AttachmentTypeDeferredRegister;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import mekanism.common.tile.machine.TileEntityChemicalCrystallizer;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import mekanism.common.util.TransporterUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class MekanismAttachmentTypes {

    private MekanismAttachmentTypes() {
    }

    public static final AttachmentTypeDeferredRegister ATTACHMENT_TYPES = new AttachmentTypeDeferredRegister(Mekanism.MODID);

    //Note: We do not specify copy on death as we want radiation to reset to baseline on death
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Double>> RADIATION = ATTACHMENT_TYPES.register("radiation",
          () -> AttachmentType.builder(() -> RadiationManager.BASELINE)
                .serialize(Codec.doubleRange(RadiationManager.BASELINE, Double.MAX_VALUE), radiation -> radiation != RadiationManager.BASELINE)
                //Note: Technically this comparator is not needed as by default neo only checks for attachment compatability for item stacks,
                // but we set it regardless just so that if anyone is checking it for entities then they can bypass the serialization for it
                .comparator(Double::equals)
                .build()
    );

    //Item based attachments:
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<ModuleContainer>> MODULE_CONTAINER = ATTACHMENT_TYPES.register("module_container",
          () -> AttachmentType.serializable(ModuleContainer::create)
                .comparator(ModuleContainer::isCompatible)
                .build());

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<DisassemblerMode>> DISASSEMBLER_MODE = ATTACHMENT_TYPES.register("disassembler_mode", DisassemblerMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<ConfiguratorMode>> CONFIGURATOR_MODE = ATTACHMENT_TYPES.register("configurator_mode", ConfiguratorMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<FlamethrowerMode>> FLAMETHROWER_MODE = ATTACHMENT_TYPES.register("flamethrower_mode", FlamethrowerMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<FreeRunnerMode>> FREE_RUNNER_MODE = ATTACHMENT_TYPES.register("free_runner_mode", FreeRunnerMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<JetpackMode>> JETPACK_MODE = ATTACHMENT_TYPES.register("jetpack_mode", JetpackMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<ContainerEditMode>> EDIT_MODE = ATTACHMENT_TYPES.register("edit_mode", ContainerEditMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<GasMode>> DUMP_MODE = ATTACHMENT_TYPES.register("dump_mode", GasMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<GasMode>> SECONDARY_DUMP_MODE = ATTACHMENT_TYPES.register("secondary_dump_mode", GasMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<RedstoneControl>> REDSTONE_CONTROL = ATTACHMENT_TYPES.register("redstone_control", RedstoneControl.class);

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Optional<EnumColor>>> COLOR = ATTACHMENT_TYPES.registerOptional("color", EnumColor.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Optional<EnumColor>>> TRANSPORTER_COLOR = ATTACHMENT_TYPES.register("transporter_color",
          () -> AttachmentType.<Optional<EnumColor>>builder(Optional::empty)
                .serialize(new IAttachmentSerializer<IntTag, Optional<EnumColor>>() {
                    @Nullable
                    @Override
                    public IntTag write(Optional<EnumColor> value) {
                        if (value.isEmpty()) {
                            return null;
                        }
                        int index = TransporterUtils.getColorIndex(value.get());
                        return index == -1 ? null : IntTag.valueOf(index);
                    }

                    @Override
                    public Optional<EnumColor> read(IAttachmentHolder holder, IntTag tag) {
                        return Optional.ofNullable(TransporterUtils.readColor(tag.getAsInt()));
                    }
                }).comparator(AttachmentTypeDeferredRegister.optionalComparator(Objects::equals))
                .build());


    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> SCUBA_TANK_MODE = ATTACHMENT_TYPES.registerBoolean("scuba_tank_mode", false);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> ELECTRIC_BOW_MODE = ATTACHMENT_TYPES.registerBoolean("electric_bow_mode", false);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> BUCKET_MODE = ATTACHMENT_TYPES.registerBoolean("bucket_mode", false);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> AUTO = ATTACHMENT_TYPES.registerBoolean("auto", false);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> SORTING = ATTACHMENT_TYPES.registerBoolean("sorting", false);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> EJECT = ATTACHMENT_TYPES.registerBoolean("eject", false);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> PULL = ATTACHMENT_TYPES.registerBoolean("pull", false);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> ROUND_ROBIN = ATTACHMENT_TYPES.registerBoolean("round_robin", false);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> SINGLE_ITEM = ATTACHMENT_TYPES.registerBoolean("single", false);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> FUZZY = ATTACHMENT_TYPES.registerBoolean("fuzzy", false);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> SILK_TOUCH = ATTACHMENT_TYPES.registerBoolean("silk_touch", false);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> INVERSE = ATTACHMENT_TYPES.registerBoolean("inverse", false);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> INVERSE_REQUIRES_REPLACE = ATTACHMENT_TYPES.registerBoolean("inverse_replace", false);

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Integer>> RADIUS = ATTACHMENT_TYPES.registerNonNegativeInt("radius", TileEntityDigitalMiner.DEFAULT_RADIUS);
    //TODO - 1.20.4: Better min and max for these?
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Integer>> MIN_Y = ATTACHMENT_TYPES.registerInt("min_y", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Integer>> MAX_Y = ATTACHMENT_TYPES.registerInt("max_y", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Long>> LONG_AMOUNT = ATTACHMENT_TYPES.registerNonNegativeLong("long_amount", 0);

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<AttachedEnergyContainers>> ENERGY_CONTAINERS = ATTACHMENT_TYPES.registerContainer("energy_containers", () -> ContainerType.ENERGY);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<AttachedInventorySlots>> INVENTORY_SLOTS = ATTACHMENT_TYPES.registerContainer("inventory_slots", () -> ContainerType.ITEM);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<AttachedFluidTanks>> FLUID_TANKS = ATTACHMENT_TYPES.registerContainer("fluid_tanks", () -> ContainerType.FLUID);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<AttachedGasTanks>> GAS_TANKS = ATTACHMENT_TYPES.registerContainer("gas_tanks", () -> ContainerType.GAS);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<AttachedInfusionTanks>> INFUSION_TANKS = ATTACHMENT_TYPES.registerContainer("infusion_tanks", () -> ContainerType.INFUSION);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<AttachedPigmentTanks>> PIGMENT_TANKS = ATTACHMENT_TYPES.registerContainer("pigment_tanks", () -> ContainerType.PIGMENT);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<AttachedSlurryTanks>> SLURRY_TANKS = ATTACHMENT_TYPES.registerContainer("slurry_tanks", () -> ContainerType.SLURRY);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<AttachedHeatCapacitors>> HEAT_CAPACITORS = ATTACHMENT_TYPES.registerContainer("heat_capacitors", () -> ContainerType.HEAT);

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<OwnerOnlyObject>> OWNER_ONLY = ATTACHMENT_TYPES.register("owner",
          () -> AttachmentType.serializable(OwnerOnlyObject::new)
                .comparator(OwnerObject::isCompatible)
                .build());
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<SecurityObject>> SECURITY = ATTACHMENT_TYPES.register("security",
          () -> AttachmentType.serializable(SecurityObject::new)
                .comparator(SecurityObject::isCompatible)
                .build());

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<ColoredItem>> COLORABLE = ATTACHMENT_TYPES.register("colorable",
          () -> AttachmentType.serializable(ColoredItem::new)
                .comparator(ColoredItem::isCompatible)
                .build());

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<FilterAware>> FILTER_AWARE = ATTACHMENT_TYPES.register("filters",
          () -> AttachmentType.serializable(FilterAware::new)
                .comparator(FilterAware::isCompatible)
                .build());

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<OverflowAware>> OVERFLOW_AWARE = ATTACHMENT_TYPES.register("overflow",
          () -> AttachmentType.serializable(OverflowAware::new)
                .comparator(OverflowAware::isCompatible)
                .build());

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Item>> REPLACE_STACK = ATTACHMENT_TYPES.register("replace_stack",
          () -> AttachmentType.builder(() -> Items.AIR)
                .serialize(new IAttachmentSerializer<StringTag, Item>() {
                    @Nullable
                    @Override
                    public StringTag write(Item item) {
                        if (item == Items.AIR) {
                            return null;
                        }
                        return StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString());
                    }

                    @Override
                    public Item read(IAttachmentHolder holder, StringTag tag) {
                        ResourceLocation registryName = ResourceLocation.tryParse(tag.getAsString());
                        if (registryName == null) {
                            return Items.AIR;
                        }
                        return BuiltInRegistries.ITEM.get(registryName);
                    }
                })
                .comparator(Objects::equals)
                .build());

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<ItemStack>> ITEM_TARGET = ATTACHMENT_TYPES.register("item_target",
          () -> AttachmentType.builder(() -> ItemStack.EMPTY)
                .serialize(new IAttachmentSerializer<CompoundTag, ItemStack>() {
                    @Nullable
                    @Override
                    public CompoundTag write(ItemStack stack) {
                        if (stack.isEmpty()) {
                            return null;
                        }
                        CompoundTag nbt = new CompoundTag();
                        stack.save(nbt);
                        return nbt;
                    }

                    @Override
                    public ItemStack read(IAttachmentHolder holder, CompoundTag tag) {
                        return ItemStack.of(tag);
                    }
                })
                .comparator(ItemStack::matches)
                .build());

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<boolean[]>> STABILIZER_CHUNKS = ATTACHMENT_TYPES.register("stabilzer_chunks",
          () -> AttachmentType.builder(() -> new boolean[TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER * TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER])
                .serialize(new IAttachmentSerializer<ByteArrayTag, boolean[]>() {
                    @Nullable
                    @Override
                    public ByteArrayTag write(boolean[] value) {
                        for (boolean v : value) {
                            if (v) {
                                byte[] bytes = new byte[value.length];
                                for (int i = 0; i < value.length; i++) {
                                    bytes[i] = (byte) (value[i] ? 1 : 0);
                                }
                                return new ByteArrayTag(bytes);
                            }
                        }
                        return null;
                    }

                    @Override
                    public boolean[] read(IAttachmentHolder holder, ByteArrayTag tag) {
                        boolean[] chunksToLoad = new boolean[TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER * TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER];
                        byte[] bytes = tag.getAsByteArray();
                        if (bytes.length == chunksToLoad.length) {
                            for (int i = 0; i < chunksToLoad.length; i++) {
                                chunksToLoad[i] = bytes[i] == 1;
                            }
                        }
                        return chunksToLoad;
                    }
                })
                .comparator(Objects::equals)
                .build());

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<FrequencyAware<?>>> FREQUENCY_AWARE = ATTACHMENT_TYPES.registerFrequencyAware("frequency_aware", FrequencyAware::create);

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<UpgradeAware>> UPGRADES = ATTACHMENT_TYPES.register("upgrades",
          () -> AttachmentType.serializable(UpgradeAware::create)
                .comparator(UpgradeAware::isCompatible)
                .build());

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<FormulaAttachment>> FORMULA_HOLDER = ATTACHMENT_TYPES.register("formula",
          () -> AttachmentType.serializable(FormulaAttachment::create)
                .comparator(FormulaAttachment::isCompatible)
                .build());

    //Non-serializable attachments for use in persisting a backing object between multiple capabilities
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<MergedChemicalTank>> CHEMICAL_TANK_CONTENTS_HANDLER = ATTACHMENT_TYPES.register("chemical_tank_contents_handler",
          () -> AttachmentType.builder(holder -> {
              if (holder instanceof ItemStack stack && !stack.isEmpty() && stack.getItem() instanceof ItemBlockChemicalTank tank) {
                  ChemicalTankTier tier = Objects.requireNonNull(tank.getTier(), "Chemical tank tier cannot be null");
                  return MergedChemicalTank.create(
                        new GasTankRateLimitChemicalTank(tier, null),
                        new InfusionTankRateLimitChemicalTank(tier, null),
                        new PigmentTankRateLimitChemicalTank(tier, null),
                        new SlurryTankRateLimitChemicalTank(tier, null)
                  );
              }
              throw new IllegalArgumentException("Attempted to attach a CHEMICAL_TANK_CONTENTS_HANDLER to an object other than a chemical tank.");
          }).build());
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<MergedTank>> GAUGE_DROPPER_CONTENTS_HANDLER = ATTACHMENT_TYPES.register("gauge_dropper_contents_handler",
          () -> AttachmentType.builder(holder -> {
              if (holder instanceof ItemStack stack && stack.is(MekanismItems.GAUGE_DROPPER)) {
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
              }
              throw new IllegalArgumentException("Attempted to attach a GAUGE_DROPPER_CONTENTS_HANDLER to an object other than a gauge dropper.");
          }).build());
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<MergedChemicalTank>> CDC_CONTENTS_HANDLER = ATTACHMENT_TYPES.register("cdc_contents_handler",
          () -> AttachmentType.builder(holder -> {
              if (holder instanceof ItemStack stack && stack.is(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER.asItem())) {
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
              }
              throw new IllegalArgumentException("Attempted to attach a CDC_CONTENTS_HANDLER to an object other than a chemical dissolution chamber.");
          }).build());
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<MergedChemicalTank>> CRYSTALLIZER_CONTENTS_HANDLER = ATTACHMENT_TYPES.register("crystallizer_contents_handler",
          () -> AttachmentType.builder(holder -> {
              if (holder instanceof ItemStack stack && stack.is(MekanismBlocks.CHEMICAL_CRYSTALLIZER.asItem())) {
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
              }
              throw new IllegalArgumentException("Attempted to attach a CRYSTALLIZER_CONTENTS_HANDLER to an object other than a chemical crystallizer.");
          }).build());
}