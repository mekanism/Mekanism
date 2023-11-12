package mekanism.common.recipe.serializer;

import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.basic.BasicChemicalInfuserRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.common.recipe.impl.ChemicalInfuserIRecipe;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class ChemicalInfuserRecipeSerializer extends
      ChemicalChemicalToChemicalRecipeSerializer<Gas, GasStack, GasStackIngredient, BasicChemicalInfuserRecipe> {

    public ChemicalInfuserRecipeSerializer(IFactory<Gas, GasStack, GasStackIngredient, BasicChemicalInfuserRecipe> factory) {
        super(factory, ChemicalUtils.GAS_STACK_CODEC);
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