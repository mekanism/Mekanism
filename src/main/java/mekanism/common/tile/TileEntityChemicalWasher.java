package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.BasicGasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.FluidGasToGasCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableFluidStack;
import mekanism.common.inventory.container.sync.SyncableGasStack;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityChemicalWasher extends TileEntityMekanism implements IGasHandler, IFluidHandlerWrapper, ISustainedData, ITankManager,
      ITileCachedRecipeHolder<FluidGasToGasRecipe> {

    public static final int MAX_GAS = 10_000;
    public static final int MAX_FLUID = 10_000;
    public FluidTank fluidTank;
    public BasicGasTank inputTank;
    public BasicGasTank outputTank;
    public int gasOutput = 256;

    public CachedRecipe<FluidGasToGasRecipe> cachedRecipe;

    public double clientEnergyUsed;

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final IInputHandler<@NonNull FluidStack> fluidInputHandler;
    private final IInputHandler<@NonNull GasStack> gasInputHandler;

    private FluidInventorySlot fluidSlot;
    private GasInventorySlot gasOutputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityChemicalWasher() {
        super(MekanismBlocks.CHEMICAL_WASHER);
        fluidInputHandler = InputHelper.getInputHandler(fluidTank, 0);
        gasInputHandler = InputHelper.getInputHandler(inputTank);
        outputHandler = OutputHelper.getOutputHandler(outputTank);
    }

    @Override
    protected void presetVariables() {
        fluidTank = new FluidTank(MAX_FLUID);
        inputTank = new BasicGasTank(MAX_GAS);
        outputTank = new BasicGasTank(MAX_GAS);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(fluidSlot = FluidInventorySlot.fill(fluidTank, fluid -> containsRecipe(recipe -> recipe.getFluidInput().testType(fluid)), this, 180, 71),
              RelativeSide.LEFT);
        //Output slot for the fluid container that was used as an input
        builder.addSlot(OutputInventorySlot.at(this, 180, 102), RelativeSide.TOP);
        builder.addSlot(gasOutputSlot = GasInventorySlot.drain(outputTank, this, 155, 56), RelativeSide.RIGHT);
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 155, 5));
        gasOutputSlot.setSlotOverlay(SlotOverlay.MINUS);
        fluidSlot.setSlotType(ContainerSlotType.INPUT);
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            energySlot.discharge(this);
            //TODO: Fix this not moving the item to the output slot
            fluidSlot.fillTank();
            gasOutputSlot.drainTank();
            double prev = getEnergy();
            cachedRecipe = getUpdatedCache(0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
            //Update amount of energy that actually got used, as if we are "near" full we may not have performed our max number of operations
            clientEnergyUsed = prev - getEnergy();
            TileUtils.emitGas(this, outputTank, gasOutput, getRightSide());
        }
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
              .setEnergyRequirements(this::getEnergyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setOnFinish(this::markDirty)
              .setPostProcessOperations(currentMax -> {
                  if (currentMax <= 0) {
                      //Short circuit that if we already can't perform any outputs, just return
                      return currentMax;
                  }
                  return Math.min((int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED)), currentMax);
              });
    }

    public boolean isValidGas(@Nonnull Gas gas) {
        return containsRecipe(recipe -> recipe.getGasInput().testType(gas));
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        fluidTank.readFromNBT(nbtTags.getCompound("leftTank"));
        inputTank.read(nbtTags.getCompound("rightTank"));
        outputTank.read(nbtTags.getCompound("centerTank"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.put("leftTank", fluidTank.writeToNBT(new CompoundNBT()));
        nbtTags.put("rightTank", inputTank.write(new CompoundNBT()));
        nbtTags.put("centerTank", outputTank.write(new CompoundNBT()));
        return nbtTags;
    }

    public BasicGasTank getTank(Direction side) {
        if (side == getLeftSide()) {
            return inputTank;
        } else if (side == getRightSide()) {
            return outputTank;
        }
        return null;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        if (getTank(side) == inputTank) {
            return getTank(side).canReceive(type) && containsRecipe(recipe -> recipe.getGasInput().testType(type));
        }
        return false;
    }


    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, Action action) {
        if (canReceiveGas(side, stack.getType())) {
            return getTank(side).insert(stack, action);
        }
        return 0;
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, Action action) {
        if (canDrawGas(side, MekanismAPI.EMPTY_GAS)) {
            return getTank(side).extract(amount, action);
        }
        return GasStack.EMPTY;
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        return getTank(side) == outputTank && getTank(side).canDraw(type);
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{inputTank, outputTank};
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        }
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> new FluidHandlerWrapper(this, side)));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return side != null && getTank(side) == null;
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return side == getDirection() || side == getOppositeDirection();
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        return fluidTank.fill(resource, fluidAction);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        if (from != Direction.UP) {
            return false;
        }
        FluidStack currentFluid = fluidTank.getFluid();
        if (currentFluid.isEmpty()) {
            //If we don't have a fluid currently stored, then check if the fluid wanting to be input is valid for this machine
            return containsRecipe(recipe -> recipe.getFluidInput().testType(fluid));
        }
        //Otherwise return true if the fluid is the same as the one we already have stored
        return currentFluid.isFluidEqual(fluid);
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        if (from == Direction.UP) {
            return new IFluidTank[]{fluidTank};
        }
        return PipeUtils.EMPTY;
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return new IFluidTank[]{fluidTank};
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (!fluidTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "fluidTank", fluidTank.getFluid().writeToNBT(new CompoundNBT()));
        }
        if (!inputTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "inputTank", inputTank.getStack().write(new CompoundNBT()));
        }
        if (!outputTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "outputTank", outputTank.getStack().write(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(ItemDataUtils.getCompound(itemStack, "fluidTank")));
        inputTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "inputTank")));
        outputTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "outputTank")));
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put("leftTank", "fluidTank");
        remap.put("rightTank.stored", "inputTank");
        remap.put("centerTank.stored", "outputTank");
        return remap;
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{fluidTank, inputTank, outputTank};
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

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(() -> clientEnergyUsed, value -> clientEnergyUsed = value));
        container.track(SyncableFluidStack.create(fluidTank));
        container.track(SyncableGasStack.create(inputTank));
        container.track(SyncableGasStack.create(outputTank));
    }
}