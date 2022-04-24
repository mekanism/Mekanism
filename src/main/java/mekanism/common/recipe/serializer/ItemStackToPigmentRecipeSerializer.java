package mekanism.common.recipe.serializer;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import net.minecraft.network.FriendlyByteBuf;

public class ItemStackToPigmentRecipeSerializer<RECIPE extends ItemStackToPigmentRecipe> extends ItemStackToChemicalRecipeSerializer<Pigment, PigmentStack, RECIPE> {

    public ItemStackToPigmentRecipeSerializer(IFactory<Pigment, PigmentStack, RECIPE> factory) {
        super(factory);
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