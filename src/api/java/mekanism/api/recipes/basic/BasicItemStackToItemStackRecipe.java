package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public abstract class BasicItemStackToItemStackRecipe extends ItemStackToItemStackRecipe {

    protected final ItemStackIngredient input;
    protected final ItemStackTemplate output;

    /// @param input  Input.
    /// @param output Output.
    public BasicItemStackToItemStackRecipe(ItemStackIngredient input, ItemStackTemplate output, RecipeType<ItemStackToItemStackRecipe> recipeType) {
        super(recipeType);
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
    }

    @Override
    public ItemStackIngredient getInput() {
        return input;
    }

    @Override
    @Contract(pure = true)
    public ItemStackTemplate getOutput(TypedInstance<Item> input) {
        return output;
    }

    @Override
    public List<ItemStackTemplate> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        return new SlotDisplay.ItemStackSlotDisplay(output);
    }

    public ItemStackTemplate getOutputRaw() {
        return output;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicItemStackToItemStackRecipe other = (BasicItemStackToItemStackRecipe) o;
        return input.equals(other.input) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int hash = input.hashCode();
        hash = 31 * hash + output.hashCode();
        return hash;
    }
}