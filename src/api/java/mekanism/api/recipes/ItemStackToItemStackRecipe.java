package mekanism.api.recipes;

import java.util.Objects;
import mekanism.api.recipes.SingleInputRecipe.ItemInputRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;

/// Input: ItemStack
///
/// Output: ItemStack
///
/// @apiNote There are currently three types of ItemStack to ItemStack recipe types:
/// - Crushing: Can be processed in Crushers and Crushing Factories.
/// - Enriching: Can be processed in Enrichment Chambers and Enriching Factories.
/// - Smelting: Can be processed in Energized Smelters, Smelting Factories, and Robits.
public abstract class ItemStackToItemStackRecipe extends ItemInputRecipe<ItemStackTemplate> {

    protected final RecipeType<ItemStackToItemStackRecipe> recipeType;

    public ItemStackToItemStackRecipe(RecipeType<ItemStackToItemStackRecipe> recipeType) {
        this.recipeType = Objects.requireNonNull(recipeType, "Recipe type cannot be null");
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        if (!isIncomplete() && test(input.item())) {
            return getOutput(input.item()).create();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public final RecipeType<ItemStackToItemStackRecipe> getType() {
        return this.recipeType;
    }
}
