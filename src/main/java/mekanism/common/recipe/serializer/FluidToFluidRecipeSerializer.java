package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class FluidToFluidRecipeSerializer<T extends FluidToFluidRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

    private final IFactory<T> factory;

    public FluidToFluidRecipeSerializer(IFactory<T> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement input = JSONUtils.isJsonArray(json, "input") ? JSONUtils.getJsonArray(json, "input") :
                            JSONUtils.getJsonObject(json, "input");
        FluidStackIngredient inputIngredient = FluidStackIngredient.deserialize(input);
        FluidStack output = SerializerHelper.getFluidStack(json, "output");
        return this.factory.create(recipeId, inputIngredient, output);
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        FluidStackIngredient inputIngredient = FluidStackIngredient.read(buffer);
        FluidStack output = FluidStack.readFromPacket(buffer);
        return this.factory.create(recipeId, inputIngredient, output);
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        recipe.write(buffer);
    }

    public interface IFactory<T extends FluidToFluidRecipe> {

        T create(ResourceLocation id, FluidStackIngredient input, FluidStack output);
    }
}