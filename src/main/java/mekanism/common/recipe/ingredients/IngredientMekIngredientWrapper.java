package mekanism.common.recipe.ingredients;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

public class IngredientMekIngredientWrapper implements IMekanismIngredient<ItemStack> {

    private final Ingredient ingredient;

    public IngredientMekIngredientWrapper(@Nonnull Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    @Nonnull
    @Override
    public List<ItemStack> getMatching() {
        return Arrays.asList(ingredient.getMatchingStacks());
    }

    @Override
    public boolean contains(@Nonnull ItemStack stack) {
        return ingredient.apply(stack);
    }

    @Override
    public int hashCode() {
        return ingredient.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IngredientMekIngredientWrapper && ingredient.equals(((IngredientMekIngredientWrapper) obj).ingredient);
    }
}