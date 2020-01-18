package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.common.recipe.serializer.SerializerHelper;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.advancements.Advancement;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidGasToGasRecipeBuilder extends MekanismRecipeBuilder<FluidGasToGasRecipe, FluidGasToGasRecipeBuilder> {

    private final GasStackIngredient gasInput;
    private final FluidStackIngredient fluidInput;
    private final GasStack output;

    protected FluidGasToGasRecipeBuilder(FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack output) {
        super(MekanismRecipeSerializers.WASHING.getRecipeSerializer());
        this.fluidInput = fluidInput;
        this.gasInput = gasInput;
        this.output = output;
    }

    public static FluidGasToGasRecipeBuilder washing(FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This washing recipe requires a non empty gas output.");
        }
        return new FluidGasToGasRecipeBuilder(fluidInput, gasInput, output);
    }

    @Override
    public FluidGasToGasRecipeResult getResult(ResourceLocation id) {
        return new FluidGasToGasRecipeResult(id, fluidInput, gasInput, output, advancementBuilder,
              new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath()), recipeSerializer);
    }

    public static class FluidGasToGasRecipeResult extends RecipeResult<FluidGasToGasRecipe> {

        private final GasStackIngredient gasInput;
        private final FluidStackIngredient fluidInput;
        private final GasStack output;

        public FluidGasToGasRecipeResult(ResourceLocation id, FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack output,
              Advancement.Builder advancementBuilder, ResourceLocation advancementId, IRecipeSerializer<FluidGasToGasRecipe> recipeSerializer) {
            super(id, advancementBuilder, advancementId, recipeSerializer);
            this.fluidInput = fluidInput;
            this.gasInput = gasInput;
            this.output = output;
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("fluidInput", fluidInput.serialize());
            json.add("gasInput", gasInput.serialize());
            json.add("output", SerializerHelper.serializeGasStack(output));
        }
    }
}