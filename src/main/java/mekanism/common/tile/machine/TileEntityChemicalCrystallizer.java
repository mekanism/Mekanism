package mekanism.common.tile.machine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ChemicalCrystallizerCachedRecipe;
import mekanism.api.recipes.inputs.BoxedChemicalInputHandler;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.api.recipes.inputs.chemical.SlurryStackIngredient;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.MergedChemicalInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;

public class TileEntityChemicalCrystallizer extends TileEntityProgressMachine<ChemicalCrystallizerRecipe> {

    public static final long MAX_CHEMICAL = 10_000;

    public MergedChemicalTank inputTank;

    private final IOutputHandler<@NonNull ItemStack> outputHandler;
    private final BoxedChemicalInputHandler inputHandler;

    private MachineEnergyContainer<TileEntityChemicalCrystallizer> energyContainer;
    private MergedChemicalInventorySlot<MergedChemicalTank> inputSlot;
    private OutputInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityChemicalCrystallizer() {
        super(MekanismBlocks.CHEMICAL_CRYSTALLIZER, 200);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY,
              TransmissionType.GAS, TransmissionType.INFUSION, TransmissionType.PIGMENT, TransmissionType.SLURRY);
        configComponent.setupItemIOConfig(inputSlot, outputSlot, energySlot);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);
        configComponent.setupInputConfig(TransmissionType.GAS, inputTank.getGasTank());
        configComponent.setupInputConfig(TransmissionType.INFUSION, inputTank.getInfusionTank());
        configComponent.setupInputConfig(TransmissionType.PIGMENT, inputTank.getPigmentTank());
        configComponent.setupInputConfig(TransmissionType.SLURRY, inputTank.getSlurryTank());

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
        inputHandler = new BoxedChemicalInputHandler(inputTank);
        outputHandler = OutputHelper.getOutputHandler(outputSlot);
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        inputTank = MergedChemicalTank.create(
              ChemicalTankBuilder.GAS.input(MAX_CHEMICAL, gas -> containsRecipe(recipe -> {
                  IChemicalStackIngredient<?, ?> input = recipe.getInput();
                  return input instanceof GasStackIngredient && ((GasStackIngredient) input).testType(gas);
              }), this),
              ChemicalTankBuilder.INFUSION.input(MAX_CHEMICAL, infuseType -> containsRecipe(recipe -> {
                  IChemicalStackIngredient<?, ?> input = recipe.getInput();
                  return input instanceof InfusionStackIngredient && ((InfusionStackIngredient) input).testType(infuseType);
              }), this),
              ChemicalTankBuilder.PIGMENT.input(MAX_CHEMICAL, pigment -> containsRecipe(recipe -> {
                  IChemicalStackIngredient<?, ?> input = recipe.getInput();
                  return input instanceof PigmentStackIngredient && ((PigmentStackIngredient) input).testType(pigment);
              }), this),
              ChemicalTankBuilder.SLURRY.input(MAX_CHEMICAL, slurry -> containsRecipe(recipe -> {
                  IChemicalStackIngredient<?, ?> input = recipe.getInput();
                  return input instanceof SlurryStackIngredient && ((SlurryStackIngredient) input).testType(slurry);
              }), this)
        );
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        builder.addTank(inputTank.getGasTank());
        return builder.build();
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks() {
        ChemicalTankHelper<InfuseType, InfusionStack, IInfusionTank> builder = ChemicalTankHelper.forSideInfusionWithConfig(this::getDirection, this::getConfig);
        builder.addTank(inputTank.getInfusionTank());
        return builder.build();
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks() {
        ChemicalTankHelper<Pigment, PigmentStack, IPigmentTank> builder = ChemicalTankHelper.forSidePigmentWithConfig(this::getDirection, this::getConfig);
        builder.addTank(inputTank.getPigmentTank());
        return builder.build();
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks() {
        ChemicalTankHelper<Slurry, SlurryStack, ISlurryTank> builder = ChemicalTankHelper.forSideSlurryWithConfig(this::getDirection, this::getConfig);
        builder.addTank(inputTank.getSlurryTank());
        return builder.build();
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(inputSlot = MergedChemicalInventorySlot.fill(inputTank, this, 8, 65));
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 129, 57));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 152, 5));
        inputSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        inputSlot.fillChemicalTanks();
        cachedRecipe = getUpdatedCache(0);
        if (cachedRecipe != null) {
            cachedRecipe.process();
        }
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ChemicalCrystallizerRecipe> getRecipeType() {
        return MekanismRecipeType.CRYSTALLIZING;
    }

    @Nullable
    @Override
    public ChemicalCrystallizerRecipe getRecipe(int cacheIndex) {
        BoxedChemicalStack boxedChemical = inputHandler.getInput();
        if (boxedChemical.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(boxedChemical));
    }

    @Nullable
    @Override
    public CachedRecipe<ChemicalCrystallizerRecipe> createNewCachedRecipe(@Nonnull ChemicalCrystallizerRecipe recipe, int cacheIndex) {
        return new ChemicalCrystallizerCachedRecipe(recipe, inputHandler, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(() -> markDirty(false))
              .setOperatingTicksChanged(this::setOperatingTicks);
    }

    public MachineEnergyContainer<TileEntityChemicalCrystallizer> getEnergyContainer() {
        return energyContainer;
    }
}