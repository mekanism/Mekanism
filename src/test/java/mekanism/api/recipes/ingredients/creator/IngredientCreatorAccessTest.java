package mekanism.api.recipes.ingredients.creator;

import mekanism.common.recipe.ingredients.creator.FluidStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.GasStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.InfusionStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.ItemStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.PigmentStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.SlurryStackIngredientCreator;
import mekanism.common.recipe.ingredients.gas.GasIngredientCreator;
import mekanism.common.recipe.ingredients.infusion.InfusionIngredientCreator;
import mekanism.common.recipe.ingredients.pigment.PigmentIngredientCreator;
import mekanism.common.recipe.ingredients.slurry.SlurryIngredientCreator;
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
    @DisplayName("Test getting GasStackIngredient creator")
    void testGetGasStackIngredientCreator() {
        Assertions.assertInstanceOf(GasStackIngredientCreator.class, IngredientCreatorAccess.gas());
    }

    @Test
    @DisplayName("Test getting GasIngredient creator")
    void testGetGasIngredientCreator() {
        Assertions.assertInstanceOf(GasIngredientCreator.class, IngredientCreatorAccess.basicGas());
    }

    @Test
    @DisplayName("Test getting InfusionStackIngredient creator")
    void testGetInfusionStackIngredientCreator() {
        Assertions.assertInstanceOf(InfusionStackIngredientCreator.class, IngredientCreatorAccess.infusion());
    }

    @Test
    @DisplayName("Test getting InfusionIngredient creator")
    void testGetInfusionIngredientCreator() {
        Assertions.assertInstanceOf(InfusionIngredientCreator.class, IngredientCreatorAccess.basicInfusion());
    }

    @Test
    @DisplayName("Test getting PigmentStackIngredient creator")
    void testGetPigmentStackIngredientCreator() {
        Assertions.assertInstanceOf(PigmentStackIngredientCreator.class, IngredientCreatorAccess.pigment());
    }

    @Test
    @DisplayName("Test getting PigmentIngredient creator")
    void testGetPigmentIngredientCreator() {
        Assertions.assertInstanceOf(PigmentIngredientCreator.class, IngredientCreatorAccess.basicPigment());
    }

    @Test
    @DisplayName("Test getting SlurryStackIngredient creator")
    void testGetSlurryStackIngredientCreator() {
        Assertions.assertInstanceOf(SlurryStackIngredientCreator.class, IngredientCreatorAccess.slurry());
    }

    @Test
    @DisplayName("Test getting SlurryIngredient creator")
    void testGetSlurryIngredientCreator() {
        Assertions.assertInstanceOf(SlurryIngredientCreator.class, IngredientCreatorAccess.basicSlurry());
    }
}