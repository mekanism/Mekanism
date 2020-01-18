package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.common.recipe.serializer.SerializerHelper;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.advancements.Advancement;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidToFluidRecipeBuilder extends MekanismRecipeBuilder<FluidToFluidRecipe, FluidToFluidRecipeBuilder> {

    private final FluidStackIngredient input;
    private final FluidStack output;

    protected FluidToFluidRecipeBuilder(FluidStackIngredient input, FluidStack output) {
        super(MekanismRecipeSerializers.EVAPORATING.getRecipeSerializer());
        this.input = input;
        this.output = output;
    }

    public static FluidToFluidRecipeBuilder evaporating(FluidStackIngredient input, FluidStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This evaporating recipe requires a non empty fluid output.");
        }
        return new FluidToFluidRecipeBuilder(input, output);
    }

    @Override
    public FluidToFluidRecipeResult getResult(ResourceLocation id) {
        return new FluidToFluidRecipeResult(id, input, output, advancementBuilder, new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath()), recipeSerializer);
    }

    public static class FluidToFluidRecipeResult extends RecipeResult<FluidToFluidRecipe> {

        private final FluidStackIngredient input;
        private final FluidStack output;

        public FluidToFluidRecipeResult(ResourceLocation id, FluidStackIngredient input, FluidStack output, Advancement.Builder advancementBuilder,
              ResourceLocation advancementId, IRecipeSerializer<FluidToFluidRecipe> recipeSerializer) {
            super(id, advancementBuilder, advancementId, recipeSerializer);
            this.input = input;
            this.output = output;
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("input", input.serialize());
            json.add("output", SerializerHelper.serializeFluidStack(output));
        }
    }
}