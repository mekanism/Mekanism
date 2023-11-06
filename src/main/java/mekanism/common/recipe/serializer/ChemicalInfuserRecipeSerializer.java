package mekanism.common.recipe.serializer;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class ChemicalInfuserRecipeSerializer<RECIPE extends ChemicalInfuserRecipe> extends
      ChemicalChemicalToChemicalRecipeSerializer<Gas, GasStack, GasStackIngredient, RECIPE> {

    public ChemicalInfuserRecipeSerializer(IFactory<Gas, GasStack, GasStackIngredient, RECIPE> factory) {
        super(factory, GasStack.CODEC);
    }

    @Override
    protected ChemicalIngredientDeserializer<Gas, GasStack, GasStackIngredient> getDeserializer() {
        return ChemicalIngredientDeserializer.GAS;
    }

    @Override
    protected GasStack fromBuffer(@NotNull FriendlyByteBuf buffer) {
        return GasStack.readFromPacket(buffer);
    }
}