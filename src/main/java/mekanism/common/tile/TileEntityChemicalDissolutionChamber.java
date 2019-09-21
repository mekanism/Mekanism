package mekanism.common.tile;

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
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.MekanismBlock;
import mekanism.common.Upgrade;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.ITankManager;
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
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityChemicalDissolutionChamber extends TileEntityOperationalMachine<ItemStackGasToGasRecipe> implements IGasHandler, ISustainedData, ITankManager,
      IComparatorSupport {

    public static final int MAX_GAS = 10000;
    public static final int BASE_INJECT_USAGE = 1;
    public static final int BASE_TICKS_REQUIRED = 100;
    public GasTank injectTank = new GasTank(MAX_GAS);
    public GasTank outputTank = new GasTank(MAX_GAS);
    public double injectUsage = BASE_INJECT_USAGE;
    public int injectUsageThisTick;
    public int gasOutput = 256;

    public TileEntityChemicalDissolutionChamber() {
        super(MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER, 4, BASE_TICKS_REQUIRED);
        upgradeComponent.setSupported(Upgrade.GAS);
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            ChargeUtils.discharge(3, this);
            ItemStack itemStack = getInventory().get(0);
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
            TileUtils.drawGas(getInventory().get(2), outputTank);
            injectUsageThisTick = Math.max(BASE_INJECT_USAGE, StatUtils.inversePoisson(injectUsage));
            cachedRecipe = getUpdatedCache(0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
            TileUtils.emitGas(this, outputTank, gasOutput, getRightSide());
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
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        if (slotID == 2) {
            return !itemstack.isEmpty() && itemstack.getItem() instanceof IGasItem && ((IGasItem) itemstack.getItem()).canProvideGas(itemstack, null);
        }
        return false;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        if (side == getLeftSide() || side == Direction.UP) {
            return new int[]{1};
        } else if (side == Direction.DOWN) {
            return new int[]{0};
        } else if (side == getRightSide()) {
            return new int[]{2};
        }
        return InventoryUtils.EMPTY;
    }

    @Override
    @Nonnull
    public Recipe<ItemStackGasToGasRecipe> getRecipes() {
        return Recipe.CHEMICAL_DISSOLUTION_CHAMBER;
    }

    @Nullable
    @Override
    public CachedRecipe<ItemStackGasToGasRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public ItemStackGasToGasRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inventory.get(0);
        if (stack.isEmpty()) {
            return null;
        }
        GasStack gasStack = injectTank.getGas();
        if (gasStack.isEmpty()) {
            return null;
        }
        return getRecipes().findFirst(recipe -> recipe.test(stack, gasStack));
    }

    @Nullable
    @Override
    public CachedRecipe<ItemStackGasToGasRecipe> createNewCachedRecipe(@Nonnull ItemStackGasToGasRecipe recipe, int cacheIndex) {
        return new ItemStackGasToGasCachedRecipe(recipe, InputHelper.getInputHandler(inventory, 0), InputHelper.getInputHandler(injectTank), () -> injectUsageThisTick,
              OutputHelper.getOutputHandler(outputTank))
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(this::getEnergyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::markDirty);
    }

    public void minorOperate() {
        injectTank.draw(injectUsageThisTick, true);
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (world.isRemote) {
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
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        injectTank.read(nbtTags.getCompound("injectTank"));
        outputTank.read(nbtTags.getCompound("gasTank"));
        GasUtils.clearIfInvalid(injectTank, this::isValidGas);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.put("injectTank", injectTank.write(new CompoundNBT()));
        nbtTags.put("gasTank", outputTank.write(new CompoundNBT()));
        return nbtTags;
    }

    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, boolean doTransfer) {
        if (canReceiveGas(side, stack.getGas())) {
            return injectTank.receive(stack, doTransfer);
        }
        return 0;
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, boolean doTransfer) {
        return GasStack.EMPTY;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        return side == getLeftSide() && injectTank.canReceive(type) && isValidGas(type);
    }

    private boolean isValidGas(Gas gas) {
        return getRecipes().contains(recipe -> recipe.getGasInput().testType(gas));
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        return false;
    }

    @Override
    @Nonnull
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{injectTank, outputTank};
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
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return side != null && side != getLeftSide() && side != getRightSide();
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return side == getDirection() || side == getOppositeDirection();
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
            ItemDataUtils.setCompound(itemStack, "injectTank", injectTank.getGas().write(new CompoundNBT()));
        }
        if (outputTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "outputTank", outputTank.getGas().write(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        injectTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "injectTank")));
        outputTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "outputTank")));
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
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
}