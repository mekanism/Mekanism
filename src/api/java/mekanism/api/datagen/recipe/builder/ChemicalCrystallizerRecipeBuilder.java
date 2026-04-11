package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.basic.BasicChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;

@NothingNullByDefault
public class ChemicalCrystallizerRecipeBuilder extends MekanismRecipeBuilder<ChemicalCrystallizerRecipeBuilder> {

    private final ChemicalStackIngredient input;
    private final ItemStackTemplate output;

    protected ChemicalCrystallizerRecipeBuilder(ChemicalStackIngredient input, ItemStackTemplate output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(output);
    }

    /**
     * Creates a Chemical Crystallizing recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ChemicalCrystallizerRecipeBuilder crystallizing(ChemicalStackIngredient input, ItemStackTemplate output) {
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
        save(recipeOutput, output.typeHolder());
    }
}