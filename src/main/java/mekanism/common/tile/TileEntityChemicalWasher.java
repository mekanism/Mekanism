package mekanism.common.tile;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.TileNetworkList;
import mekanism.api.chemical.ChemicalAction;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.FluidGasToGasCachedRecipe;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.MekanismBlock;
import mekanism.common.Upgrade;
import mekanism.common.Upgrade.IUpgradeInfoHandler;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ITankManager;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.tile.prefab.TileEntityMachine;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityChemicalWasher extends TileEntityMachine implements IGasHandler, IFluidHandlerWrapper, ISustainedData, IUpgradeInfoHandler, ITankManager,
      IComparatorSupport, ITileCachedRecipeHolder<FluidGasToGasRecipe> {

    public static final int MAX_GAS = 10000;
    public static final int MAX_FLUID = 10000;
    public FluidTank fluidTank = new FluidTank(MAX_FLUID);
    public GasTank inputTank = new GasTank(MAX_GAS);
    public GasTank outputTank = new GasTank(MAX_GAS);
    public int gasOutput = 256;

    public CachedRecipe<FluidGasToGasRecipe> cachedRecipe;

    private int currentRedstoneLevel;
    public double clientEnergyUsed;

    public TileEntityChemicalWasher() {
        super(MekanismBlock.CHEMICAL_WASHER, 4);
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            ChargeUtils.discharge(3, this);
            ItemStack fluidInputStack = inventory.get(0);
            if (!fluidInputStack.isEmpty() && isFluidInputItem(fluidInputStack)) {
                fluidTank.fill(FluidContainerUtils.extractFluid(fluidTank, this, 0), FluidAction.EXECUTE);
            }
            TileUtils.drawGas(getInventory().get(2), outputTank);
            double prev = getEnergy();
            cachedRecipe = getUpdatedCache(0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
            //Update amount of energy that actually got used, as if we are "near" full we may not have performed our max number of operations
            clientEnergyUsed = prev - getEnergy();
            TileUtils.emitGas(this, outputTank, gasOutput, getRightSide());
            World world = getWorld();
            if (world != null) {
                int newRedstoneLevel = getRedstoneLevel();
                if (newRedstoneLevel != currentRedstoneLevel) {
                    world.updateComparatorOutputLevel(pos, getBlockType());
                    currentRedstoneLevel = newRedstoneLevel;
                }
            }
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
        GasStack gasStack = inputTank.getStack();
        FluidStack fluid = fluidTank.getFluid();
        return gasStack.isEmpty() || fluid.isEmpty() ? null : findFirstRecipe(recipe -> recipe.test(fluid, gasStack));
    }

    @Nullable
    @Override
    public CachedRecipe<FluidGasToGasRecipe> createNewCachedRecipe(@Nonnull FluidGasToGasRecipe recipe, int cacheIndex) {
        return new FluidGasToGasCachedRecipe(recipe, InputHelper.getInputHandler(fluidTank), InputHelper.getInputHandler(inputTank), OutputHelper.getOutputHandler(outputTank))
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(this::getEnergyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setOnFinish(this::markDirty)
              .setPostProcessOperations(currentMax -> {
                  if (currentMax == 0) {
                      //Short circuit that if we already can't perform any outputs, just return
                      return 0;
                  }
                  return Math.min((int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED)), currentMax);
              });
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            clientEnergyUsed = dataStream.readDouble();
            TileUtils.readTankData(dataStream, fluidTank);
            TileUtils.readTankData(dataStream, inputTank);
            TileUtils.readTankData(dataStream, outputTank);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(clientEnergyUsed);
        TileUtils.addTankData(data, fluidTank);
        TileUtils.addTankData(data, inputTank);
        TileUtils.addTankData(data, outputTank);
        return data;
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

    public GasTank getTank(Direction side) {
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
    public int receiveGas(Direction side, @Nonnull GasStack stack, ChemicalAction action) {
        if (canReceiveGas(side, stack.getType())) {
            return getTank(side).fill(stack, action);
        }
        return 0;
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, ChemicalAction action) {
        if (canDrawGas(side, MekanismAPI.EMPTY_GAS)) {
            return getTank(side).drain(amount, action);
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

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 0) {
            return isFluidInputItem(itemstack);
        } else if (slotID == 2) {
            return ChargeUtils.canBeDischarged(itemstack);
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        if (slotID == 1) {
            return !itemstack.isEmpty() && itemstack.getItem() instanceof IGasItem && ((IGasItem) itemstack.getItem()).canProvideGas(itemstack, MekanismAPI.EMPTY_GAS);
        } else if (slotID == 2) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        }
        return false;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        if (side == getLeftSide()) {
            return new int[]{0};
        } else if (side == getRightSide()) {
            return new int[]{1};
        } else if (side.getAxis() == Axis.Y) {
            return new int[2];
        }
        return InventoryUtils.EMPTY;
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
        if (!fluidTank.getFluid().isEmpty()) {
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
    public List<ITextComponent> getInfo(Upgrade upgrade) {
        return upgrade == Upgrade.SPEED ? upgrade.getExpScaledInfo(this) : upgrade.getMultScaledInfo(this);
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{fluidTank, inputTank, outputTank};
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(inputTank.getStored(), inputTank.getCapacity());
    }

    public boolean isFluidInputItem(ItemStack itemStack) {
        return new LazyOptionalHelper<>(FluidUtil.getFluidContained(itemStack)).matches(
              fluidStack -> !fluidStack.isEmpty() && containsRecipe(recipe -> recipe.getFluidInput().testType(fluidStack)));
    }
}