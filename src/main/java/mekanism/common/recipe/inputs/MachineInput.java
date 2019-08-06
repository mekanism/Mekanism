package mekanism.common.recipe.inputs;

import java.util.HashMap;
import java.util.Map;
import mekanism.common.OreDictCache;
import mekanism.common.util.StackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public abstract class MachineInput<INPUT extends MachineInput<INPUT>> {

    public static final ItemStackIngredientMatcher DEFAULT_MATCHER = MachineInput::inputItemMatchesDefault;
    private static final Map<Class<? extends Item>, ItemStackIngredientMatcher> ITEM_MATCHER_OVERRIDES = new HashMap<>();

    public static void addCustomItemMatcher(Class<? extends Item> clazz, ItemStackIngredientMatcher matcher) {
        ITEM_MATCHER_OVERRIDES.put(clazz, matcher);
    }

    public static boolean inputContains(ItemStack container, ItemStack contained) {
        if (!container.isEmpty() && container.getCount() >= contained.getCount()) {
            return inputItemMatches(container, contained);
        }
        return false;
    }

    public abstract boolean isValid();

    public abstract INPUT copy();

    public abstract int hashIngredients();

    public abstract void load(CompoundNBT nbtTags);

    /**
     * Test equality to another input. This should return true if the input matches this one, IGNORING AMOUNTS. Allows usage of HashMap optimisation to get recipes.
     *
     * @param other The other input to check
     *
     * @return True if input matches this one, IGNORING AMOUNTS!
     */
    public abstract boolean testEquality(INPUT other);

    /**
     * Checks if the two item stacks match (IGNORES AMOUNTS)
     */
    public static boolean inputItemMatches(ItemStack container, ItemStack contained) {
        return ITEM_MATCHER_OVERRIDES.getOrDefault(container.getItem().getClass(), DEFAULT_MATCHER).test(container, contained);
    }

    private static boolean inputItemMatchesDefault(ItemStack container, ItemStack contained) {
        if (OreDictCache.getOreDictName(container).contains("treeSapling")) {
            return StackUtils.equalsWildcard(container, contained);
        }
        return StackUtils.equalsWildcardWithNBT(container, contained);
    }

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

    @FunctionalInterface
    public interface ItemStackIngredientMatcher {

        /**
         * Test equality to another input. This should return true if the input matches this one, IGNORING AMOUNTS.
         *
         * @param definition The ingredient stored in the ItemStackInput
         * @param test       The other input to check
         *
         * @return True if input matches this one, IGNORING AMOUNTS!
         */
        boolean test(ItemStack definition, ItemStack test);
    }
}