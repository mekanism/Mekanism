package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.FluidGasToGasCachedRecipe;
import mekanism.api.recipes.cache.ICachedRecipeHolder;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.Upgrade;
import mekanism.common.Upgrade.IUpgradeInfoHandler;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITankManager;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.tile.prefab.TileEntityMachine;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.FluidChecker;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityChemicalWasher extends TileEntityMachine implements IGasHandler, IFluidHandlerWrapper, ISustainedData, IUpgradeInfoHandler, ITankManager,
      IComparatorSupport, ICachedRecipeHolder<FluidGasToGasRecipe> {

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
        super("machine.washer", MachineType.CHEMICAL_WASHER, 4);
        inventory = NonNullList.withSize(5, ItemStack.EMPTY);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            ChargeUtils.discharge(3, this);
            manageBuckets();
            TileUtils.drawGas(inventory.get(2), outputTank);
            double prev = getEnergy();
            cachedRecipe = getUpdatedCache(cachedRecipe, 0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
            //Update amount of energy that actually got used, as if we are "near" full we may not have performed our max number of operations
            clientEnergyUsed = prev - getEnergy();

            TileUtils.emitGas(this, outputTank, gasOutput, MekanismUtils.getRight(facing));
            int newRedstoneLevel = getRedstoneLevel();
            if (newRedstoneLevel != currentRedstoneLevel) {
                world.updateComparatorOutputLevel(pos, getBlockType());
                currentRedstoneLevel = newRedstoneLevel;
            }
        }
    }

    @Nonnull
    @Override
    public Recipe<FluidGasToGasRecipe> getRecipes() {
        return Recipe.CHEMICAL_WASHER;
    }

    @Nullable
    @Override
    public FluidGasToGasRecipe getRecipe(int cacheIndex) {
        GasStack gasStack = inputTank.getGas();
        FluidStack fluid = fluidTank.getFluid();
        return gasStack == null || gasStack.amount == 0 || fluid == null || fluid.amount == 0 ? null : getRecipes().findFirst(recipe -> recipe.test(fluid, gasStack));
    }

    @Nullable
    @Override
    public FluidGasToGasCachedRecipe createNewCachedRecipe(@Nonnull FluidGasToGasRecipe recipe, int cacheIndex) {
        int maxOperations = getUpgradedUsage(recipe);
        return new FluidGasToGasCachedRecipe(recipe, () -> MekanismUtils.canFunction(this), () -> energyPerTick, this::getEnergy, () -> 1,
              this::setActive, energy -> setEnergy(getEnergy() - energy), this::markDirty, () -> fluidTank, () -> inputTank, () -> maxOperations,
              OutputHelper.getAddToOutput(outputTank));
    }

    private void manageBuckets() {
        if (FluidContainerUtils.isFluidContainer(inventory.get(0))) {
            //TODO: Instead of water, use the recipe's cleansing fluid
            FluidContainerUtils.handleContainerItemEmpty(this, fluidTank, 0, 1, FluidChecker.check(FluidRegistry.WATER));
        }
    }

    private int getUpgradedUsage(FluidGasToGasRecipe recipe) {
        int possibleProcess = (int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED));
        possibleProcess = Math.min(Math.min(inputTank.getStored(), outputTank.getNeeded()), possibleProcess);
        possibleProcess = Math.min((int) (getEnergy() / energyPerTick), possibleProcess);
        //TODO: Instead of water, use the recipe's cleansing fluid amount

        return Math.min(fluidTank.getFluidAmount() / WATER_USAGE, possibleProcess);
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
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
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);
        fluidTank.readFromNBT(nbtTags.getCompoundTag("leftTank"));
        inputTank.read(nbtTags.getCompoundTag("rightTank"));
        outputTank.read(nbtTags.getCompoundTag("centerTank"));
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setTag("leftTank", fluidTank.writeToNBT(new NBTTagCompound()));
        nbtTags.setTag("rightTank", inputTank.write(new NBTTagCompound()));
        nbtTags.setTag("centerTank", outputTank.write(new NBTTagCompound()));
        return nbtTags;
    }

    @Override
    public boolean canSetFacing(@Nonnull EnumFacing facing) {
        return facing != EnumFacing.DOWN && facing != EnumFacing.UP;
    }

    public GasTank getTank(EnumFacing side) {
        if (side == MekanismUtils.getLeft(facing)) {
            return inputTank;
        } else if (side == MekanismUtils.getRight(facing)) {
            return outputTank;
        }
        return null;
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        if (getTank(side) == inputTank) {
            return getTank(side).canReceive(type) && getRecipes().contains(recipe -> recipe.getGasInput().testType(type));
        }
        return false;
    }


    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        if (canReceiveGas(side, stack != null ? stack.getGas() : null)) {
            return getTank(side).receive(stack, doTransfer);
        }
        return 0;
    }

    @Override
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        if (canDrawGas(side, null)) {
            return getTank(side).draw(amount, doTransfer);
        }
        return null;
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
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
            //TODO: Instead of water, use the recipe's cleansing fluid
            return FluidUtil.getFluidContained(itemstack) != null && FluidUtil.getFluidContained(itemstack).getFluid() == FluidRegistry.WATER;
        } else if (slotID == 2) {
            return ChargeUtils.canBeDischarged(itemstack);
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        if (slotID == 1) {
            return !itemstack.isEmpty() && itemstack.getItem() instanceof IGasItem && ((IGasItem) itemstack.getItem()).canProvideGas(itemstack, null);
        } else if (slotID == 2) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        }
        return false;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        if (side == MekanismUtils.getLeft(facing)) {
            return new int[]{0};
        } else if (side == MekanismUtils.getRight(facing)) {
            return new int[]{1};
        } else if (side.getAxis() == Axis.Y) {
            return new int[2];
        }
        return InventoryUtils.EMPTY;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return false;
        }
        return capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return null;
        } else if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.cast(this);
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new FluidHandlerWrapper(this, side));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return side != null && getTank(side) == null;
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return side == facing || side == facing.getOpposite();
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public int fill(EnumFacing from, @Nonnull FluidStack resource, boolean doFill) {
        return fluidTank.fill(resource, doFill);
    }

    @Override
    public boolean canFill(EnumFacing from, @Nonnull FluidStack fluid) {
        //TODO: Instead of water, use the recipe's cleansing fluid
        return from == EnumFacing.UP && fluid.getFluid().equals(FluidRegistry.WATER);
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        if (from == EnumFacing.UP) {
            return new FluidTankInfo[]{fluidTank.getInfo()};
        }
        return PipeUtils.EMPTY;
    }

    @Override
    public FluidTankInfo[] getAllTanks() {
        return new FluidTankInfo[]{fluidTank.getInfo()};
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (fluidTank.getFluid() != null) {
            ItemDataUtils.setCompound(itemStack, "fluidTank", fluidTank.getFluid().writeToNBT(new NBTTagCompound()));
        }
        if (inputTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "inputTank", inputTank.getGas().write(new NBTTagCompound()));
        }
        if (outputTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "outputTank", outputTank.getGas().write(new NBTTagCompound()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(ItemDataUtils.getCompound(itemStack, "fluidTank")));
        inputTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "inputTank")));
        outputTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "outputTank")));
    }

    @Override
    public List<String> getInfo(Upgrade upgrade) {
        return upgrade == Upgrade.SPEED ? upgrade.getExpScaledInfo(this) : upgrade.getMultScaledInfo(this);
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{fluidTank, inputTank, outputTank};
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(inputTank.getStored(), inputTank.getMaxGas());
    }
}