package mekanism.common.recipe.serializer;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import net.minecraft.network.FriendlyByteBuf;

public class PigmentMixingRecipeSerializer<RECIPE extends PigmentMixingRecipe> extends
      ChemicalChemicalToChemicalRecipeSerializer<Pigment, PigmentStack, PigmentStackIngredient, RECIPE> {

    public PigmentMixingRecipeSerializer(IFactory<Pigment, PigmentStack, PigmentStackIngredient, RECIPE> factory) {
        super(factory);
    }

    @Override
    protected ChemicalIngredientDeserializer<Pigment, PigmentStack, PigmentStackIngredient> getDeserializer() {
        return ChemicalIngredientDeserializer.PIGMENT;
    }

    @Override
    protected PigmentStack fromJson(@Nonnull JsonObject json, @Nonnull String key) {
        return SerializerHelper.getPigmentStack(json, key);
    }

    @Override
    protected PigmentStack fromBuffer(@Nonnull FriendlyByteBuf buffer) {
        return PigmentStack.readFromPacket(buffer);
    }
}