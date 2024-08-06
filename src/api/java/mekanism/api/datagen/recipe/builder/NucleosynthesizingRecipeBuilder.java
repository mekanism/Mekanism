package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.basic.BasicNucleosynthesizingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class NucleosynthesizingRecipeBuilder extends MekanismRecipeBuilder<NucleosynthesizingRecipeBuilder> {

    private final ItemStackIngredient itemInput;
    private final ChemicalStackIngredient gasInput;
    private final ItemStack output;
    private final int duration;

    protected NucleosynthesizingRecipeBuilder(ItemStackIngredient itemInput, ChemicalStackIngredient gasInput, ItemStack output, int duration) {
        this.itemInput = itemInput;
        this.gasInput = gasInput;
        this.output = output;
        this.duration = duration;
    }

    /**
     * Creates a Nucleosynthesizing recipe builder.
     *
     * @param itemInput Item Input.
     * @param gasInput  Gas Input.
     * @param output    Output.
     * @param duration  Duration in ticks that it takes the recipe to complete. Must be greater than zero.
     */
    public static NucleosynthesizingRecipeBuilder nucleosynthesizing(ItemStackIngredient itemInput, ChemicalStackIngredient gasInput, ItemStack output, int duration) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This nucleosynthesizing recipe requires a non empty item output.");
        } else if (duration <= 0) {
            throw new IllegalArgumentException("This nucleosynthesizing recipe must have a positive duration.");
        }
        return new NucleosynthesizingRecipeBuilder(itemInput, gasInput, output, duration);
    }

    @Override
    protected NucleosynthesizingRecipe asRecipe() {
        return new BasicNucleosynthesizingRecipe(itemInput, gasInput, output, duration);
    }

    /**
     * Builds this recipe using the output item's name as the recipe name.
     *
     * @param recipeOutput Finished Recipe Consumer.
     */
    public void build(RecipeOutput recipeOutput) {
        build(recipeOutput, output.getItem());
    }
}