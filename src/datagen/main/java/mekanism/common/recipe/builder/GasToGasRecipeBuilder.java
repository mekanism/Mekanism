package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.common.recipe.serializer.SerializerHelper;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.advancements.Advancement;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasToGasRecipeBuilder extends MekanismRecipeBuilder<GasToGasRecipe, GasToGasRecipeBuilder> {

    private final GasStackIngredient input;
    private final GasStack output;

    protected GasToGasRecipeBuilder(GasStackIngredient input, GasStack output) {
        super(MekanismRecipeSerializers.ACTIVATING.getRecipeSerializer());
        this.input = input;
        this.output = output;
    }

    public static GasToGasRecipeBuilder activating(GasStackIngredient input, GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This solar neutron activator recipe requires a non empty gas output.");
        }
        return new GasToGasRecipeBuilder(input, output);
    }

    @Override
    public GasToGasRecipeResult getResult(ResourceLocation id) {
        return new GasToGasRecipeResult(id, input, output, advancementBuilder, new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath()), recipeSerializer);
    }

    public static class GasToGasRecipeResult extends RecipeResult<GasToGasRecipe> {

        private final GasStackIngredient input;
        private final GasStack output;

        public GasToGasRecipeResult(ResourceLocation id, GasStackIngredient input, GasStack output, Advancement.Builder advancementBuilder,
              ResourceLocation advancementId, IRecipeSerializer<GasToGasRecipe> recipeSerializer) {
            super(id, advancementBuilder, advancementId, recipeSerializer);
            this.input = input;
            this.output = output;
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("input", input.serialize());
            json.add("output", SerializerHelper.serializeGasStack(output));
        }
    }
}