package mekanism.api.recipes.ingredients.creator;

import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.GasStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.InfusionStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.PigmentStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.SlurryStackIngredientCreator;
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
    @DisplayName("Test getting InfusionStackIngredient creator")
    void testGetInfusionStackIngredientCreator() {
        Assertions.assertInstanceOf(InfusionStackIngredientCreator.class, IngredientCreatorAccess.infusion());
    }

    @Test
    @DisplayName("Test getting PigmentStackIngredient creator")
    void testGetPigmentStackIngredientCreator() {
        Assertions.assertInstanceOf(PigmentStackIngredientCreator.class, IngredientCreatorAccess.pigment());
    }

    @Test
    @DisplayName("Test getting SlurryStackIngredient creator")
    void testGetSlurryStackIngredientCreator() {
        Assertions.assertInstanceOf(SlurryStackIngredientCreator.class, IngredientCreatorAccess.slurry());
    }
}