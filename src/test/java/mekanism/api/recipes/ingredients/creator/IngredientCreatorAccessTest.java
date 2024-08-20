package mekanism.api.recipes.ingredients.creator;

import mekanism.common.recipe.ingredients.ChemicalIngredientCreator;
import mekanism.common.recipe.ingredients.creator.ChemicalStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.FluidStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.ItemStackIngredientCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test accessing ingredient creators")
class IngredientCreatorAccessTest {

    @Test
    @DisplayName("Test getting ItemStackIngredient creator")
    void testGetItemStackIngredientCreator() {
        Assertions.assertInstanceOf(ItemStackIngredientCreator.class, IngredientCreatorAccess.item());
    }

    @Test
    @DisplayName("Test getting FluidStackIngredient creator")
    void testGetFluidStackIngredientCreator() {
        Assertions.assertInstanceOf(FluidStackIngredientCreator.class, IngredientCreatorAccess.fluid());
    }

    @Test
    @DisplayName("Test getting ChemicalStackIngredient creator")
    void testGetChemicalStackIngredientCreator() {
        Assertions.assertInstanceOf(ChemicalStackIngredientCreator.class, IngredientCreatorAccess.chemicalStack());
    }

    @Test
    @DisplayName("Test getting ChemicalIngredient creator")
    void testGetChemicalIngredientCreator() {
        Assertions.assertInstanceOf(ChemicalIngredientCreator.class, IngredientCreatorAccess.chemical());
    }
}