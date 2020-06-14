package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.chemical.SlurryStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class FluidSlurryToSlurryRecipeSerializer<RECIPE extends FluidSlurryToSlurryRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;

    public FluidSlurryToSlurryRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public RECIPE read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement fluidInput = JSONUtils.isJsonArray(json, JsonConstants.FLUID_INPUT) ? JSONUtils.getJsonArray(json, JsonConstants.FLUID_INPUT) :
                                 JSONUtils.getJsonObject(json, JsonConstants.FLUID_INPUT);
        FluidStackIngredient fluidIngredient = FluidStackIngredient.deserialize(fluidInput);
        JsonElement slurryInput = JSONUtils.isJsonArray(json, JsonConstants.SLURRY_INPUT) ? JSONUtils.getJsonArray(json, JsonConstants.SLURRY_INPUT) :
                                  JSONUtils.getJsonObject(json, JsonConstants.SLURRY_INPUT);
        SlurryStackIngredient slurryIngredient = SlurryStackIngredient.deserialize(slurryInput);
        SlurryStack output = SerializerHelper.getSlurryStack(json, JsonConstants.OUTPUT);
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Recipe output must not be empty.");
        }
        return this.factory.create(recipeId, fluidIngredient, slurryIngredient, output);
    }

    @Override
    public RECIPE read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        try {
            FluidStackIngredient fluidInput = FluidStackIngredient.read(buffer);
            SlurryStackIngredient slurryInput = SlurryStackIngredient.read(buffer);
            SlurryStack output = SlurryStack.readFromPacket(buffer);
            return this.factory.create(recipeId, fluidInput, slurryInput, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading fluid slurry to slurry recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing fluid slurry to slurry recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends FluidSlurryToSlurryRecipe> {

        RECIPE create(ResourceLocation id, FluidStackIngredient fluidInput, SlurryStackIngredient slurryInput, SlurryStack output);
    }
}