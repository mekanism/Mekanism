package mekanism.common.tile.machine;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.IntObjectToIntFunction;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ElectrolysisRecipe.ElectrolysisRecipeOutput;
import mekanism.api.recipes.basic.BasicElectrolysisRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.recipes.vanilla_input.SingleFluidRecipeInput;
import mekanism.api.upgrade.Upgrade;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.capabilities.energy.ElectroSeparatorEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import mekanism.common.capabilities.holder.single.SingleConfigHolder;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerConstants;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.ChemicalInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler.FluidRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleFluid;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.ChemicalUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class TileEntityElectrolyticSeparator extends TileEntityRecipeMachine<ElectrolysisRecipe> implements IHasGasMode, FluidRecipeLookupHandler<ElectrolysisRecipe> {

    public static final RecipeError NOT_ENOUGH_SPACE_LEFT_OUTPUT_ERROR = RecipeError.create();
    public static final RecipeError NOT_ENOUGH_SPACE_RIGHT_OUTPUT_ERROR = RecipeError.create();
    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE,
          RecipeError.NOT_ENOUGH_INPUT,
          NOT_ENOUGH_SPACE_LEFT_OUTPUT_ERROR,
          NOT_ENOUGH_SPACE_RIGHT_OUTPUT_ERROR,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );
    /// The maximum amount of gas this block can store.
    public static final long MAX_GAS = 2_400;
    public static final long MAX_FLUID = 24L * FluidType.BUCKET_VOLUME;
    private static final int BASE_DUMP_RATE = 8;
    private static final IntObjectToIntFunction<TileEntityElectrolyticSeparator> BASE_ENERGY_CALCULATOR = (base, tile) -> base * tile.getRecipeEnergyMultiplier();

    /// This separator's water slot.
    @UnknownNullability//Initialized via getInitialFluidTanks
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getInput", "getInputCapacity", "getInputNeeded",
                                                                                     "getInputFilledPercentage"}, docPlaceholder = "input tank")
    public BasicFluidTank fluidTank;
    /// The amount of oxygen this block is storing.
    @UnknownNullability//Initialized via getInitialChemicalTanks
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getLeftOutput", "getLeftOutputCapacity", "getLeftOutputNeeded",
                                                                                        "getLeftOutputFilledPercentage"}, docPlaceholder = "left output tank")
    public IChemicalTank leftTank;
    /// The amount of hydrogen this block is storing.
    @UnknownNullability//Initialized via getInitialChemicalTanks
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getRightOutput", "getRightOutputCapacity", "getRightOutputNeeded",
                                                                                        "getRightOutputFilledPercentage"}, docPlaceholder = "right output tank")
    public IChemicalTank rightTank;
    @SyntheticComputerMethod(getter = "getLeftOutputDumpingMode")
    public GasMode dumpLeft = GasMode.IDLE;
    @SyntheticComputerMethod(getter = "getRightOutputDumpingMode")
    public GasMode dumpRight = GasMode.IDLE;
    private int clientEnergyUsed = 1;
    private int recipeEnergyMultiplier = 1;
    private boolean isMakingHydrogen = false;
    private int baselineMaxOperations = 1;
    private int dumpRate = BASE_DUMP_RATE;

    private final IOutputHandler<ElectrolysisRecipeOutput> outputHandler;
    private final IInputHandler<Fluid, FluidStack> inputHandler;

    @UnknownNullability//Initialized via getInitialEnergyContainer
    private ElectroSeparatorEnergyContainer energyContainer;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem", docPlaceholder = "input item slot")
    FluidInventorySlot fluidSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getLeftOutputItem", docPlaceholder = "left output item slot")
    ChemicalInventorySlot leftOutputSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getRightOutputItem", docPlaceholder = "right output item slot")
    ChemicalInventorySlot rightOutputSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    public TileEntityElectrolyticSeparator(BlockPos pos, BlockState state) {
        super(MekanismBlocks.ELECTROLYTIC_SEPARATOR, pos, state, TRACKED_ERROR_TYPES);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(true, true, fluidSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT_1, new InventorySlotInfo(true, true, leftOutputSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT_2, new InventorySlotInfo(true, true, rightOutputSlot));
            itemConfig.addSlotInfo(DataType.INPUT_OUTPUT, new InventorySlotInfo(true, true, fluidSlot, leftOutputSlot, rightOutputSlot));
            itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(true, true, energySlot));
        }

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.CHEMICAL);
        if (gasConfig != null) {
            gasConfig.addSlotInfo(DataType.OUTPUT_1, new ChemicalSlotInfo(false, true, leftTank));
            gasConfig.addSlotInfo(DataType.OUTPUT_2, new ChemicalSlotInfo(false, true, rightTank));
        }

        configComponent.setupInputConfig(TransmissionType.FLUID, fluidTank);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.CHEMICAL)
              .setCanTankEject(tank -> {
                  if (tank == leftTank) {
                      return dumpLeft != GasMode.DUMPING;
                  } else if (tank == rightTank) {
                      return dumpRight != GasMode.DUMPING;
                  }
                  return true;
              });

        inputHandler = InputHelper.getInputHandler(fluidTank, RecipeError.NOT_ENOUGH_INPUT);
        outputHandler = OutputHelper.getOutputHandler(leftTank, NOT_ENOUGH_SPACE_LEFT_OUTPUT_ERROR, rightTank, NOT_ENOUGH_SPACE_RIGHT_OUTPUT_ERROR);
    }

    @Override
    protected IContainerHolder<IFluidTank> getInitialFluidTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        MekContainerHelper<IFluidTank> builder = MekContainerHelper.forSideWithFluidConfig(this);
        builder.addContainer(fluidTank = BasicFluidTank.input(MAX_FLUID, this::containsRecipe, recipeCacheListener));
        return builder.build();
    }

    @Override
    public IContainerHolder<IChemicalTank> getInitialChemicalTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        MekContainerHelper<IChemicalTank> builder = MekContainerHelper.forSideWithChemicalConfig(this);
        builder.addContainer(leftTank = BasicChemicalTank.output(MAX_GAS, recipeCacheUnpauseListener));
        builder.addContainer(rightTank = BasicChemicalTank.output(MAX_GAS, recipeCacheUnpauseListener));
        return builder.build();
    }

    @Override
    protected ISingleContainerHolder<IEnergyContainer> getInitialEnergyContainer(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        energyContainer = ElectroSeparatorEnergyContainer.input(this, BASE_ENERGY_CALCULATOR, recipeCacheUnpauseListener);
        return SingleConfigHolder.energy(energyContainer, this);
    }

    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSideWithItemConfig(this);
        builder.addContainer(fluidSlot = FluidInventorySlot.fill(fluidTank, listener, 26, 35));
        builder.addContainer(leftOutputSlot = ChemicalInventorySlot.drain(leftTank, listener, 59, 52));
        builder.addContainer(rightOutputSlot = ChemicalInventorySlot.drain(rightTank, listener, 101, 52));
        builder.addContainer(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 143, 35));
        fluidSlot.setSlotType(ContainerSlotType.INPUT);
        leftOutputSlot.setSlotType(ContainerSlotType.OUTPUT);
        rightOutputSlot.setSlotType(ContainerSlotType.OUTPUT);
        return builder.build();
    }

    @Override
    public void onCachedRecipeChanged(HolderLookup.Provider registries, @Nullable CachedRecipe<ElectrolysisRecipe> cachedRecipe, int cacheIndex) {
        super.onCachedRecipeChanged(registries, cachedRecipe, cacheIndex);
        recipeEnergyMultiplier = cachedRecipe == null ? 1 : cachedRecipe.getRecipe().getEnergyMultiplier();
        isMakingHydrogen = cachedRecipe != null && isHydrogenElectrolysis(cachedRecipe.getRecipe());
        HolderGetter<Upgrade> upgrades = getUpgradeLookup(registries);
        energyContainer.updateEnergyPerTick(upgrades);
        energyContainer.updateMaxEnergy(upgrades);
    }

    private boolean isHydrogenElectrolysis(ElectrolysisRecipe recipe) {
        if (recipe instanceof BasicElectrolysisRecipe basicRecipe) {
            return basicRecipe.getLeftChemicalOutput().is(ChemicalIds.HYDROGEN) || basicRecipe.getRightChemicalOutput().is(ChemicalIds.HYDROGEN);
        }
        //do it the slow way
        ContextMap contextMap = level == null ? ContextMap.EMPTY : SlotDisplayContext.fromLevel(level);
        for (ElectrolysisRecipeOutput electrolysisRecipeOutput : recipe.getOutputDefinition(contextMap)) {
            if (electrolysisRecipeOutput.left().is(ChemicalIds.HYDROGEN) || electrolysisRecipeOutput.right().is(ChemicalIds.HYDROGEN)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        energySlot.fillContainerOrConvert(null);
        fluidSlot.fillTankFromSlot(null);

        leftOutputSlot.drainTankIntoSlot(null);
        rightOutputSlot.drainTankIntoSlot(null);
        clientEnergyUsed = recipeCacheLookupMonitor.updateAndProcess(level.registryAccess(), energyContainer);

        handleTank(leftTank, dumpLeft);
        handleTank(rightTank, dumpRight);
        return sendUpdatePacket;
    }

    private void handleTank(IChemicalTank tank, GasMode mode) {
        if (!tank.isEmpty() && mode != GasMode.IDLE) {
            //Dump excess that we need to get to the target (capping at our eject rate for how much we can dump at once)
            ChemicalUtils.dump(tank, mode, dumpRate, MekanismConfig.general.chemicalAutoEjectRate.get());
        }
    }

    private long getDumpingExcessTarget(IChemicalTank tank) {
        return MathUtils.clampToLong(tank.capacityAsLong(tank.resource()) * MekanismConfig.general.dumpExcessKeepRatio.get());
    }

    private boolean atDumpingExcessTarget(IChemicalTank tank) {
        //Check >= so that if we are past and our eject rate is just low then we don't continue making it, so we never get to the eject rate
        return tank.amountAsLong() >= getDumpingExcessTarget(tank);
    }

    @Override
    public boolean canFunction() {
        //We can function if:
        // - the tile can function
        // - at least one side is not set to dumping excess
        // - at least one side is not at the dumping excess target
        return super.canFunction() && (dumpLeft != GasMode.DUMPING_EXCESS || dumpRight != GasMode.DUMPING_EXCESS || !atDumpingExcessTarget(leftTank) || !atDumpingExcessTarget(rightTank));
    }

    public int getRecipeEnergyMultiplier() {
        return recipeEnergyMultiplier;
    }

    @ComputerMethod(nameOverride = "getEnergyUsage", methodDescription = ComputerConstants.DESCRIPTION_GET_ENERGY_USAGE)
    public int getEnergyUsed() {
        return clientEnergyUsed;
    }

    @Override
    public IMekanismRecipeTypeProvider<SingleFluidRecipeInput, ElectrolysisRecipe, SingleFluid<ElectrolysisRecipe>> getRecipeType() {
        return MekanismRecipeType.SEPARATING;
    }

    @Override
    public IRecipeViewerRecipeType<ElectrolysisRecipe> recipeViewerType() {
        return RecipeViewerRecipeType.SEPARATING;
    }

    @Nullable
    @Override
    public ElectrolysisRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandler);
    }

    @Override
    public CachedRecipe<ElectrolysisRecipe> createNewCachedRecipe(ElectrolysisRecipe recipe, int cacheIndex) {
        return new OneInputCachedRecipe<>(recipe, recheckAllRecipeErrors, inputHandler, outputHandler)
              .setErrorsChanged(this::onErrorsChanged)
              .setCanHolderFunction(this::canFunction)
              .setActive(this::setActive)
              .setOperationsCost(isHydrogenElectrolysis(recipe))
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setBaselineMaxOperations(() -> baselineMaxOperations)
              .setOnFinish(this::markForSave);
    }

    @Override
    public void recalculateUpgrades(HolderGetter<Upgrade> upgrades, Holder<Upgrade> upgrade, int totalInstalled) {
        super.recalculateUpgrades(upgrades, upgrade, totalInstalled);
        if (upgrade.is(UpgradeIds.SPEED)) {
            double speed = Math.pow(2, totalInstalled);
            baselineMaxOperations = (int) speed;
            dumpRate = (int) (BASE_DUMP_RATE * speed);
        }
    }

    @Override
    public boolean upgradeInfoIsExponential(Holder<Upgrade> upgrade) {
        return upgrade.is(UpgradeIds.SPEED);
    }

    public ElectroSeparatorEnergyContainer energyContainer() {
        return energyContainer;
    }

    @Override
    public void nextMode(int tank) {
        if (tank == 0) {
            dumpLeft = dumpLeft.getNext();
            markForSave();
        } else if (tank == 1) {
            dumpRight = dumpRight.getNext();
            markForSave();
        }
    }

    @Override
    public void writeSustainedData(ValueOutput output) {
        super.writeSustainedData(output);
        NBTUtils.writeEnum(output, SerializationConstants.DUMP_LEFT, dumpLeft);
        NBTUtils.writeEnum(output, SerializationConstants.DUMP_RIGHT, dumpRight);
    }

    @Override
    public void readSustainedData(ValueInput input) {
        super.readSustainedData(input);
        NBTUtils.setEnumIfPresent(input, SerializationConstants.DUMP_LEFT, GasMode.BY_ID, mode -> dumpLeft = mode);
        NBTUtils.setEnumIfPresent(input, SerializationConstants.DUMP_RIGHT, GasMode.BY_ID, mode -> dumpRight = mode);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MekanismDataComponents.DUMP_MODE, dumpLeft);
        builder.set(MekanismDataComponents.SECONDARY_DUMP_MODE, dumpRight);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter input) {
        super.applyImplicitComponents(input);
        dumpLeft = input.getOrDefault(MekanismDataComponents.DUMP_MODE, dumpLeft);
        dumpRight = input.getOrDefault(MekanismDataComponents.SECONDARY_DUMP_MODE, dumpRight);
    }

    @Override
    public int getRedstoneLevel() {
        return ContainerType.FLUID.getRedstoneSignalFromContainer(fluidTank);
    }

    @Override
    protected boolean makesComparatorDirty(IContainerType<?, ?> type) {
        return type == ContainerType.FLUID;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableEnum.create(GasMode.BY_ID, GasMode.IDLE, () -> dumpLeft, value -> dumpLeft = value));
        container.track(SyncableEnum.create(GasMode.BY_ID, GasMode.IDLE, () -> dumpRight, value -> dumpRight = value));
        container.track(SyncableInt.create(this::getEnergyUsed, value -> clientEnergyUsed = value));
    }

    //Methods relating to IComputerTile
    @ComputerMethod(requiresPublicSecurity = true)
    void setLeftOutputDumpingMode(GasMode mode) throws ComputerException {
        validateSecurityIsPublic();
        if (dumpLeft != mode) {
            dumpLeft = mode;
            markForSave();
        }
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void incrementLeftOutputDumpingMode() throws ComputerException {
        validateSecurityIsPublic();
        nextMode(0);
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void decrementLeftOutputDumpingMode() throws ComputerException {
        validateSecurityIsPublic();
        dumpLeft = dumpLeft.getPrevious();
        markForSave();
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void setRightOutputDumpingMode(GasMode mode) throws ComputerException {
        validateSecurityIsPublic();
        if (dumpRight != mode) {
            dumpRight = mode;
            markForSave();
        }
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void incrementRightOutputDumpingMode() throws ComputerException {
        validateSecurityIsPublic();
        nextMode(1);
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void decrementRightOutputDumpingMode() throws ComputerException {
        validateSecurityIsPublic();
        dumpRight = dumpRight.getPrevious();
        markForSave();
    }
    //End methods IComputerTile

    public boolean isMakingHydrogen() {
        return isMakingHydrogen;
    }

    public int getBaselineMaxOperations() {
        return baselineMaxOperations;
    }
}
