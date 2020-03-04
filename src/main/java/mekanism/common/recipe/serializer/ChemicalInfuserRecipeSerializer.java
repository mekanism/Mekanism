package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ChemicalInfuserRecipeSerializer<T extends ChemicalInfuserRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

    private final IFactory<T> factory;

    public ChemicalInfuserRecipeSerializer(IFactory<T> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement leftIngredients = JSONUtils.isJsonArray(json, "leftInput") ? JSONUtils.getJsonArray(json, "leftInput") :
                                      JSONUtils.getJsonObject(json, "leftInput");
        GasStackIngredient leftInput = GasStackIngredient.deserialize(leftIngredients);
        JsonElement rightIngredients = JSONUtils.isJsonArray(json, "rightInput") ? JSONUtils.getJsonArray(json, "rightInput") :
                                       JSONUtils.getJsonObject(json, "rightInput");
        GasStackIngredient rightInput = GasStackIngredient.deserialize(rightIngredients);
        GasStack output = SerializerHelper.getGasStack(json, "output");
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Chemical infuser recipe output must not be empty.");
        }
        return this.factory.create(recipeId, leftInput, rightInput, output);
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        try {
            GasStackIngredient leftInput = GasStackIngredient.read(buffer);
            GasStackIngredient rightInput = GasStackIngredient.read(buffer);
            GasStack output = GasStack.readFromPacket(buffer);
            return this.factory.create(recipeId, leftInput, rightInput, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading chemical infuser recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing chemical infuser recipe to packet.", e);
            throw e;
        }
    }

    public interface IFactory<T extends ChemicalInfuserRecipe> {

        T create(ResourceLocation id, GasStackIngredient leftInput, GasStackIngredient rightInput, GasStack output);
    }
}