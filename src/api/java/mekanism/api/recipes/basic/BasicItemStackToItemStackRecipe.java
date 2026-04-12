package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.ItemStackTemplateHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public abstract class BasicItemStackToItemStackRecipe extends ItemStackToItemStackRecipe {

    protected final ItemStackIngredient input;
    protected final ItemStackTemplate output;

    /**
     * @param input  Input.
     * @param output Output.
     */
    public BasicItemStackToItemStackRecipe(ItemStackIngredient input, ItemStackTemplate output, RecipeType<ItemStackToItemStackRecipe> recipeType) {
        super(recipeType);
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        this.output = output;
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
    @Contract(pure = true)
    public ItemStackTemplate getOutput(ItemStack input) {
        return output;
    }

    @Override
    public List<ItemStack> getOutputDefinition() {
        return Collections.singletonList(output.create());
    }

    public ItemStackTemplate getOutputRaw() {
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
        return input.equals(other.input) && ItemStackTemplateHelper.matches(output, other.output);
    }

    @Override
    public int hashCode() {
        int hash = input.hashCode();
        hash = 31 * hash + ItemStackTemplateHelper.hashItemAndComponents(output);
        hash = 31 * hash + output.count();
        return hash;
    }
}