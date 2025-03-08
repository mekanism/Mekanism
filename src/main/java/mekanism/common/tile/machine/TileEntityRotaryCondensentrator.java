package mekanism.common.tile.machine;

import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.Upgrade;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.RotaryCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.recipes.vanilla_input.RotaryRecipeInput;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.integration.computer.computercraft.ComputerConstants;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.RotaryInputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityRotaryCondensentrator extends TileEntityRecipeMachine<RotaryRecipe> implements IHasMode {

    public static final RecipeError NOT_ENOUGH_FLUID_INPUT_ERROR = RecipeError.create();
    public static final RecipeError NOT_ENOUGH_GAS_INPUT_ERROR = RecipeError.create();
    public static final RecipeError NOT_ENOUGH_SPACE_GAS_OUTPUT_ERROR = RecipeError.create();
    public static final RecipeError NOT_ENOUGH_SPACE_FLUID_OUTPUT_ERROR = RecipeError.create();
    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE,
          NOT_ENOUGH_FLUID_INPUT_ERROR,
          NOT_ENOUGH_GAS_INPUT_ERROR,
          NOT_ENOUGH_SPACE_GAS_OUTPUT_ERROR,
          NOT_ENOUGH_SPACE_FLUID_OUTPUT_ERROR,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );
    public static final int CAPACITY = 10 * FluidType.BUCKET_VOLUME;

    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getGas", "getGasCapacity", "getGasNeeded",
                                                                                        "getGasFilledPercentage"}, docPlaceholder = "gas tank")
    public IChemicalTank gasTank;
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getFluid", "getFluidCapacity", "getFluidNeeded",
                                                                                     "getFluidFilledPercentage"}, docPlaceholder = "fluid tank")
    public BasicFluidTank fluidTank;
    /**
     * True: fluid -> chemical
     * <p>
     * False: chemical -> fluid
     */
    private boolean mode;

    private final IOutputHandler<@NotNull ChemicalStack> gasOutputHandler;
    private final IOutputHandler<@NotNull FluidStack> fluidOutputHandler;
    private final IInputHandler<@NotNull FluidStack> fluidInputHandler;
    private final IInputHandler<@NotNull ChemicalStack> gasInputHandler;

    private long clientEnergyUsed = 0;
    private int baselineMaxOperations = 1;

    private MachineEnergyContainer<TileEntityRotaryCondensentrator> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getGasItemInput", docPlaceholder = "gas item input slot")
    ChemicalInventorySlot gasInputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getGasItemOutput", docPlaceholder = "gas item output slot")
    ChemicalInventorySlot gasOutputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFluidItemInput", docPlaceholder = "fluid item input slot")
    FluidInventorySlot fluidInputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFluidItemOutput", docPlaceholder = "fluid item ouput slot")
    OutputInventorySlot fluidOutputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    public TileEntityRotaryCondensentrator(BlockPos pos, BlockState state) {
        super(MekanismBlocks.ROTARY_CONDENSENTRATOR, pos, state, TRACKED_ERROR_TYPES);
        configComponent.setupItemIOConfig(List.of(gasInputSlot, fluidInputSlot), List.of(gasOutputSlot, fluidOutputSlot), energySlot, true);
        configComponent.setupIOConfig(TransmissionType.CHEMICAL, gasTank, RelativeSide.LEFT, true);
        configComponent.setupIOConfig(TransmissionType.FLUID, fluidTank, RelativeSide.RIGHT, true);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.CHEMICAL, TransmissionType.FLUID)
              .setCanEject(transmissionType -> {
                  if (transmissionType == TransmissionType.CHEMICAL) {
                      return mode;
                  } else if (transmissionType == TransmissionType.FLUID) {
                      return !mode;
                  }
                  return true;
              });

        gasInputHandler = InputHelper.getInputHandler(gasTank, NOT_ENOUGH_GAS_INPUT_ERROR);
        fluidInputHandler = InputHelper.getInputHandler(fluidTank, NOT_ENOUGH_FLUID_INPUT_ERROR);
        gasOutputHandler = OutputHelper.getOutputHandler(gasTank, NOT_ENOUGH_SPACE_GAS_OUTPUT_ERROR);
        fluidOutputHandler = OutputHelper.getOutputHandler(fluidTank, NOT_ENOUGH_SPACE_FLUID_OUTPUT_ERROR);
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);
        //Only allow extraction
        builder.addTank(gasTank = BasicChemicalTank.createModern(CAPACITY, (gas, automationType) -> automationType == AutomationType.MANUAL || mode,
              (gas, automationType) -> automationType == AutomationType.INTERNAL || !mode, this::isValidGas, ChemicalAttributeValidator.ALWAYS_ALLOW, recipeCacheListener));
        return builder.build();
    }

    private boolean isValidGas(@NotNull ChemicalStack chemical) {
        return getRecipeType().getInputCache().containsInput(level, chemical);
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this);
        builder.addTank(fluidTank = BasicFluidTank.create(CAPACITY, (fluid, automationType) -> automationType == AutomationType.MANUAL || !mode,
              (fluid, automationType) -> automationType == AutomationType.INTERNAL || mode, this::isValidFluid, recipeCacheListener));
        return builder.build();
    }

    private boolean isValidFluid(@NotNull FluidStack fluidStack) {
        return getRecipeType().getInputCache().containsInput(level, fluidStack);
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, recipeCacheUnpauseListener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
        BooleanSupplier modeSupplier = this::getMode;
        builder.addSlot(gasInputSlot = ChemicalInventorySlot.rotaryDrain(gasTank, modeSupplier, listener, 5, 25));
        builder.addSlot(gasOutputSlot = ChemicalInventorySlot.rotaryFill(gasTank, modeSupplier, listener, 5, 56));
        builder.addSlot(fluidInputSlot = FluidInventorySlot.rotary(fluidTank, modeSupplier, listener, 155, 25));
        builder.addSlot(fluidOutputSlot = OutputInventorySlot.at(listener, 155, 56));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 155, 5));
        gasInputSlot.setSlotType(ContainerSlotType.INPUT);
        gasInputSlot.setSlotOverlay(SlotOverlay.PLUS);
        gasOutputSlot.setSlotType(ContainerSlotType.OUTPUT);
        gasOutputSlot.setSlotOverlay(SlotOverlay.MINUS);
        fluidInputSlot.setSlotType(ContainerSlotType.INPUT);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        if (mode) {//Fluid to Gas
            fluidInputSlot.fillTank(fluidOutputSlot);
            gasInputSlot.drainTank();
        } else {//Gas to Fluid
            gasOutputSlot.fillTank();
            fluidInputSlot.drainTank(fluidOutputSlot);
        }
        clientEnergyUsed = recipeCacheLookupMonitor.updateAndProcess(energyContainer);
        return sendUpdatePacket;
    }

    public boolean getMode() {
        return mode;
    }

    @Override
    public void nextMode() {
        mode = !mode;
        setChanged();
    }

    @Override
    public void previousMode() {
        //We only have two modes just flip it
        nextMode();
    }

    @ComputerMethod(nameOverride = "getEnergyUsage", methodDescription = ComputerConstants.DESCRIPTION_GET_ENERGY_USAGE)
    public long getEnergyUsed() {
        return clientEnergyUsed;
    }

    @Override
    public void readSustainedData(HolderLookup.Provider provider, @NotNull CompoundTag data) {
        super.readSustainedData(provider, data);
        NBTUtils.setBooleanIfPresent(data, SerializationConstants.MODE, value -> mode = value);
    }

    @Override
    public void writeSustainedData(HolderLookup.Provider provider, CompoundTag data) {
        super.writeSustainedData(provider, data);
        data.putBoolean(SerializationConstants.MODE, mode);
    }

    @Override
    protected void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        mode = input.getOrDefault(MekanismDataComponents.ROTARY_MODE, mode);
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MekanismDataComponents.ROTARY_MODE, mode);
    }

    @Override
    public int getRedstoneLevel() {
        if (mode) {
            return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
        }
        return MekanismUtils.redstoneLevelFromContents(gasTank.getStored(), gasTank.getCapacity());
    }

    @Override
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return type == ContainerType.FLUID || type == ContainerType.CHEMICAL;
    }

    @NotNull
    @Override
    public IMekanismRecipeTypeProvider<RotaryRecipeInput, RotaryRecipe, RotaryInputRecipeCache> getRecipeType() {
        return MekanismRecipeType.ROTARY;
    }

    @Nullable
    @Override
    public RotaryRecipe getRecipe(int cacheIndex) {
        RotaryInputRecipeCache inputCache = getRecipeType().getInputCache();
        return mode ? inputCache.findFirstRecipe(level, fluidInputHandler.getInput()) : inputCache.findFirstRecipe(level, gasInputHandler.getInput());
    }

    public MachineEnergyContainer<TileEntityRotaryCondensentrator> getEnergyContainer() {
        return energyContainer;
    }

    @NotNull
    @Override
    public CachedRecipe<RotaryRecipe> createNewCachedRecipe(@NotNull RotaryRecipe recipe, int cacheIndex) {
        return new RotaryCachedRecipe(recipe, recheckAllRecipeErrors, fluidInputHandler, gasInputHandler, gasOutputHandler, fluidOutputHandler, this::getMode)
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
            baselineMaxOperations = (int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED));
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(this::getMode, value -> mode = value));
        container.track(SyncableLong.create(this::getEnergyUsed, value -> clientEnergyUsed = value));
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    boolean isCondensentrating() {
        return !mode;
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void setCondensentrating(boolean value) throws ComputerException {
        validateSecurityIsPublic();
        if (mode != value) {
            mode = value;
            setChanged();
        }
    }
    //End methods IComputerTile
}
