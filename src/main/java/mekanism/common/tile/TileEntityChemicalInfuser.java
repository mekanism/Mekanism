package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.common.Upgrade;
import mekanism.common.Upgrade.IUpgradeInfoHandler;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITankManager;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ChemicalPairInput;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.prefab.TileEntityMachine;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityChemicalInfuser extends TileEntityMachine implements IGasHandler, IRedstoneControl,
      ISustainedData, IUpgradeTile, IUpgradeInfoHandler, ITankManager, ISecurityTile {

    public static final int MAX_GAS = 10000;
    public GasTank leftTank = new GasTank(MAX_GAS);
    public GasTank rightTank = new GasTank(MAX_GAS);
    public GasTank centerTank = new GasTank(MAX_GAS);
    public int gasOutput = 256;

    public ChemicalInfuserRecipe cachedRecipe;

    public double clientEnergyUsed;

    public TileEntityChemicalInfuser() {
        super("machine.cheminfuser", "ChemicalInfuser", BlockStateMachine.MachineType.CHEMICAL_INFUSER.baseEnergy,
              usage.chemicalInfuserUsage, 4);

        inventory = NonNullList.withSize(5, ItemStack.EMPTY);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            ChargeUtils.discharge(3, this);
            TileUtils.receiveGas(inventory.get(0), leftTank);
            TileUtils.receiveGas(inventory.get(1), rightTank);
            TileUtils.drawGas(inventory.get(2), centerTank);

            ChemicalInfuserRecipe recipe = getRecipe();

            if (canOperate(recipe) && getEnergy() >= energyPerTick && MekanismUtils.canFunction(this)) {
                setActive(true);

                int operations = operate(recipe);
                double prev = getEnergy();

                setEnergy(getEnergy() - energyPerTick * operations);
                clientEnergyUsed = prev - getEnergy();
            } else {
                if (prevEnergy >= getEnergy()) {
                    setActive(false);
                }
            }
            TileUtils.emitGas(this, centerTank, gasOutput, facing);
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
        possibleProcess = Math.min((int) (getEnergy() / energyPerTick), possibleProcess);

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
    public void handlePacketData(ByteBuf dataStream) {
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
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        leftTank.read(nbtTags.getCompoundTag("leftTank"));
        rightTank.read(nbtTags.getCompoundTag("rightTank"));
        centerTank.read(nbtTags.getCompoundTag("centerTank"));
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setTag("leftTank", leftTank.write(new NBTTagCompound()));
        nbtTags.setTag("rightTank", rightTank.write(new NBTTagCompound()));
        nbtTags.setTag("centerTank", centerTank.write(new NBTTagCompound()));

        return nbtTags;
    }

    @Override
    public boolean canSetFacing(int i) {
        return i != 0 && i != 1;
    }

    public GasTank getTank(EnumFacing side) {
        if (side == MekanismUtils.getLeft(facing)) {
            return leftTank;
        } else if (side == MekanismUtils.getRight(facing)) {
            return rightTank;
        } else if (side == facing) {
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
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return (getTank(side) != null && getTank(side) != centerTank) && getTank(side).canReceive(type);
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
        return (getTank(side) != null && getTank(side) == centerTank) && getTank(side).canDraw(type);
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
            return (T) this;
        }

        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return side != MekanismUtils.getLeft(facing) && side != MekanismUtils.getRight(facing) && side != facing;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        return slotID == 3 && ChargeUtils.canBeDischarged(itemstack);
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        if (slotID == 0 || slotID == 2) {
            return !itemstack.isEmpty() && itemstack.getItem() instanceof IGasItem && ((IGasItem) itemstack.getItem())
                  .canReceiveGas(itemstack, null);
        } else if (slotID == 1) {
            return !itemstack.isEmpty() && itemstack.getItem() instanceof IGasItem && ((IGasItem) itemstack.getItem())
                  .canProvideGas(itemstack, null);
        } else if (slotID == 3) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        }

        return false;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        if (side == MekanismUtils.getLeft(facing)) {
            return new int[]{0};
        } else if (side == facing) {
            return new int[]{1};
        } else if (side == MekanismUtils.getRight(facing)) {
            return new int[]{2};
        } else if (side.getAxis() == Axis.Y) {
            return new int[3];
        }

        return InventoryUtils.EMPTY;
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (leftTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "leftTank", leftTank.getGas().write(new NBTTagCompound()));
        }

        if (rightTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "rightTank", rightTank.getGas().write(new NBTTagCompound()));
        }

        if (centerTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "centerTank", centerTank.getGas().write(new NBTTagCompound()));
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
