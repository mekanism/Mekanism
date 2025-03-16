package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.basic.BasicCombinerRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class CombinerRecipeBuilder extends MekanismRecipeBuilder<CombinerRecipeBuilder> {

    private final ItemStackIngredient mainInput;
    private final ItemStackIngredient extraInput;
    private final ItemStack output;

    protected CombinerRecipeBuilder(ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output) {
        this.mainInput = mainInput;
        this.extraInput = extraInput;
        this.output = output;
    }

    /**
     * Creates a Combining recipe builder.
     *
     * @param mainInput  Main Input.
     * @param extraInput Extra/Secondary Input.
     * @param output     Output.
     */
    public static CombinerRecipeBuilder combining(ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This combining recipe requires a non empty item output.");
        }
        return new CombinerRecipeBuilder(mainInput, extraInput, output);
    }

    @Override
    protected CombinerRecipe asRecipe() {
        return new BasicCombinerRecipe(mainInput, extraInput, output);
    }

    /**
     * Builds this recipe using the output item's name as the recipe name.
     *
     * @param recipeOutput Finished Recipe Consumer.
     */
    public void build(RecipeOutput recipeOutput) {
        build(recipeOutput, output.getItemHolder());
    }
}