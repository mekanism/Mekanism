package mekanism.common.recipe.serializer;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import mekanism.api.SerializerHelper;
import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import net.minecraft.network.PacketBuffer;

public class ItemStackToInfuseTypeRecipeSerializer<T extends ItemStackToInfuseTypeRecipe> extends ItemStackToChemicalRecipeSerializer<InfuseType, InfusionStack, T> {

    public ItemStackToInfuseTypeRecipeSerializer(IFactory<InfuseType, InfusionStack, T> factory) {
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