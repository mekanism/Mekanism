package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public abstract class BasicItemStackToItemStackRecipe extends ItemStackToItemStackRecipe {

    protected final ItemStackIngredient input;
    protected final ItemStack output;

    /**
     * @param input  Input.
     * @param output Output.
     */
    public BasicItemStackToItemStackRecipe(ItemStackIngredient input, ItemStack output, RecipeType<ItemStackToItemStackRecipe> recipeType) {
        super(recipeType);
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public boolean test(ItemStack input) {
        return this.input.test(input);
    }

    @Override
    public ItemStackIngredient getInput() {
        return input;
    }

    @Override
    @Contract(value = "_ -> new", pure = true)
    public ItemStack getOutput(ItemStack input) {
        return output.copy();
    }

    @NotNull
    @Override
    public ItemStack getResultItem(@NotNull HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public List<ItemStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    public ItemStack getOutputRaw() {
        return output;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicItemStackToItemStackRecipe other = (BasicItemStackToItemStackRecipe) o;
        return input.equals(other.input) && ItemStack.matches(output, other.output);
    }

    @Override
    public int hashCode() {
        int hash = input.hashCode();
        hash = 31 * hash + ItemStack.hashItemAndComponents(output);
        hash = 31 * hash + output.getCount();
        return hash;
    }
}