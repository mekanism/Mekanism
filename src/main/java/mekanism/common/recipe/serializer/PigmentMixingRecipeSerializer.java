package mekanism.common.recipe.serializer;

import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class PigmentMixingRecipeSerializer<RECIPE extends PigmentMixingRecipe> extends
      ChemicalChemicalToChemicalRecipeSerializer<Pigment, PigmentStack, PigmentStackIngredient, RECIPE> {

    public PigmentMixingRecipeSerializer(IFactory<Pigment, PigmentStack, PigmentStackIngredient, RECIPE> factory) {
        super(factory, ChemicalUtils.PIGMENT_STACK_CODEC);
    }

    @Override
    protected ChemicalIngredientDeserializer<Pigment, PigmentStack, PigmentStackIngredient> getDeserializer() {
        return ChemicalIngredientDeserializer.PIGMENT;
    }

    @Override
    protected PigmentStack fromBuffer(@NotNull FriendlyByteBuf buffer) {
        return PigmentStack.readFromPacket(buffer);
    }
}