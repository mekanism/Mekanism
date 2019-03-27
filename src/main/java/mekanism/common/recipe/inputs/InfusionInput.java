package mekanism.common.recipe.inputs;

import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.common.InfuseStorage;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

/**
 * An infusion input, containing the type of and amount of infuse the operation requires, as well as the input
 * ItemStack.
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
        infuse = new InfuseStorage(storage.type, storage.amount);
        inputStack = itemStack;
    }

    public InfusionInput(InfuseType infusionType, int required, ItemStack itemStack) {
        infuse = new InfuseStorage(infusionType, required);
        inputStack = itemStack;
    }

    public InfusionInput() {
    }

    @Override
    public void load(NBTTagCompound nbtTags) {
        inputStack = new ItemStack(nbtTags.getCompoundTag("input"));
        InfuseType type = InfuseRegistry.get(nbtTags.getString("infuseType"));
        int amount = nbtTags.getInteger("infuseAmount");
        infuse = new InfuseStorage(type, amount);
    }

    @Override
    public InfusionInput copy() {
        return new InfusionInput(infuse.type, infuse.amount, inputStack.copy());
    }

    @Override
    public boolean isValid() {
        return infuse.type != null && !inputStack.isEmpty();
    }

    public boolean use(NonNullList<ItemStack> inventory, int index, InfuseStorage infuseStorage, boolean deplete) {
        if (inputContains(inventory.get(index), inputStack) && infuseStorage.contains(infuse)) {
            if (deplete) {
                inventory.set(index, StackUtils.subtract(inventory.get(index), inputStack));
                infuseStorage.subtract(infuse);
            }

            return true;
        }

        return false;
    }

    @Override
    public int hashIngredients() {
        return infuse.type.unlocalizedName.hashCode() << 8 | StackUtils.hashItemStack(inputStack);
    }

    @Override
    public boolean testEquality(InfusionInput other) {
        if (!isValid()) {
            return !other.isValid();
        }

        return infuse.type == other.infuse.type && StackUtils.equalsWildcardWithNBT(inputStack, other.inputStack);
    }

    @Override
    public boolean isInstance(Object other) {
        return other instanceof InfusionInput;
    }

    public InfusionInput wildCopy() {
        return new InfusionInput(infuse,
              new ItemStack(inputStack.getItem(), inputStack.getCount(), OreDictionary.WILDCARD_VALUE));
    }
}
