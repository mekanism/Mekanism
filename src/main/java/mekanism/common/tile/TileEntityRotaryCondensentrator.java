package mekanism.common.tile;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
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
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityRotaryCondensentrator extends TileEntityMekanism implements ITileCachedRecipeHolder<RotaryRecipe>, IHasMode {

    private static final int CAPACITY = 10_000;

    public BasicGasTank gasTank;
    public BasicFluidTank fluidTank;
    /**
     * True: fluid -> gas
     *
     * False: gas -> fluid
     */
    public boolean mode;

    public int gasOutput = 256;

    private final IOutputHandler<@NonNull GasStack> gasOutputHandler;
    private final IOutputHandler<@NonNull FluidStack> fluidOutputHandler;
    private final IInputHandler<@NonNull FluidStack> fluidInputHandler;
    private final IInputHandler<@NonNull GasStack> gasInputHandler;

    private CachedRecipe<RotaryRecipe> cachedRecipe;

    public FloatingLong clientEnergyUsed = FloatingLong.ZERO;

    private MachineEnergyContainer<TileEntityRotaryCondensentrator> energyContainer;
    private GasInventorySlot gasInputSlot;
    private GasInventorySlot gasOutputSlot;
    private FluidInventorySlot fluidInputSlot;
    private OutputInventorySlot fluidOutputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityRotaryCondensentrator() {
        super(MekanismBlocks.ROTARY_CONDENSENTRATOR);
        gasInputHandler = InputHelper.getInputHandler(gasTank);
        fluidInputHandler = InputHelper.getInputHandler(fluidTank);
        gasOutputHandler = OutputHelper.getOutputHandler(gasTank);
        fluidOutputHandler = OutputHelper.getOutputHandler(fluidTank);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack> builder = ChemicalTankHelper.forSideGas(this::getDirection);
        //Only allow extraction
        builder.addTank(gasTank = BasicGasTank.create(CAPACITY, (gas, automationType) -> automationType == AutomationType.MANUAL || mode,
              (gas, automationType) -> automationType == AutomationType.INTERNAL || !mode, this::isValidGas, this), RelativeSide.LEFT);
        return builder.build();
    }

    private boolean isValidGas(@Nonnull Gas gas) {
        return containsRecipe(recipe -> recipe.hasGasToFluid() && recipe.getGasInput().testType(gas));
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
        builder.addTank(fluidTank = BasicFluidTank.create(CAPACITY, (fluid, automationType) -> automationType == AutomationType.MANUAL || !mode,
              (fluid, automationType) -> automationType == AutomationType.INTERNAL || mode, this::isValidFluid, this), RelativeSide.RIGHT);
        return builder.build();
    }

    private boolean isValidFluid(@Nonnull FluidStack fluidStack) {
        return containsRecipe(recipe -> recipe.hasFluidToGas() && recipe.getFluidInput().testType(fluidStack));
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
        //TODO: Fix these, not only is the naming bad, but there is a dedicated drain and empty slot for gas unlike how the fluid is
        builder.addSlot(gasInputSlot = GasInventorySlot.rotary(gasTank, () -> mode, this, 5, 25), RelativeSide.LEFT);
        builder.addSlot(gasOutputSlot = GasInventorySlot.rotary(gasTank, () -> mode, this, 5, 56), RelativeSide.LEFT);
        builder.addSlot(fluidInputSlot = FluidInventorySlot.rotary(fluidTank, () -> mode, this, 155, 25), RelativeSide.RIGHT);
        builder.addSlot(fluidOutputSlot = OutputInventorySlot.at(this, 155, 56), RelativeSide.RIGHT);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 155, 5), RelativeSide.FRONT, RelativeSide.BACK, RelativeSide.BOTTOM, RelativeSide.TOP);
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
            GasUtils.emit(EnumSet.of(getLeftSide()), gasTank, this, gasOutput);
        } else {//Gas to Fluid
            gasOutputSlot.fillTank();
            fluidInputSlot.drainTank(fluidOutputSlot);
            //TODO: Auto eject fluid?
        }
        FloatingLong prev = energyContainer.getEnergy().copyAsConst();
        cachedRecipe = getUpdatedCache(0);
        if (cachedRecipe != null) {
            cachedRecipe.process();
        }
        //Update amount of energy that actually got used, as if we are "near" full we may not have performed our max number of operations
        clientEnergyUsed = prev.subtract(energyContainer.getEnergy());
    }

    @Override
    public void nextMode() {
        mode = !mode;
        markDirty(false);
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        mode = nbtTags.getBoolean(NBTConstants.MODE);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.MODE, mode);
        return nbtTags;
    }

    @Override
    public int getRedstoneLevel() {
        if (mode) {
            return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
        }
        return MekanismUtils.redstoneLevelFromContents(gasTank.getStored(), gasTank.getCapacity());
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Nonnull
    @Override
    public MekanismRecipeType<RotaryRecipe> getRecipeType() {
        return MekanismRecipeType.ROTARY;
    }

    @Nullable
    @Override
    public CachedRecipe<RotaryRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public RotaryRecipe getRecipe(int cacheIndex) {
        if (mode) {//Fluid to Gas
            FluidStack fluid = fluidInputHandler.getInput();
            if (fluid.isEmpty()) {
                return null;
            }
            return findFirstRecipe(recipe -> recipe.test(fluid));
        }
        //Gas to Fluid
        GasStack gas = gasInputHandler.getInput();
        if (gas.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(gas));
    }

    public MachineEnergyContainer<TileEntityRotaryCondensentrator> getEnergyContainer() {
        return energyContainer;
    }

    @Nullable
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
                  int possibleProcess = (int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED));
                  if (mode) {
                      //Fluid to gas
                      return Math.min(Math.min(fluidTank.getFluidAmount(), gasTank.getNeeded()), possibleProcess);
                  }
                  //Gas to fluid
                  return Math.min(Math.min(gasTank.getStored(), fluidTank.getNeeded()), possibleProcess);
              });
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(() -> mode, value -> mode = value));
        container.track(SyncableFloatingLong.create(() -> clientEnergyUsed, value -> clientEnergyUsed = value));
    }
}