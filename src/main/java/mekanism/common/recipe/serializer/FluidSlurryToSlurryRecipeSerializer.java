package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class FluidSlurryToSlurryRecipeSerializer<RECIPE extends FluidSlurryToSlurryRecipe> implements RecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;
    private Codec<RECIPE> codec;

    public FluidSlurryToSlurryRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public Codec<RECIPE> codec() {
        if (codec == null) {
            codec = RecordCodecBuilder.create(instance->instance.group(
                  IngredientCreatorAccess.fluid().codec().fieldOf(JsonConstants.FLUID_INPUT).forGetter(FluidSlurryToSlurryRecipe::getFluidInput),
                  IngredientCreatorAccess.slurry().codec().fieldOf(JsonConstants.SLURRY_INPUT).forGetter(FluidSlurryToSlurryRecipe::getChemicalInput),
                  SlurryStack.CODEC.fieldOf(JsonConstants.OUTPUT).forGetter(FluidSlurryToSlurryRecipe::getOutputRaw)
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @Override
    public RECIPE fromNetwork(@NotNull FriendlyByteBuf buffer) {
        try {
            FluidStackIngredient fluidInput = IngredientCreatorAccess.fluid().read(buffer);
            SlurryStackIngredient slurryInput = IngredientCreatorAccess.slurry().read(buffer);
            SlurryStack output = SlurryStack.readFromPacket(buffer);
            return this.factory.create(fluidInput, slurryInput, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading fluid slurry to slurry recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing fluid slurry to slurry recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends FluidSlurryToSlurryRecipe> {

        RECIPE create(FluidStackIngredient fluidInput, SlurryStackIngredient slurryInput, SlurryStack output);
    }
}