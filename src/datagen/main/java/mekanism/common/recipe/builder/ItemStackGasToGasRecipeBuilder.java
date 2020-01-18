package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.recipe.serializer.SerializerHelper;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.advancements.Advancement;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackGasToGasRecipeBuilder extends MekanismRecipeBuilder<ItemStackGasToGasRecipe, ItemStackGasToGasRecipeBuilder> {

    private final ItemStackIngredient itemInput;
    private final GasStackIngredient gasInput;
    private final GasStack output;

    protected ItemStackGasToGasRecipeBuilder(ItemStackIngredient itemInput, GasStackIngredient gasInput, GasStack output) {
        super(MekanismRecipeSerializers.DISSOLUTION.getRecipeSerializer());
        this.itemInput = itemInput;
        this.gasInput = gasInput;
        this.output = output;
    }

    public static ItemStackGasToGasRecipeBuilder dissolution(ItemStackIngredient itemInput, GasStackIngredient gasInput, GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This dissolution chamber recipe requires a non empty gas output.");
        }
        return new ItemStackGasToGasRecipeBuilder(itemInput, gasInput, output);
    }

    @Override
    public ItemStackGasToGasRecipeResult getResult(ResourceLocation id) {
        return new ItemStackGasToGasRecipeResult(id, itemInput, gasInput, output, advancementBuilder,
              new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath()), recipeSerializer);
    }

    public static class ItemStackGasToGasRecipeResult extends RecipeResult<ItemStackGasToGasRecipe> {

        private final ItemStackIngredient itemInput;
        private final GasStackIngredient gasInput;
        private final GasStack output;

        public ItemStackGasToGasRecipeResult(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, GasStack output,
              Advancement.Builder advancementBuilder, ResourceLocation advancementId, IRecipeSerializer<ItemStackGasToGasRecipe> recipeSerializer) {
            super(id, advancementBuilder, advancementId, recipeSerializer);
            this.itemInput = itemInput;
            this.gasInput = gasInput;
            this.output = output;
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("itemInput", itemInput.serialize());
            json.add("gasInput", gasInput.serialize());
            json.add("output", SerializerHelper.serializeGasStack(output));
        }
    }
}