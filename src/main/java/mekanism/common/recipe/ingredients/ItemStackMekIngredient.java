package mekanism.common.recipe.ingredients;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;

public class ItemStackMekIngredient implements IMekanismIngredient<ItemStack> {

    private final ItemStack stack;
    private final List<ItemStack> matching;

    public ItemStackMekIngredient(@Nonnull ItemStack stack) {
        this.stack = stack;
        matching = Collections.singletonList(this.stack);
    }

    public ItemStack getStack() {
        return stack;
    }

    @Nonnull
    @Override
    public List<ItemStack> getMatching() {
        return matching;
    }

    @Override
    public boolean contains(@Nonnull ItemStack stack) {
        return ItemStack.areItemStacksEqual(this.stack, stack);
    }

    @Override
    public int hashCode() {
        return stack.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ItemStackMekIngredient && ItemStack.areItemStacksEqual(stack, ((ItemStackMekIngredient) obj).stack);
    }
}