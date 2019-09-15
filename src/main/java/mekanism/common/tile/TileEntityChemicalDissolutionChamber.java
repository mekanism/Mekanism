package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ItemStackGasToGasCachedRecipe;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.Upgrade;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITankManager;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.prefab.TileEntityOperationalMachine;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StatUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;

//TODO: Make an ItemStackGasToGasRecipe, so that this properly supports using other things in the inject tank
public class TileEntityChemicalDissolutionChamber extends TileEntityOperationalMachine<ItemStackGasToGasRecipe> implements IGasHandler, ISustainedData, ITankManager {

    public static final int MAX_GAS = 10000;
    public static final int BASE_INJECT_USAGE = 1;
    public static final int BASE_TICKS_REQUIRED = 100;
    public GasTank injectTank = new GasTank(MAX_GAS);
    public GasTank outputTank = new GasTank(MAX_GAS);
    public double injectUsage = BASE_INJECT_USAGE;
    public int injectUsageThisTick;
    public int gasOutput = 256;

    public TileEntityChemicalDissolutionChamber() {
        super("machine.dissolution", MachineType.CHEMICAL_DISSOLUTION_CHAMBER, 4, BASE_TICKS_REQUIRED);
        inventory = NonNullList.withSize(5, ItemStack.EMPTY);
        upgradeComponent.setSupported(Upgrade.GAS);
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            ChargeUtils.discharge(3, this);
            ItemStack itemStack = inventory.get(0);
            if (!itemStack.isEmpty() && injectTank.getNeeded() > 0 && itemStack.getItem() instanceof IGasItem) {
                //TODO: Maybe make this use GasUtils.getItemGas. This only currently accepts IGasItems here though
                IGasItem item = (IGasItem) itemStack.getItem();
                GasStack gasStack = item.getGas(itemStack);
                //Check to make sure it can provide the gas it contains
                if (gasStack != null && item.canProvideGas(itemStack, gasStack.getGas())) {
                    Gas gas = gasStack.getGas();
                    if (gas != null && injectTank.canReceive(gas) && isValidGas(gas)) {
                        injectTank.receive(GasUtils.removeGas(itemStack, gas, injectTank.getNeeded()), true);
                    }
                }
            }
            TileUtils.drawGas(inventory.get(2), outputTank);
            injectUsageThisTick = Math.max(BASE_INJECT_USAGE, StatUtils.inversePoisson(injectUsage));
            cachedRecipe = getUpdatedCache(cachedRecipe, 0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
            TileUtils.emitGas(this, outputTank, gasOutput, MekanismUtils.getRight(facing));
        }
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 1) {
            return getRecipes().contains(recipe -> recipe.getItemInput().testType(itemstack));
        } else if (slotID == 3) {
            return ChargeUtils.canBeDischarged(itemstack);
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        if (slotID == 2) {
            return !itemstack.isEmpty() && itemstack.getItem() instanceof IGasItem && ((IGasItem) itemstack.getItem()).canProvideGas(itemstack, null);
        }
        return false;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        if (side == MekanismUtils.getLeft(facing) || side == EnumFacing.UP) {
            return new int[]{1};
        } else if (side == EnumFacing.DOWN) {
            return new int[]{0};
        } else if (side == MekanismUtils.getRight(facing)) {
            return new int[]{2};
        }
        return InventoryUtils.EMPTY;
    }

    @Nullable
    @Override
    public ItemStackGasToGasRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inventory.get(0);
        if (stack.isEmpty()) {
            return null;
        }
        GasStack gasStack = injectTank.getGas();
        if (gasStack == null || gasStack.amount == 0) {
            return null;
        }
        return getRecipes().findFirst(recipe -> recipe.test(stack, gasStack));
    }

    @Nullable
    @Override
    public CachedRecipe<ItemStackGasToGasRecipe> createNewCachedRecipe(@Nonnull ItemStackGasToGasRecipe recipe, int cacheIndex) {
        return new ItemStackGasToGasCachedRecipe(recipe, () -> inventory.get(0), () -> injectTank, () -> injectUsageThisTick, OutputHelper.getAddToOutput(outputTank))
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(() -> energyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::markDirty);
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            TileUtils.readTankData(dataStream, injectTank);
            TileUtils.readTankData(dataStream, outputTank);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        TileUtils.addTankData(data, injectTank);
        TileUtils.addTankData(data, outputTank);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);
        injectTank.read(nbtTags.getCompoundTag("injectTank"));
        outputTank.read(nbtTags.getCompoundTag("gasTank"));
        GasUtils.clearIfInvalid(injectTank, this::isValidGas);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setTag("injectTank", injectTank.write(new NBTTagCompound()));
        nbtTags.setTag("gasTank", outputTank.write(new NBTTagCompound()));
        return nbtTags;
    }

    @Override
    public boolean canSetFacing(@Nonnull EnumFacing facing) {
        return facing != EnumFacing.DOWN && facing != EnumFacing.UP;
    }

    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        if (canReceiveGas(side, stack.getGas())) {
            return injectTank.receive(stack, doTransfer);
        }
        return 0;
    }

    @Override
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        return null;
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return side == MekanismUtils.getLeft(facing) && injectTank.canReceive(type) && isValidGas(type);
    }

    private boolean isValidGas(Gas gas) {
        return getRecipes().contains(recipe -> recipe.getGasInput().testType(gas));
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
        return false;
    }

    @Override
    @Nonnull
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{injectTank, outputTank};
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return false;
        }
        return capability == Capabilities.GAS_HANDLER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return null;
        } else if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return side != null && side != MekanismUtils.getLeft(facing) && side != MekanismUtils.getRight(facing);
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return side == facing || side == facing.getOpposite();
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (injectTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "injectTank", injectTank.getGas().write(new NBTTagCompound()));
        }
        if (outputTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "outputTank", outputTank.getGas().write(new NBTTagCompound()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        injectTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "injectTank")));
        outputTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "outputTank")));
    }

    @Override
    public void recalculateUpgradables(Upgrade upgrade) {
        super.recalculateUpgradables(upgrade);
        switch (upgrade) {
            case GAS:
                injectUsage = MekanismUtils.getSecondaryEnergyPerTickMean(this, BASE_INJECT_USAGE);
                break;
            case SPEED:
                injectUsage = MekanismUtils.getSecondaryEnergyPerTickMean(this, BASE_INJECT_USAGE);
                break;
            default:
                break;
        }
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{injectTank, outputTank};
    }

    @Override
    public int getRedstoneLevel() {
        return Container.calcRedstoneFromInventory(this);
    }

    @Nonnull
    @Override
    public Recipe<ItemStackGasToGasRecipe> getRecipes() {
        return Recipe.CHEMICAL_DISSOLUTION_CHAMBER;
    }
}