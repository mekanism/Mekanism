package mekanism.common.recipe.outputs;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

public class PressurizedOutput extends MachineOutput<PressurizedOutput> {

    private ItemStack itemOutput = ItemStack.EMPTY;
    private GasStack gasOutput;

    public PressurizedOutput(ItemStack item, GasStack gas) {
        itemOutput = item;
        gasOutput = gas;
    }

    public PressurizedOutput() {
    }

    @Override
    public void load(NBTTagCompound nbtTags) {
        itemOutput = new ItemStack(nbtTags.getCompoundTag("itemOutput"));
        gasOutput = GasStack.readFromNBT(nbtTags.getCompoundTag("gasOutput"));
    }

    public boolean canFillTank(GasTank tank) {
        return tank.canReceive(gasOutput.getGas()) && tank.getNeeded() >= gasOutput.amount;
    }

    public boolean canAddProducts(NonNullList<ItemStack> inventory, int index) {
        return inventory.get(index).isEmpty() || (inventory.get(index).isItemEqual(itemOutput)
              && inventory.get(index).getCount() + itemOutput.getCount() <= inventory.get(index).getMaxStackSize());
    }

    public void fillTank(GasTank tank) {
        tank.receive(gasOutput, true);
    }

    public void addProducts(NonNullList<ItemStack> inventory, int index) {
        if (inventory.get(index).isEmpty()) {
            inventory.set(index, itemOutput.copy());
        } else if (inventory.get(index).isItemEqual(itemOutput)) {
            inventory.get(index).grow(itemOutput.getCount());
        }
    }

    public boolean applyOutputs(NonNullList<ItemStack> inventory, int index, GasTank tank, boolean doEmit) {
        if (canFillTank(tank) && canAddProducts(inventory, index)) {
            if (doEmit) {
                fillTank(tank);
                addProducts(inventory, index);
            }

            return true;
        }

        return false;
    }

    public ItemStack getItemOutput() {
        return itemOutput;
    }

    public GasStack getGasOutput() {
        return gasOutput;
    }

    @Override
    public PressurizedOutput copy() {
        return new PressurizedOutput(itemOutput.copy(), gasOutput.copy());
    }
}
