package mekanism.api.datagen.recipe.builder;

import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.basic.BasicChemicalConversionRecipe;
import mekanism.api.recipes.basic.BasicChemicalOxidizerRecipe;
import mekanism.api.recipes.basic.BasicPigmentExtractingRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public class ItemStackToChemicalRecipeBuilder extends MekanismRecipeBuilder<ItemStackToChemicalRecipeBuilder> {

    private final ItemStackToChemicalRecipeBuilder.Factory factory;
    private final ItemStackIngredient input;
    private final ChemicalStackTemplate output;

    protected ItemStackToChemicalRecipeBuilder(ItemStackIngredient input, ChemicalStackTemplate output, ItemStackToChemicalRecipeBuilder.Factory factory) {
        this.input = input;
        this.output = output;
        this.factory = factory;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return getDefaultRecipeId(output);
    }

    /// Creates a Chemical Conversion recipe builder.
    ///
    /// @param input  Input.
    /// @param output Output.
    public static ItemStackToChemicalRecipeBuilder chemicalConversion(ItemStackIngredient input, ChemicalStackTemplate output) {
        return new ItemStackToChemicalRecipeBuilder(input, output, BasicChemicalConversionRecipe::new);
    }

    /// Creates an Oxidizing recipe builder.
    ///
    /// @param input  Input.
    /// @param output Output.
    public static ItemStackToChemicalRecipeBuilder oxidizing(ItemStackIngredient input, ChemicalStackTemplate output) {
        return new ItemStackToChemicalRecipeBuilder(input, output, BasicChemicalOxidizerRecipe::new);
    }

    /// Creates a Pigment Extracting recipe builder.
    ///
    /// @param input  Input.
    /// @param output Output.
    public static ItemStackToChemicalRecipeBuilder pigmentExtracting(ItemStackIngredient input, ChemicalStackTemplate output) {
        return new ItemStackToChemicalRecipeBuilder(input, output, BasicPigmentExtractingRecipe::new);
    }

    @Override
    protected ItemStackToChemicalRecipe asRecipe() {
        return factory.create(input, output);
    }

    @FunctionalInterface
    public interface Factory {

        ItemStackToChemicalRecipe create(ItemStackIngredient input, ChemicalStackTemplate output);
    }
}