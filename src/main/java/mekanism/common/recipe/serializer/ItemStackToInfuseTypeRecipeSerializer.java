package mekanism.common.recipe.serializer;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import net.minecraft.network.PacketBuffer;

public class ItemStackToInfuseTypeRecipeSerializer<RECIPE extends ItemStackToInfuseTypeRecipe> extends ItemStackToChemicalRecipeSerializer<InfuseType, InfusionStack, RECIPE> {

    public ItemStackToInfuseTypeRecipeSerializer(IFactory<InfuseType, InfusionStack, RECIPE> factory) {
        super(factory);
    }

    @Override
    protected InfusionStack fromJson(@Nonnull JsonObject json, @Nonnull String key) {
        return SerializerHelper.getInfusionStack(json, key);
    }

    @Override
    protected InfusionStack fromBuffer(@Nonnull PacketBuffer buffer) {
        return InfusionStack.readFromPacket(buffer);
    }
}