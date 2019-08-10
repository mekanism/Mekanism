package mekanism.common.recipe.inputs;

import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.common.InfuseStorage;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

/**
 * An infusion input, containing the type of and amount of infuse the operation requires, as well as the input ItemStack.
 *
 * @author AidanBrady
 */
public class InfusionInput extends MachineInput<InfusionInput> implements IWildInput<InfusionInput> {

    public InfuseStorage infuse;

    /**
     * The input ItemStack
     */
    public ItemStack inputStack = ItemStack.EMPTY;

    public InfusionInput(InfuseStorage storage, ItemStack itemStack) {
        infuse = new InfuseStorage(storage.getType(), storage.getAmount());
        inputStack = itemStack;
    }

    public InfusionInput(InfuseType infusionType, int required, ItemStack itemStack) {
        infuse = new InfuseStorage(infusionType, required);
        inputStack = itemStack;
    }

    public InfusionInput() {
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        inputStack = ItemStack.read(nbtTags.getCompound("input"));
        InfuseType type = InfuseRegistry.get(nbtTags.getString("infuseType"));
        int amount = nbtTags.getInt("infuseAmount");
        infuse = new InfuseStorage(type, amount);
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
        return infuse.getType().unlocalizedName.hashCode() << 8 | StackUtils.hashItemStack(inputStack);
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

    @Override
    public InfusionInput wildCopy() {
        return new InfusionInput(infuse, new ItemStack(inputStack.getItem(), inputStack.getCount(), OreDictionary.WILDCARD_VALUE));
    }
}