package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
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
public class ItemStackToItemStackRecipeBuilder extends MekanismRecipeBuilder<ItemStackToItemStackRecipe, ItemStackToItemStackRecipeBuilder> {

    private final ItemStackIngredient input;
    private final ItemStack output;

    protected ItemStackToItemStackRecipeBuilder(ItemStackIngredient input, ItemStack output, IRecipeSerializer<ItemStackToItemStackRecipe> recipeSerializer) {
        super(recipeSerializer);
        this.input = input;
        this.output = output;
    }

    public static ItemStackToItemStackRecipeBuilder crushing(ItemStackIngredient input, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This crushing recipe requires a non empty item output.");
        }
        return new ItemStackToItemStackRecipeBuilder(input, output, MekanismRecipeSerializers.CRUSHING.getRecipeSerializer());
    }

    public static ItemStackToItemStackRecipeBuilder enriching(ItemStackIngredient input, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This enriching recipe requires a non empty item output.");
        }
        return new ItemStackToItemStackRecipeBuilder(input, output, MekanismRecipeSerializers.ENRICHING.getRecipeSerializer());
    }

    public static ItemStackToItemStackRecipeBuilder smelting(ItemStackIngredient input, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This smelting recipe requires a non empty item output.");
        }
        return new ItemStackToItemStackRecipeBuilder(input, output, MekanismRecipeSerializers.SMELTING.getRecipeSerializer());
    }

    @Override
    public ItemStackToItemStackRecipeResult getResult(ResourceLocation id) {
        return new ItemStackToItemStackRecipeResult(id, input, output, advancementBuilder,
              new ResourceLocation(id.getNamespace(), "recipes/" + output.getItem().getGroup().getPath() + "/" + id.getPath()), recipeSerializer);
    }

    public static class ItemStackToItemStackRecipeResult extends RecipeResult<ItemStackToItemStackRecipe> {

        private final ItemStackIngredient input;
        private final ItemStack output;

        public ItemStackToItemStackRecipeResult(ResourceLocation id, ItemStackIngredient input, ItemStack output, Advancement.Builder advancementBuilder,
              ResourceLocation advancementId, IRecipeSerializer<ItemStackToItemStackRecipe> recipeSerializer) {
            super(id, advancementBuilder, advancementId, recipeSerializer);
            this.input = input;
            this.output = output;
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("input", input.serialize());
            json.add("output", SerializerHelper.serializeItemStack(output));
        }
    }
}