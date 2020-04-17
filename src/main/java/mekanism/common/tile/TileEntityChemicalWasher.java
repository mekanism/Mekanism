package mekanism.common.tile;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.FluidGasToGasCachedRecipe;
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
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityChemicalWasher extends TileEntityMekanism implements ITileCachedRecipeHolder<FluidGasToGasRecipe> {

    public static final long MAX_GAS = 10_000;
    public static final int MAX_FLUID = 10_000;
    public BasicFluidTank fluidTank;
    public BasicGasTank inputTank;
    public BasicGasTank outputTank;
    public long gasOutput = 256;

    public CachedRecipe<FluidGasToGasRecipe> cachedRecipe;

    public FloatingLong clientEnergyUsed = FloatingLong.ZERO;

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final IInputHandler<@NonNull FluidStack> fluidInputHandler;
    private final IInputHandler<@NonNull GasStack> gasInputHandler;

    private MachineEnergyContainer<TileEntityChemicalWasher> energyContainer;
    private FluidInventorySlot fluidSlot;
    private OutputInventorySlot fluidOutputSlot;
    private GasInventorySlot gasOutputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityChemicalWasher() {
        super(MekanismBlocks.CHEMICAL_WASHER);
        fluidInputHandler = InputHelper.getInputHandler(fluidTank);
        gasInputHandler = InputHelper.getInputHandler(inputTank);
        outputHandler = OutputHelper.getOutputHandler(outputTank);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack> builder = ChemicalTankHelper.forSideGas(this::getDirection);
        builder.addTank(inputTank = BasicGasTank.input(MAX_GAS, gas -> containsRecipe(recipe -> recipe.getGasInput().testType(gas)), this), RelativeSide.LEFT);
        builder.addTank(outputTank = BasicGasTank.output(MAX_GAS, this), RelativeSide.RIGHT);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
        builder.addTank(fluidTank = BasicFluidTank.input(MAX_FLUID, fluid -> containsRecipe(recipe -> recipe.getFluidInput().testType(fluid)), this),
              RelativeSide.TOP);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(fluidSlot = FluidInventorySlot.fill(fluidTank, this, 180, 71), RelativeSide.LEFT);
        //Output slot for the fluid container that was used as an input
        builder.addSlot(fluidOutputSlot = OutputInventorySlot.at(this, 180, 102), RelativeSide.TOP);
        builder.addSlot(gasOutputSlot = GasInventorySlot.drain(outputTank, this, 155, 56), RelativeSide.RIGHT);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 155, 5));
        gasOutputSlot.setSlotOverlay(SlotOverlay.MINUS);
        fluidSlot.setSlotType(ContainerSlotType.INPUT);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        fluidSlot.fillTank(fluidOutputSlot);
        gasOutputSlot.drainTank();
        FloatingLong prev = energyContainer.getEnergy().copyAsConst();
        cachedRecipe = getUpdatedCache(0);
        if (cachedRecipe != null) {
            cachedRecipe.process();
        }
        //Update amount of energy that actually got used, as if we are "near" full we may not have performed our max number of operations
        clientEnergyUsed = prev.subtract(energyContainer.getEnergy());
        GasUtils.emit(EnumSet.of(getRightSide()), outputTank, this, gasOutput);
    }

    @Nonnull
    @Override
    public MekanismRecipeType<FluidGasToGasRecipe> getRecipeType() {
        return MekanismRecipeType.WASHING;
    }

    @Nullable
    @Override
    public CachedRecipe<FluidGasToGasRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public FluidGasToGasRecipe getRecipe(int cacheIndex) {
        GasStack gasStack = gasInputHandler.getInput();
        if (gasStack.isEmpty()) {
            return null;
        }
        FluidStack fluid = fluidInputHandler.getInput();
        if (fluid.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(fluid, gasStack));
    }

    @Nullable
    @Override
    public CachedRecipe<FluidGasToGasRecipe> createNewCachedRecipe(@Nonnull FluidGasToGasRecipe recipe, int cacheIndex) {
        return new FluidGasToGasCachedRecipe(recipe, fluidInputHandler, gasInputHandler, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setOnFinish(() -> markDirty(false))
              .setPostProcessOperations(currentMax -> {
                  if (currentMax <= 0) {
                      //Short circuit that if we already can't perform any outputs, just return
                      return currentMax;
                  }
                  return Math.min((int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED)), currentMax);
              });
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(inputTank.getStored(), inputTank.getCapacity());
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    public MachineEnergyContainer<TileEntityChemicalWasher> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableFloatingLong.create(() -> clientEnergyUsed, value -> clientEnergyUsed = value));
    }
}