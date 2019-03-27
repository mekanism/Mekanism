package mekanism.common.recipe.inputs;

import mekanism.common.OreDictCache;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class MachineInput<INPUT extends MachineInput<INPUT>> {

    public static boolean inputContains(ItemStack container, ItemStack contained) {
        if (!container.isEmpty() && container.getCount() >= contained.getCount()) {
            if (OreDictCache.getOreDictName(container).contains("treeSapling")) {
                return StackUtils.equalsWildcard(contained, container);
            }

            return StackUtils.equalsWildcardWithNBT(contained, container) && container.getCount() >= contained
                  .getCount();
        }

        return false;
    }

    public abstract boolean isValid();

    public abstract INPUT copy();

    public abstract int hashIngredients();

    public abstract void load(NBTTagCompound nbtTags);

    /**
     * Test equality to another input. This should return true if the input matches this one, IGNORING AMOUNTS. Allows
     * usage of HashMap optimisation to get recipes.
     *
     * @param other The other input to check
     * @return True if input matches this one, IGNORING AMOUNTS!
     */
    public abstract boolean testEquality(INPUT other);

    @Override
    public int hashCode() {
        return hashIngredients();
    }

    @Override
    public boolean equals(Object other) {
        if (isInstance(other)) {
            return testEquality((INPUT) other);
        }

        return false;
    }

    public abstract boolean isInstance(Object other);
}
