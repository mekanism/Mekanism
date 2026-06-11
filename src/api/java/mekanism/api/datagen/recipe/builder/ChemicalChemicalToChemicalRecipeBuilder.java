package mekanism.api.datagen.recipe.builder;

import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicChemicalInfuserRecipe;
import mekanism.api.recipes.basic.BasicPigmentMixingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public class ChemicalChemicalToChemicalRecipeBuilder extends MekanismRecipeBuilder<ChemicalChemicalToChemicalRecipeBuilder> {

    private final ChemicalChemicalToChemicalRecipeBuilder.Factory factory;
    private final ChemicalStackIngredient leftInput;
    private final ChemicalStackIngredient rightInput;
    private final ChemicalStackTemplate output;

    protected ChemicalChemicalToChemicalRecipeBuilder(ChemicalStackIngredient leftInput, ChemicalStackIngredient rightInput, ChemicalStackTemplate output,
          ChemicalChemicalToChemicalRecipeBuilder.Factory factory) {
        this.leftInput = leftInput;
        this.rightInput = rightInput;
        this.output = output;
        this.factory = factory;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return getDefaultRecipeId(output);
    }

    /**
     * Creates a Chemical Infusing recipe builder.
     *
     * @param leftInput  Left input.
     * @param rightInput Right input.
     * @param output     Output.
     */
    public static ChemicalChemicalToChemicalRecipeBuilder chemicalInfusing(ChemicalStackIngredient leftInput, ChemicalStackIngredient rightInput,
          ChemicalStackTemplate output) {
        return new ChemicalChemicalToChemicalRecipeBuilder(leftInput, rightInput, output, BasicChemicalInfuserRecipe::new);
    }

    /**
     * Creates a Pigment Mixing recipe builder.
     *
     * @param leftInput  Left input.
     * @param rightInput Right input.
     * @param output     Output.
     */
    public static ChemicalChemicalToChemicalRecipeBuilder pigmentMixing(ChemicalStackIngredient leftInput, ChemicalStackIngredient rightInput, ChemicalStackTemplate output) {
        return new ChemicalChemicalToChemicalRecipeBuilder(leftInput, rightInput, output, BasicPigmentMixingRecipe::new);
    }

    @Override
    protected ChemicalChemicalToChemicalRecipe asRecipe() {
        return factory.create(leftInput, rightInput, output);
    }

    @FunctionalInterface
    public interface Factory {

        ChemicalChemicalToChemicalRecipe create(ChemicalStackIngredient leftInput, ChemicalStackIngredient rightInput, ChemicalStackTemplate output);
    }
}