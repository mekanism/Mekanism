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
import mekanism.common.MekanismBlock;
import mekanism.common.MekanismFluids;
import mekanism.common.Upgrade;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.DissolutionRecipe;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.prefab.TileEntityMachine;
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

public class TileEntityChemicalDissolutionChamber extends TileEntityMachine implements IGasHandler, ISustainedData, ITankManager, IComparatorSupport {

    public static final int MAX_GAS = 10000;
    public static final int BASE_INJECT_USAGE = 1;
    public static final int BASE_TICKS_REQUIRED = 100;
    public GasTank injectTank = new GasTank(MAX_GAS);
    public GasTank outputTank = new GasTank(MAX_GAS);
    public double injectUsage = BASE_INJECT_USAGE;
    public int injectUsageThisTick;
    public int gasOutput = 256;
    public int operatingTicks = 0;
    public int ticksRequired = BASE_TICKS_REQUIRED;
    public DissolutionRecipe cachedRecipe;

    public TileEntityChemicalDissolutionChamber() {
        super(MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER, 4);
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
            boolean changed = false;
            DissolutionRecipe recipe = getRecipe();
            injectUsageThisTick = Math.max(BASE_INJECT_USAGE, StatUtils.inversePoisson(injectUsage));
            if (canOperate(recipe) && getEnergy() >= getEnergyPerTick() && injectTank.getStored() >= injectUsageThisTick && MekanismUtils.canFunction(this)) {
                setActive(true);
                setEnergy(getEnergy() - getEnergyPerTick());
                minorOperate();
                if ((operatingTicks + 1) < ticksRequired) {
                    operatingTicks++;
                } else {
                    operate(recipe);
                    operatingTicks = 0;
                }
            } else if (prevEnergy >= getEnergy()) {
                changed = true;
                setActive(false);
            }
            if (changed && !canOperate(recipe)) {
                operatingTicks = 0;
            }
            prevEnergy = getEnergy();
            TileUtils.emitGas(this, outputTank, gasOutput, getRightSide());
        }
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 1) {
            return RecipeHandler.getDissolutionRecipe(new ItemStackInput(itemstack)) != null;
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

    public double getScaledProgress() {
        return (double) operatingTicks / (double) ticksRequired;
    }

    public DissolutionRecipe getRecipe() {
        ItemStackInput input = getInput();
        if (cachedRecipe == null || !input.testEquality(cachedRecipe.getInput())) {
            cachedRecipe = RecipeHandler.getDissolutionRecipe(getInput());
        }
        return cachedRecipe;
    }

    public ItemStackInput getInput() {
        return new ItemStackInput(getInventory().get(1));
    }

    public boolean canOperate(DissolutionRecipe recipe) {
        return recipe != null && recipe.canOperate(getInventory(), outputTank);
    }

    public void operate(DissolutionRecipe recipe) {
        recipe.operate(getInventory(), outputTank);
        markDirty();
    }

    public void minorOperate() {
        injectTank.draw(injectUsageThisTick, true);
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (world.isRemote) {
            operatingTicks = dataStream.readInt();
            TileUtils.readTankData(dataStream, injectTank);
            TileUtils.readTankData(dataStream, outputTank);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(operatingTicks);
        TileUtils.addTankData(data, injectTank);
        TileUtils.addTankData(data, outputTank);
        return data;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        operatingTicks = nbtTags.getInt("operatingTicks");
        injectTank.read(nbtTags.getCompound("injectTank"));
        outputTank.read(nbtTags.getCompound("gasTank"));
        GasUtils.clearIfInvalid(injectTank, this::isValidGas);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("operatingTicks", operatingTicks);
        nbtTags.put("injectTank", injectTank.write(new CompoundNBT()));
        nbtTags.put("gasTank", outputTank.write(new CompoundNBT()));
        return nbtTags;
    }

    @Override
    public int receiveGas(Direction side, GasStack stack, boolean doTransfer) {
        if (canReceiveGas(side, stack.getGas())) {
            return injectTank.receive(stack, doTransfer);
        }
        return 0;
    }

    @Override
    public GasStack drawGas(Direction side, int amount, boolean doTransfer) {
        return null;
    }

    @Override
    public boolean canReceiveGas(Direction side, Gas type) {
        return side == getLeftSide() && injectTank.canReceive(type) && isValidGas(type);
    }

    private boolean isValidGas(Gas gas) {
        //TODO: Replace with commented version once this becomes an AdvancedMachine
        return gas == MekanismFluids.SULFURIC_ACID;//Recipe.CHEMICAL_DISSOLUTION_CHAMBER.containsRecipe(gas);
    }

    @Override
    public boolean canDrawGas(Direction side, Gas type) {
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
            case ENERGY:
                setEnergyPerTick(MekanismUtils.getEnergyPerTick(this, getBaseUsage())); // incorporate speed upgrades
                break;
            case GAS:
                injectUsage = MekanismUtils.getSecondaryEnergyPerTickMean(this, BASE_INJECT_USAGE);
                break;
            case SPEED:
                ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
                setEnergyPerTick(MekanismUtils.getEnergyPerTick(this, getBaseUsage()));
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