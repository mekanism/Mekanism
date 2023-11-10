package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.impl.FluidToFluidIRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class FluidToFluidRecipeSerializer<RECIPE extends FluidToFluidIRecipe> implements RecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;
    private Codec<RECIPE> codec;

    public FluidToFluidRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public Codec<RECIPE> codec() {
        if (codec == null) {
            codec = RecordCodecBuilder.create(instance->instance.group(
                  IngredientCreatorAccess.fluid().codec().fieldOf(JsonConstants.INPUT).forGetter(FluidToFluidRecipe::getInput),
                  SerializerHelper.FLUIDSTACK_CODEC.fieldOf(JsonConstants.OUTPUT).forGetter(FluidToFluidIRecipe::getOutputRaw)
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @Override
    public RECIPE fromNetwork(@NotNull FriendlyByteBuf buffer) {
        try {
            FluidStackIngredient inputIngredient = IngredientCreatorAccess.fluid().read(buffer);
            FluidStack output = FluidStack.readFromPacket(buffer);
            return this.factory.create(inputIngredient, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading fluid to fluid recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing fluid to fluid recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends FluidToFluidRecipe> {

        RECIPE create(FluidStackIngredient input, FluidStack output);
    }
}