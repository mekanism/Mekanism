package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.recipe.serializer.SerializerHelper;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.advancements.Advancement;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackGasToItemStackRecipeBuilder extends MekanismRecipeBuilder<ItemStackGasToItemStackRecipe, ItemStackGasToItemStackRecipeBuilder> {

    private final ItemStackIngredient itemInput;
    private final GasStackIngredient gasInput;
    private final ItemStack output;

    protected ItemStackGasToItemStackRecipeBuilder(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output,
          IRecipeSerializer<ItemStackGasToItemStackRecipe> recipeSerializer) {
        super(recipeSerializer);
        this.itemInput = itemInput;
        this.gasInput = gasInput;
        this.output = output;
    }

    public static ItemStackGasToItemStackRecipeBuilder compressing(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This compressing recipe requires a non empty item output.");
        }
        return new ItemStackGasToItemStackRecipeBuilder(itemInput, gasInput, output, MekanismRecipeSerializers.COMPRESSING.getRecipeSerializer());
    }

    public static ItemStackGasToItemStackRecipeBuilder purifying(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This purifying recipe requires a non empty item output.");
        }
        return new ItemStackGasToItemStackRecipeBuilder(itemInput, gasInput, output, MekanismRecipeSerializers.PURIFYING.getRecipeSerializer());
    }

    public static ItemStackGasToItemStackRecipeBuilder injecting(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This injecting recipe requires a non empty item output.");
        }
        return new ItemStackGasToItemStackRecipeBuilder(itemInput, gasInput, output, MekanismRecipeSerializers.INJECTING.getRecipeSerializer());
    }

    @Override
    public ItemStackGasToItemStackRecipeResult getResult(ResourceLocation id) {
        return new ItemStackGasToItemStackRecipeResult(id, itemInput, gasInput, output, advancementBuilder,
              new ResourceLocation(id.getNamespace(), "recipes/" + output.getItem().getGroup().getPath() + "/" + id.getPath()), recipeSerializer);
    }

    public static class ItemStackGasToItemStackRecipeResult extends RecipeResult<ItemStackGasToItemStackRecipe> {

        private final ItemStackIngredient itemInput;
        private final GasStackIngredient gasInput;
        private final ItemStack output;

        public ItemStackGasToItemStackRecipeResult(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output,
              Advancement.Builder advancementBuilder, ResourceLocation advancementId, IRecipeSerializer<ItemStackGasToItemStackRecipe> recipeSerializer) {
            super(id, advancementBuilder, advancementId, recipeSerializer);
            this.itemInput = itemInput;
            this.gasInput = gasInput;
            this.output = output;
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("itemInput", itemInput.serialize());
            json.add("gasInput", gasInput.serialize());
            json.add("output", SerializerHelper.serializeItemStack(output));
        }
    }
}