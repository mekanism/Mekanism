package mekanism.common.recipe.inputs;

import mekanism.api.providers.IInfuseTypeProvider;
import mekanism.common.InfuseStorage;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;

/**
 * An infusion input, containing the type of and amount of infuse the operation requires, as well as the input ItemStack.
 *
 * @author AidanBrady
 */
public class InfusionInput extends MachineInput<InfusionInput> {

    public InfuseStorage infuse;

    /**
     * The input ItemStack
     */
    public ItemStack inputStack = ItemStack.EMPTY;

    public InfusionInput(InfuseStorage storage, ItemStack itemStack) {
        infuse = new InfuseStorage(storage.getType(), storage.getAmount());
        inputStack = itemStack;
    }

    public InfusionInput(IInfuseTypeProvider infuseTypeProvider, int required, ItemStack itemStack) {
        //TODO: Check to make sure infuseTypeProvider is not null. Just adding this for now because when passing IGasProvider it had issues
        infuse = new InfuseStorage(infuseTypeProvider == null ? null : infuseTypeProvider.getInfuseType(), required);
        inputStack = itemStack;
    }

    public InfusionInput() {
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        inputStack = ItemStack.read(nbtTags.getCompound("input"));
        infuse = InfuseStorage.readFromNBT(nbtTags.getCompound("infuseStored"));
    }

    @Override
    public InfusionInput copy() {
        return new InfusionInput(infuse.getType(), infuse.getAmount(), inputStack.copy());
    }

    @Override
    public boolean isValid() {
        return infuse.getType() != null && !inputStack.isEmpty();
    }

    public boolean use(NonNullList<ItemStack> inventory, int index, InfuseStorage infuseStorage, boolean deplete) {
        ItemStack stack = inventory.get(index);
        if (inputContains(stack, inputStack) && infuseStorage.contains(infuse)) {
            if (deplete) {
                inventory.set(index, StackUtils.subtract(stack, inputStack));
                infuseStorage.subtract(infuse);
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashIngredients() {
        return infuse.getType().getRegistryName().hashCode() << 8 | StackUtils.hashItemStack(inputStack);
    }

    @Override
    public boolean testEquality(InfusionInput other) {
        if (!isValid()) {
            return !other.isValid();
        }
        return infuse.getType() == other.infuse.getType() && MachineInput.inputItemMatches(inputStack, other.inputStack);
    }

    @Override
    public boolean isInstance(Object other) {
        return other instanceof InfusionInput;
    }
}