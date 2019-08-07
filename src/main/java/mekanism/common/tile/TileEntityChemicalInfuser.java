package mekanism.common.tile;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.common.MekanismBlock;
import mekanism.common.Upgrade;
import mekanism.common.Upgrade.IUpgradeInfoHandler;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ChemicalPairInput;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mekanism.common.tile.prefab.TileEntityMachine;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityChemicalInfuser extends TileEntityMachine implements IGasHandler, ISustainedData, IUpgradeInfoHandler, ITankManager {

    public static final int MAX_GAS = 10000;
    public GasTank leftTank = new GasTank(MAX_GAS);
    public GasTank rightTank = new GasTank(MAX_GAS);
    public GasTank centerTank = new GasTank(MAX_GAS);
    public int gasOutput = 256;

    public ChemicalInfuserRecipe cachedRecipe;

    public double clientEnergyUsed;

    public TileEntityChemicalInfuser() {
        super(MekanismBlock.CHEMICAL_INFUSER, 4);
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            ChargeUtils.discharge(3, this);
            TileUtils.receiveGas(getInventory().get(0), leftTank);
            TileUtils.receiveGas(getInventory().get(1), rightTank);
            TileUtils.drawGas(getInventory().get(2), centerTank);
            ChemicalInfuserRecipe recipe = getRecipe();
            if (canOperate(recipe) && getEnergy() >= getEnergyPerTick() && MekanismUtils.canFunction(this)) {
                setActive(true);
                int operations = operate(recipe);
                double prev = getEnergy();
                setEnergy(getEnergy() - getEnergyPerTick() * operations);
                clientEnergyUsed = prev - getEnergy();
            } else {
                if (prevEnergy >= getEnergy()) {
                    setActive(false);
                }
            }
            TileUtils.emitGas(this, centerTank, gasOutput, getDirection());
            prevEnergy = getEnergy();
        }
    }

    public int getUpgradedUsage(ChemicalInfuserRecipe recipe) {
        int possibleProcess = (int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED));
        if (leftTank.getGasType() == recipe.recipeInput.leftGas.getGas()) {
            possibleProcess = Math.min(leftTank.getStored() / recipe.recipeInput.leftGas.amount, possibleProcess);
            possibleProcess = Math.min(rightTank.getStored() / recipe.recipeInput.rightGas.amount, possibleProcess);
        } else {
            possibleProcess = Math.min(leftTank.getStored() / recipe.recipeInput.rightGas.amount, possibleProcess);
            possibleProcess = Math.min(rightTank.getStored() / recipe.recipeInput.leftGas.amount, possibleProcess);
        }
        possibleProcess = Math.min(centerTank.getNeeded() / recipe.recipeOutput.output.amount, possibleProcess);
        possibleProcess = Math.min((int) (getEnergy() / getEnergyPerTick()), possibleProcess);
        return possibleProcess;
    }

    public ChemicalPairInput getInput() {
        return new ChemicalPairInput(leftTank.getGas(), rightTank.getGas());
    }

    public ChemicalInfuserRecipe getRecipe() {
        ChemicalPairInput input = getInput();
        if (cachedRecipe == null || !input.testEquality(cachedRecipe.getInput())) {
            cachedRecipe = RecipeHandler.getChemicalInfuserRecipe(getInput());
        }
        return cachedRecipe;
    }

    public boolean canOperate(ChemicalInfuserRecipe recipe) {
        return recipe != null && recipe.canOperate(leftTank, rightTank, centerTank);
    }

    public int operate(ChemicalInfuserRecipe recipe) {
        int operations = getUpgradedUsage(recipe);
        recipe.operate(leftTank, rightTank, centerTank, operations);
        markDirty();
        return operations;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            clientEnergyUsed = dataStream.readDouble();
            TileUtils.readTankData(dataStream, leftTank);
            TileUtils.readTankData(dataStream, rightTank);
            TileUtils.readTankData(dataStream, centerTank);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(clientEnergyUsed);
        TileUtils.addTankData(data, leftTank);
        TileUtils.addTankData(data, rightTank);
        TileUtils.addTankData(data, centerTank);
        return data;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        leftTank.read(nbtTags.getCompound("leftTank"));
        rightTank.read(nbtTags.getCompound("rightTank"));
        centerTank.read(nbtTags.getCompound("centerTank"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.put("leftTank", leftTank.write(new CompoundNBT()));
        nbtTags.put("rightTank", rightTank.write(new CompoundNBT()));
        nbtTags.put("centerTank", centerTank.write(new CompoundNBT()));
        return nbtTags;
    }

    @Override
    public boolean canSetFacing(@Nonnull Direction facing) {
        return facing != Direction.DOWN && facing != Direction.UP;
    }

    public GasTank getTank(Direction side) {
        if (side == getLeftSide()) {
            return leftTank;
        } else if (side == getRightSide()) {
            return rightTank;
        } else if (side == getDirection()) {
            return centerTank;
        }
        return null;
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{leftTank, centerTank, rightTank};
    }

    @Override
    public boolean canReceiveGas(Direction side, Gas type) {
        return getTank(side) != null && getTank(side) != centerTank && getTank(side).canReceive(type);
    }

    @Override
    public int receiveGas(Direction side, GasStack stack, boolean doTransfer) {
        if (canReceiveGas(side, stack != null ? stack.getGas() : null)) {
            return getTank(side).receive(stack, doTransfer);
        }
        return 0;
    }

    @Override
    public GasStack drawGas(Direction side, int amount, boolean doTransfer) {
        if (canDrawGas(side, null)) {
            return getTank(side).draw(amount, doTransfer);
        }
        return null;
    }

    @Override
    public boolean canDrawGas(Direction side, Gas type) {
        return getTank(side) != null && getTank(side) == centerTank && getTank(side).canDraw(type);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return false;
        }
        return capability == Capabilities.GAS_HANDLER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return null;
        } else if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return side == Direction.UP || side == Direction.DOWN || side == getOppositeDirection();
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        return slotID == 3 && ChargeUtils.canBeDischarged(itemstack);
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        if (slotID == 0 || slotID == 2) {
            return !itemstack.isEmpty() && itemstack.getItem() instanceof IGasItem && ((IGasItem) itemstack.getItem()).canReceiveGas(itemstack, null);
        } else if (slotID == 1) {
            return !itemstack.isEmpty() && itemstack.getItem() instanceof IGasItem && ((IGasItem) itemstack.getItem()).canProvideGas(itemstack, null);
        } else if (slotID == 3) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        }
        return false;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        if (side == getLeftSide()) {
            return new int[]{0};
        } else if (side == getDirection()) {
            return new int[]{1};
        } else if (side == getRightSide()) {
            return new int[]{2};
        } else if (side.getAxis() == Axis.Y) {
            return new int[3];
        }
        return InventoryUtils.EMPTY;
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (leftTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "leftTank", leftTank.getGas().write(new CompoundNBT()));
        }
        if (rightTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "rightTank", rightTank.getGas().write(new CompoundNBT()));
        }
        if (centerTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "centerTank", centerTank.getGas().write(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        leftTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "leftTank")));
        rightTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "rightTank")));
        centerTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "centerTank")));
    }

    @Override
    public List<String> getInfo(Upgrade upgrade) {
        return upgrade == Upgrade.SPEED ? upgrade.getExpScaledInfo(this) : upgrade.getMultScaledInfo(this);
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{leftTank, rightTank, centerTank};
    }
}