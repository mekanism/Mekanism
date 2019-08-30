package mekanism.common.recipe.inputs;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasTank;
import mekanism.api.providers.IGasProvider;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;

public class AdvancedMachineInput extends MachineInput<AdvancedMachineInput> {

    public ItemStack itemStack = ItemStack.EMPTY;

    public Gas gasType;

    public AdvancedMachineInput(ItemStack item, IGasProvider gasProvider) {
        itemStack = item;
        gasType = gasProvider == null ? null : gasProvider.getGas();
    }

    public AdvancedMachineInput() {
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        itemStack = ItemStack.read(nbtTags.getCompound("input"));
        gasType = Gas.readFromNBT(nbtTags.getCompound("gasType"));
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
        return StackUtils.equalsWildcard(itemStack, input.itemStack) && input.itemStack.getCount() >= itemStack.getCount();
    }

    @Override
    public int hashIngredients() {
        return StackUtils.hashItemStack(itemStack) << 8 | gasType.getRegistryName().hashCode();
    }

    @Override
    public boolean testEquality(AdvancedMachineInput other) {
        if (!isValid()) {
            return !other.isValid();
        }
        //TODO: Use tags for comparing rather than getRegistryName
        return MachineInput.inputItemMatches(itemStack, other.itemStack) && gasType.getRegistryName() == other.gasType.getRegistryName();
    }

    @Override
    public boolean isInstance(Object other) {
        return other instanceof AdvancedMachineInput;
    }
}