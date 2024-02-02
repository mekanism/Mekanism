package mekanism.common.registries;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.function.LongSupplier;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.common.Mekanism;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedGasTanks;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedInfusionTanks;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedPigmentTanks;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedSlurryTanks;
import mekanism.common.attachments.containers.AttachedEnergyContainers;
import mekanism.common.attachments.containers.AttachedFluidTanks;
import mekanism.common.attachments.containers.AttachedHeatCapacitors;
import mekanism.common.attachments.containers.AttachedInventorySlots;
import mekanism.common.attachments.containers.ContainerType;
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
import mekanism.common.tile.machine.TileEntityChemicalCrystallizer;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.fluids.FluidType;

public class MekanismAttachmentTypes {

    private MekanismAttachmentTypes() {
    }

    public static final AttachmentTypeDeferredRegister ATTACHMENT_TYPES = new AttachmentTypeDeferredRegister(Mekanism.MODID);

    //Note: We do not specify copy on death as we want radiation to reset to baseline on death
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Double>> RADIATION = ATTACHMENT_TYPES.register("radiation",
          () -> AttachmentType.builder(() -> RadiationManager.BASELINE)
                .serialize(Codec.doubleRange(RadiationManager.BASELINE, Double.MAX_VALUE))
                //Note: Technically this comparator is not needed as by default neo only checks for attachment compatability for item stacks,
                // but we set it regardless just so that if anyone is checking it for entities then they can bypass the serialization for it
                .comparator(Objects::equals)
                .build()
    );

    //Note: As we only attach this to items we don't need to make it copy on death
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<ModuleContainer>> MODULE_CONTAINER = ATTACHMENT_TYPES.register("module_container",
          () -> AttachmentType.serializable(ModuleContainer::create)
                .comparator(ModuleContainer::isCompatible)
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
              return MergedChemicalTank.INVALID;
          }).build());
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<MergedTank>> GAUGE_DROPPER_CONTENTS_HANDLER = ATTACHMENT_TYPES.register("gauge_dropper_contents_handler",
          () -> AttachmentType.builder(holder -> {
              if (holder instanceof ItemStack stack && !stack.isEmpty() && stack.is(MekanismItems.GAUGE_DROPPER)) {
                  //TODO - 1.20.4: Convert these to config options: Convert this to a long and make it a config option after making fluids be able to handle longs
                  int CAPACITY = 16 * FluidType.BUCKET_VOLUME;
                  int TRANSFER_RATE = 256;
                  LongSupplier rate = () -> TRANSFER_RATE;
                  LongSupplier capacity = () -> CAPACITY;
                  return MergedTank.create(
                        RateLimitFluidTank.create(() -> TRANSFER_RATE, () -> CAPACITY, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue, null),
                        RateLimitGasTank.create(rate, capacity, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrueBi,
                              ChemicalTankBuilder.GAS.alwaysTrue, null, null),
                        RateLimitInfusionTank.create(rate, capacity, ChemicalTankBuilder.INFUSION.alwaysTrueBi, ChemicalTankBuilder.INFUSION.alwaysTrueBi,
                              ChemicalTankBuilder.INFUSION.alwaysTrue, null),
                        RateLimitPigmentTank.create(rate, capacity, ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrueBi,
                              ChemicalTankBuilder.PIGMENT.alwaysTrue, null),
                        RateLimitSlurryTank.create(rate, capacity, ChemicalTankBuilder.SLURRY.alwaysTrueBi, ChemicalTankBuilder.SLURRY.alwaysTrueBi,
                              ChemicalTankBuilder.SLURRY.alwaysTrue, null)
                  );
              }
              return MergedTank.INVALID;
          }).build());
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<MergedChemicalTank>> CDC_CONTENTS_HANDLER = ATTACHMENT_TYPES.register("cdc_contents_handler",
          () -> AttachmentType.builder(holder -> {
              if (holder instanceof ItemStack stack && !stack.isEmpty() && stack.is(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER.asItem())) {
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
              return MergedChemicalTank.INVALID;
          }).build());
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<MergedChemicalTank>> CRYSTALLIZER_CONTENTS_HANDLER = ATTACHMENT_TYPES.register("crystallizer_contents_handler",
          () -> AttachmentType.builder(holder -> {
              if (holder instanceof ItemStack stack && !stack.isEmpty() && stack.is(MekanismBlocks.CHEMICAL_CRYSTALLIZER.asItem())) {
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
              return MergedChemicalTank.INVALID;
          }).build());

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<DisassemblerMode>> DISASSEMBLER_MODE = ATTACHMENT_TYPES.register("disassembler_mode", DisassemblerMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<ConfiguratorMode>> CONFIGURATOR_MODE = ATTACHMENT_TYPES.register("configurator_mode", ConfiguratorMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<FlamethrowerMode>> FLAMETHROWER_MODE = ATTACHMENT_TYPES.register("flamethrower_mode", FlamethrowerMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<FreeRunnerMode>> FREE_RUNNER_MODE = ATTACHMENT_TYPES.register("free_runner_mode", FreeRunnerMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<JetpackMode>> JETPACK_MODE = ATTACHMENT_TYPES.register("jetpack_mode", JetpackMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> SCUBA_TANK_MODE = ATTACHMENT_TYPES.registerBoolean("scuba_tank_mode", false);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> ELECTRIC_BOW_MODE = ATTACHMENT_TYPES.registerBoolean("electric_bow_mode", false);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> BUCKET_MODE = ATTACHMENT_TYPES.registerBoolean("bucket_mode", false);

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<AttachedEnergyContainers>> ENERGY_CONTAINERS = ATTACHMENT_TYPES.registerContainer("energy_containers", () -> ContainerType.ENERGY);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<AttachedInventorySlots>> INVENTORY_SLOTS = ATTACHMENT_TYPES.registerContainer("inventory_slots", () -> ContainerType.ITEM);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<AttachedFluidTanks>> FLUID_TANKS = ATTACHMENT_TYPES.registerContainer("fluid_tanks", () -> ContainerType.FLUID);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<AttachedGasTanks>> GAS_TANKS = ATTACHMENT_TYPES.registerContainer("gas_tanks", () -> ContainerType.GAS);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<AttachedInfusionTanks>> INFUSION_TANKS = ATTACHMENT_TYPES.registerContainer("infusion_tanks", () -> ContainerType.INFUSION);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<AttachedPigmentTanks>> PIGMENT_TANKS = ATTACHMENT_TYPES.registerContainer("pigment_tanks", () -> ContainerType.PIGMENT);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<AttachedSlurryTanks>> SLURRY_TANKS = ATTACHMENT_TYPES.registerContainer("slurry_tanks", () -> ContainerType.SLURRY);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<AttachedHeatCapacitors>> HEAT_CAPACITORS = ATTACHMENT_TYPES.registerContainer("heat_capacitors", () -> ContainerType.HEAT);
}