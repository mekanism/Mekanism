package mekanism.common.recipe.inputs;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasTank;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

public class AdvancedMachineInput extends MachineInput<AdvancedMachineInput> {

    public ItemStack itemStack = ItemStack.EMPTY;

    public Gas gasType;

    public AdvancedMachineInput(ItemStack item, Gas gas) {
        itemStack = item;
        gasType = gas;
    }

    public AdvancedMachineInput() {
    }

    @Override
    public void load(NBTTagCompound nbtTags) {
        itemStack = new ItemStack(nbtTags.getCompoundTag("input"));
        gasType = Gas.readFromNBT(nbtTags.getCompoundTag("gasType"));
    }

    @Override
    public AdvancedMachineInput copy() {
        return new AdvancedMachineInput(itemStack.copy(), gasType);
    }

    @Override
    public boolean isValid() {
        return !itemStack.isEmpty() && gasType != null;
    }

    public boolean useItem(NonNullList<ItemStack> inventory, int index, boolean deplete) {
        if (inputContains(inventory.get(index), itemStack)) {
            if (deplete) {
                inventory.set(index, StackUtils.subtract(inventory.get(index), itemStack));
            }

            return true;
        }

        return false;
    }

    public boolean useSecondary(GasTank gasTank, int amountToUse, boolean deplete) {
        if (gasTank.getGasType() == gasType && gasTank.getStored() >= amountToUse) {
            gasTank.draw(amountToUse, deplete);
            return true;
        }

        return false;
    }

    public boolean matches(AdvancedMachineInput input) {
        return StackUtils.equalsWildcard(itemStack, input.itemStack) && input.itemStack.getCount() >= itemStack
              .getCount();
    }

    @Override
    public int hashIngredients() {
        return StackUtils.hashItemStack(itemStack) << 8 | gasType.getID();
    }

    @Override
    public boolean testEquality(AdvancedMachineInput other) {
        if (!isValid()) {
            return !other.isValid();
        }

        return StackUtils.equalsWildcardWithNBT(itemStack, other.itemStack) && gasType.getID() == other.gasType.getID();
    }

    @Override
    public boolean isInstance(Object other) {
        return other instanceof AdvancedMachineInput;
    }

    public AdvancedMachineInput wildCopy() {
        return new AdvancedMachineInput(
              new ItemStack(itemStack.getItem(), itemStack.getCount(), OreDictionary.WILDCARD_VALUE), gasType);
    }
}
