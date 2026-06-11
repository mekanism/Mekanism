package mekanism.api.datagen.recipe.builder;

import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicActivatingRecipe;
import mekanism.api.recipes.basic.BasicCentrifugingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

/**
 * @since 10.7.0 Previously was GasToGasRecipeBuilder
 */
public class ChemicalToChemicalRecipeBuilder extends MekanismRecipeBuilder<ChemicalToChemicalRecipeBuilder> {

    private final ChemicalToChemicalRecipeBuilder.Factory factory;
    private final ChemicalStackIngredient input;
    private final ChemicalStackTemplate output;

    protected ChemicalToChemicalRecipeBuilder(ChemicalStackIngredient input, ChemicalStackTemplate output, ChemicalToChemicalRecipeBuilder.Factory factory) {
        this.input = input;
        this.output = output;
        this.factory = factory;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return getDefaultRecipeId(output);
    }

    /**
     * Creates an Activating recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ChemicalToChemicalRecipeBuilder activating(ChemicalStackIngredient input, ChemicalStackTemplate output) {
        return new ChemicalToChemicalRecipeBuilder(input, output, BasicActivatingRecipe::new);
    }

    /**
     * Creates a Centrifuging recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ChemicalToChemicalRecipeBuilder centrifuging(ChemicalStackIngredient input, ChemicalStackTemplate output) {
        return new ChemicalToChemicalRecipeBuilder(input, output, BasicCentrifugingRecipe::new);
    }

    @Override
    protected ChemicalToChemicalRecipe asRecipe() {
        return factory.create(input, output);
    }

    @FunctionalInterface
    public interface Factory {

        ChemicalToChemicalRecipe create(ChemicalStackIngredient input, ChemicalStackTemplate output);
    }
}