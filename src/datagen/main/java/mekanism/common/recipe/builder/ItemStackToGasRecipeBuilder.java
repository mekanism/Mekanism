package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.recipe.serializer.SerializerHelper;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.advancements.Advancement;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackToGasRecipeBuilder extends MekanismRecipeBuilder<ItemStackToGasRecipe, ItemStackToGasRecipeBuilder> {

    private final ItemStackIngredient input;
    private final GasStack output;

    protected ItemStackToGasRecipeBuilder(ItemStackIngredient input, GasStack output, IRecipeSerializer<ItemStackToGasRecipe> recipeSerializer) {
        super(recipeSerializer);
        this.input = input;
        this.output = output;
    }

    public static ItemStackToGasRecipeBuilder gasConversion(ItemStackIngredient input, GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This gas conversion recipe requires a non empty gas output.");
        }
        return new ItemStackToGasRecipeBuilder(input, output, MekanismRecipeSerializers.GAS_CONVERSION.getRecipeSerializer());
    }

    public static ItemStackToGasRecipeBuilder oxidizing(ItemStackIngredient input, GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This oxidizing recipe requires a non empty gas output.");
        }
        return new ItemStackToGasRecipeBuilder(input, output, MekanismRecipeSerializers.OXIDIZING.getRecipeSerializer());
    }

    @Override
    public ItemStackToGasRecipeResult getResult(ResourceLocation id) {
        return new ItemStackToGasRecipeResult(id, input, output, advancementBuilder, new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath()), recipeSerializer);
    }

    public static class ItemStackToGasRecipeResult extends RecipeResult<ItemStackToGasRecipe> {

        private final ItemStackIngredient input;
        private final GasStack output;

        public ItemStackToGasRecipeResult(ResourceLocation id, ItemStackIngredient input, GasStack output, Advancement.Builder advancementBuilder,
              ResourceLocation advancementId, IRecipeSerializer<ItemStackToGasRecipe> recipeSerializer) {
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