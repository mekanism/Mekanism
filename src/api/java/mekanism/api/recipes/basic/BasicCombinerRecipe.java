package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public class BasicCombinerRecipe extends CombinerRecipe {

    protected final ItemStackIngredient mainInput;
    protected final ItemStackIngredient extraInput;
    protected final ItemStackTemplate output;

    /// @param mainInput  Main input.
    /// @param extraInput Secondary/extra input.
    /// @param output     Output.
    public BasicCombinerRecipe(ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStackTemplate output) {
        this.mainInput = Objects.requireNonNull(mainInput, "Main input cannot be null.");
        this.extraInput = Objects.requireNonNull(extraInput, "Secondary/Extra input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        this.output = output;
    }

    @Override
    public ItemStackIngredient getMainInput() {
        return mainInput;
    }

    @Override
    public ItemStackIngredient getExtraInput() {
        return extraInput;
    }

    @Override
    @Contract(pure = true)
    public ItemStackTemplate getOutput(TypedInstance<Item> input, TypedInstance<Item> extra) {
        return output;
    }

    @Override
    public List<ItemStackTemplate> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    public ItemStackTemplate getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicCombinerRecipe> getSerializer() {
        return MekanismRecipeSerializers.COMBINING.get();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicCombinerRecipe other = (BasicCombinerRecipe) o;
        return mainInput.equals(other.mainInput) && extraInput.equals(other.extraInput) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int hash = mainInput.hashCode();
        hash = 31 * hash + extraInput.hashCode();
        hash = 31 * hash + output.hashCode();
        return hash;
    }
}