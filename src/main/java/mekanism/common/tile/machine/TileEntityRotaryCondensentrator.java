package mekanism.common.tile.machine;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.RotaryCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
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
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.RotaryInputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityRotaryCondensentrator extends TileEntityRecipeMachine<RotaryRecipe> implements IHasMode {

    private static final int CAPACITY = 10_000;

    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getGas", "getGasCapacity", "getGasNeeded", "getGasFilledPercentage"})
    public IGasTank gasTank;
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getFluid", "getFluidCapacity", "getFluidNeeded", "getFluidFilledPercentage"})
    public BasicFluidTank fluidTank;
    /**
     * True: fluid -> gas
     *
     * False: gas -> fluid
     */
    public boolean mode;

    private final IOutputHandler<@NonNull GasStack> gasOutputHandler;
    private final IOutputHandler<@NonNull FluidStack> fluidOutputHandler;
    private final IInputHandler<@NonNull FluidStack> fluidInputHandler;
    private final IInputHandler<@NonNull GasStack> gasInputHandler;

    private FloatingLong clientEnergyUsed = FloatingLong.ZERO;

    private MachineEnergyContainer<TileEntityRotaryCondensentrator> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getGasItemInput")
    private GasInventorySlot gasInputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getGasItemOutput")
    private GasInventorySlot gasOutputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFluidItemInput")
    private FluidInventorySlot fluidInputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFluidItemOutput")
    private OutputInventorySlot fluidOutputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityRotaryCondensentrator() {
        super(MekanismBlocks.ROTARY_CONDENSENTRATOR);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.GAS, TransmissionType.FLUID, TransmissionType.ENERGY);
        configComponent.setupItemIOConfig(Arrays.asList(gasInputSlot, fluidInputSlot), Arrays.asList(gasOutputSlot, fluidOutputSlot), energySlot, true);
        configComponent.setupIOConfig(TransmissionType.GAS, gasTank, RelativeSide.LEFT, true).setEjecting(true);
        configComponent.setupIOConfig(TransmissionType.FLUID, fluidTank, RelativeSide.RIGHT, true).setEjecting(true);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.GAS, TransmissionType.FLUID)
              .setCanEject(transmissionType -> {
                  if (transmissionType == TransmissionType.GAS) {
                      return mode;
                  } else if (transmissionType == TransmissionType.FLUID) {
                      return !mode;
                  }
                  return true;
              });

        gasInputHandler = InputHelper.getInputHandler(gasTank);
        fluidInputHandler = InputHelper.getInputHandler(fluidTank);
        gasOutputHandler = OutputHelper.getOutputHandler(gasTank);
        fluidOutputHandler = OutputHelper.getOutputHandler(fluidTank);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        //Only allow extraction
        builder.addTank(gasTank = ChemicalTankBuilder.GAS.create(CAPACITY, (gas, automationType) -> automationType == AutomationType.MANUAL || mode,
              (gas, automationType) -> automationType == AutomationType.INTERNAL || !mode, this::isValidGas, recipeCacheLookupMonitor));
        return builder.build();
    }

    private boolean isValidGas(@Nonnull Gas gas) {
        return getRecipeType().getInputCache().containsInput(level, gas.getStack(1));
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addTank(fluidTank = BasicFluidTank.create(CAPACITY, (fluid, automationType) -> automationType == AutomationType.MANUAL || !mode,
              (fluid, automationType) -> automationType == AutomationType.INTERNAL || mode, this::isValidFluid, recipeCacheLookupMonitor));
        return builder.build();
    }

    private boolean isValidFluid(@Nonnull FluidStack fluidStack) {
        return getRecipeType().getInputCache().containsInput(level, fluidStack);
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
        BooleanSupplier modeSupplier = () -> mode;
        builder.addSlot(gasInputSlot = GasInventorySlot.rotaryDrain(gasTank, modeSupplier, this, 5, 25));
        builder.addSlot(gasOutputSlot = GasInventorySlot.rotaryFill(gasTank, modeSupplier, this, 5, 56));
        builder.addSlot(fluidInputSlot = FluidInventorySlot.rotary(fluidTank, modeSupplier, this, 155, 25));
        builder.addSlot(fluidOutputSlot = OutputInventorySlot.at(this, 155, 56));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, this, 155, 5));
        gasInputSlot.setSlotType(ContainerSlotType.INPUT);
        gasInputSlot.setSlotOverlay(SlotOverlay.PLUS);
        gasOutputSlot.setSlotType(ContainerSlotType.OUTPUT);
        gasOutputSlot.setSlotOverlay(SlotOverlay.MINUS);
        fluidInputSlot.setSlotType(ContainerSlotType.INPUT);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        if (mode) {//Fluid to Gas
            fluidInputSlot.fillTank(fluidOutputSlot);
            gasInputSlot.drainTank();
        } else {//Gas to Fluid
            gasOutputSlot.fillTank();
            fluidInputSlot.drainTank(fluidOutputSlot);
        }
        clientEnergyUsed = recipeCacheLookupMonitor.updateAndProcess(energyContainer);
    }

    @Override
    public void nextMode() {
        mode = !mode;
        markDirty(false);
    }

    @Nonnull
    @ComputerMethod(nameOverride = "getEnergyUsage")
    public FloatingLong getEnergyUsed() {
        return clientEnergyUsed;
    }

    @Override
    protected void loadGeneralPersistentData(CompoundNBT data) {
        super.loadGeneralPersistentData(data);
        NBTUtils.setBooleanIfPresent(data, NBTConstants.MODE, value -> mode = value);
    }

    @Override
    protected void addGeneralPersistentData(CompoundNBT data) {
        super.addGeneralPersistentData(data);
        data.putBoolean(NBTConstants.MODE, mode);
    }

    @Override
    public int getRedstoneLevel() {
        if (mode) {
            return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
        }
        return MekanismUtils.redstoneLevelFromContents(gasTank.getStored(), gasTank.getCapacity());
    }

    @Nonnull
    @Override
    public MekanismRecipeType<RotaryRecipe, RotaryInputRecipeCache> getRecipeType() {
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

    @Nonnull
    @Override
    public CachedRecipe<RotaryRecipe> createNewCachedRecipe(@Nonnull RotaryRecipe recipe, int cacheIndex) {
        return new RotaryCachedRecipe(recipe, fluidInputHandler, gasInputHandler, gasOutputHandler, fluidOutputHandler, () -> mode)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setOnFinish(() -> markDirty(false))
              .setPostProcessOperations(currentMax -> {
                  if (currentMax <= 0) {
                      //Short circuit that if we already can't perform any outputs, just return
                      return currentMax;
                  }
                  int possibleProcess = Math.min((int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED)), currentMax);
                  if (mode) {
                      //Fluid to gas
                      return Math.min(Math.min(fluidTank.getFluidAmount(), MathUtils.clampToInt(gasTank.getNeeded())), possibleProcess);
                  }
                  //Gas to fluid
                  return Math.min(Math.min(MathUtils.clampToInt(gasTank.getStored()), fluidTank.getNeeded()), possibleProcess);
              });
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(() -> mode, value -> mode = value));
        container.track(SyncableFloatingLong.create(this::getEnergyUsed, value -> clientEnergyUsed = value));
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private boolean isCondensentrating() {
        return !mode;
    }

    @ComputerMethod
    private void setCondensentrating(boolean value) throws ComputerException {
        validateSecurityIsPublic();
        if (mode != value) {
            mode = value;
            markDirty(false);
        }
    }
    //End methods IComputerTile
}