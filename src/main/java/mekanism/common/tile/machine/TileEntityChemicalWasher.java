package mekanism.common.tile.machine;

import java.util.Collections;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.TwoInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.recipes.vanilla_input.SingleFluidChemicalRecipeInput;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.capabilities.holder.energy.EnergyConfigHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.integration.computer.computercraft.ComputerConstants;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.ChemicalInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler.FluidChemicalRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.FluidChemical;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class TileEntityChemicalWasher extends TileEntityRecipeMachine<FluidChemicalToChemicalRecipe> implements
      FluidChemicalRecipeLookupHandler<FluidChemicalToChemicalRecipe> {

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE,
          RecipeError.NOT_ENOUGH_INPUT,
          RecipeError.NOT_ENOUGH_SECONDARY_INPUT,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );
    public static final long MAX_SLURRY = 10L * FluidType.BUCKET_VOLUME;
    public static final long MAX_FLUID = 10L * FluidType.BUCKET_VOLUME;

    @UnknownNullability//Initialized via getInitialFluidTanks
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getFluid", "getFluidCapacity", "getFluidNeeded",
                                                                                     "getFluidFilledPercentage"}, docPlaceholder = "fluid tank")
    public BasicFluidTank fluidTank;
    @UnknownNullability//Initialized via getInitialChemicalTanks
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getSlurryInput", "getSlurryInputCapacity", "getSlurryInputNeeded",
                                                                                        "getSlurryInputFilledPercentage"}, docPlaceholder = "input slurry tank")
    public IChemicalTank inputTank;
    @UnknownNullability//Initialized via getInitialChemicalTanks
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getSlurryOutput", "getSlurryOutputCapacity", "getSlurryOutputNeeded",
                                                                                        "getSlurryOutputFilledPercentage"}, docPlaceholder = "output slurry tank")
    public IChemicalTank outputTank;

    private int clientEnergyUsed = 0;
    private int baselineMaxOperations = 1;

    private final IOutputHandler<ChemicalStackTemplate> outputHandler;
    private final IInputHandler<Fluid, FluidStack> fluidInputHandler;
    private final IInputHandler<Chemical, ChemicalStack> slurryInputHandler;

    @UnknownNullability//Initialized via getInitialEnergyContainer
    private MachineEnergyContainer<TileEntityChemicalWasher> energyContainer;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFluidItemInput", docPlaceholder = "fluid item input slot")
    FluidInventorySlot fluidSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFluidItemOutput", docPlaceholder = "fluid item output slot")
    OutputInventorySlot fluidOutputSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem", docPlaceholder = "slurry item output slot")
    ChemicalInventorySlot slurryOutputSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    public TileEntityChemicalWasher(BlockPos pos, BlockState state) {
        super(MekanismBlocks.CHEMICAL_WASHER, pos, state, TRACKED_ERROR_TYPES);
        configComponent.setupItemIOConfig(Collections.singletonList(fluidSlot), List.of(slurryOutputSlot, fluidOutputSlot), energySlot, true);
        configComponent.setupIOConfig(TransmissionType.CHEMICAL, inputTank, outputTank);
        configComponent.setupInputConfig(TransmissionType.FLUID, fluidTank);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.CHEMICAL)
              .setCanTankEject(tank -> tank != inputTank);

        slurryInputHandler = InputHelper.getInputHandler(inputTank, RecipeError.NOT_ENOUGH_INPUT);
        fluidInputHandler = InputHelper.getInputHandler(fluidTank, RecipeError.NOT_ENOUGH_SECONDARY_INPUT);
        outputHandler = OutputHelper.getOutputHandler(outputTank, RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
    }

    @Override
    public IContainerHolder<IChemicalTank> getInitialChemicalTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        MekContainerHelper<IChemicalTank> builder = MekContainerHelper.forSideWithChemicalConfig(this);
        builder.addContainer(inputTank = BasicChemicalTank.input(MAX_SLURRY, (chemicalType, _) -> containsRecipeBA(fluidTank.resource(), chemicalType), this::containsRecipeB,
              recipeCacheListener));
        builder.addContainer(outputTank = BasicChemicalTank.output(MAX_SLURRY, recipeCacheUnpauseListener));
        return builder.build();
    }

    @Override
    protected IContainerHolder<IFluidTank> getInitialFluidTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        MekContainerHelper<IFluidTank> builder = MekContainerHelper.forSideWithFluidConfig(this);
        builder.addContainer(fluidTank = BasicFluidTank.input(MAX_FLUID, (fluidType, _) -> containsRecipeAB(fluidType, inputTank.resource()), this::containsRecipeA, recipeCacheListener));
        return builder.build();
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainer(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        energyContainer = MachineEnergyContainer.input(this, recipeCacheUnpauseListener);
        return new EnergyConfigHolder(energyContainer, this);
    }

    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSideWithItemConfig(this);
        builder.addContainer(fluidSlot = FluidInventorySlot.fill(fluidTank, listener, 180, 71));
        //Output slot for the fluid container that was used as an input
        builder.addContainer(fluidOutputSlot = OutputInventorySlot.at(listener, 180, 102));
        builder.addContainer(slurryOutputSlot = ChemicalInventorySlot.drain(outputTank, listener, 152, 56));
        builder.addContainer(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 152, 14));
        slurryOutputSlot.setSlotOverlay(SlotOverlay.MINUS);
        fluidSlot.setSlotType(ContainerSlotType.INPUT);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        energySlot.fillContainerOrConvert(null);
        fluidSlot.fillTankFromSlot(fluidOutputSlot, null);
        slurryOutputSlot.drainTankIntoSlot(null);
        clientEnergyUsed = recipeCacheLookupMonitor.updateAndProcess(energyContainer);
        return sendUpdatePacket;
    }

    @ComputerMethod(nameOverride = "getEnergyUsage", methodDescription = ComputerConstants.DESCRIPTION_GET_ENERGY_USAGE)
    public int getEnergyUsed() {
        return clientEnergyUsed;
    }

    @Override
    public IMekanismRecipeTypeProvider<SingleFluidChemicalRecipeInput, FluidChemicalToChemicalRecipe, FluidChemical<FluidChemicalToChemicalRecipe>> getRecipeType() {
        return MekanismRecipeType.WASHING;
    }

    @Override
    public IRecipeViewerRecipeType<FluidChemicalToChemicalRecipe> recipeViewerType() {
        return RecipeViewerRecipeType.WASHING;
    }

    @Nullable
    @Override
    public FluidChemicalToChemicalRecipe getRecipe(int cacheIndex) {
        return getRecipeType().getInputCache().findFirstRecipe(level, fluidInputHandler.getInput(), slurryInputHandler.getInput(), false);
    }

    @Override
    public CachedRecipe<FluidChemicalToChemicalRecipe> createNewCachedRecipe(FluidChemicalToChemicalRecipe recipe, int cacheIndex) {
        return new TwoInputCachedRecipe<>(recipe, recheckAllRecipeErrors, fluidInputHandler, slurryInputHandler, outputHandler)
              .setErrorsChanged(this::onErrorsChanged)
              .setCanHolderFunction(this::canFunction)
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setBaselineMaxOperations(() -> baselineMaxOperations)
              .setOnFinish(this::markForSave);
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            baselineMaxOperations = (int) Math.pow(2, getUpgrades(Upgrade.SPEED));
        }
    }

    @Override
    public int getRedstoneLevel() {
        return ContainerType.CHEMICAL.getRedstoneSignalFromContainer(inputTank);
    }

    @Override
    protected boolean makesComparatorDirty(IContainerType<?, ?> type) {
        return type == ContainerType.CHEMICAL;
    }

    public MachineEnergyContainer<TileEntityChemicalWasher> energyContainer() {
        return energyContainer;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(this::getEnergyUsed, value -> clientEnergyUsed = value));
    }
}
