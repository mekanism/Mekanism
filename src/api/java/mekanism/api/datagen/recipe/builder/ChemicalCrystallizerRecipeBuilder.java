package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.basic.BasicChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ChemicalCrystallizerRecipeBuilder extends MekanismRecipeBuilder<ChemicalCrystallizerRecipeBuilder> {

    private final ChemicalStackIngredient input;
    private final ItemStack output;

    protected ChemicalCrystallizerRecipeBuilder(ChemicalStackIngredient input, ItemStack output) {
        this.input = input;
        this.output = output;
    }

    /**
     * Creates a Chemical Crystallizing recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ChemicalCrystallizerRecipeBuilder crystallizing(ChemicalStackIngredient input, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This crystallizing recipe requires a non empty item output.");
        }
        return new ChemicalCrystallizerRecipeBuilder(input, output);
    }

    @Override
    protected ChemicalCrystallizerRecipe asRecipe() {
        return new BasicChemicalCrystallizerRecipe(input, output);
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