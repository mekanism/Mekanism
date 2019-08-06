package mekanism.common.recipe.outputs;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;

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
    public void load(CompoundNBT nbtTags) {
        itemOutput = new ItemStack(nbtTags.getCompound("itemOutput"));
        gasOutput = GasStack.readFromNBT(nbtTags.getCompound("gasOutput"));
    }

    public boolean canFillTank(GasTank tank) {
        return tank.canReceive(gasOutput.getGas()) && tank.getNeeded() >= gasOutput.amount;
    }

    public boolean canAddProducts(NonNullList<ItemStack> inventory, int index) {
        ItemStack stack = inventory.get(index);
        return stack.isEmpty() || (ItemHandlerHelper.canItemStacksStack(stack, itemOutput) && stack.getCount() + itemOutput.getCount() <= stack.getMaxStackSize());
    }

    public void fillTank(GasTank tank) {
        tank.receive(gasOutput, true);
    }

    public void addProducts(NonNullList<ItemStack> inventory, int index) {
        ItemStack stack = inventory.get(index);
        if (stack.isEmpty()) {
            inventory.set(index, itemOutput.copy());
        } else if (ItemHandlerHelper.canItemStacksStack(stack, itemOutput)) {
            stack.grow(itemOutput.getCount());
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