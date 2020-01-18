package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.recipe.serializer.SerializerHelper;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.advancements.Advancement;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackToInfuseTypeRecipeBuilder extends MekanismRecipeBuilder<ItemStackToInfuseTypeRecipe, ItemStackToInfuseTypeRecipeBuilder> {

    private final ItemStackIngredient input;
    private final InfusionStack output;

    protected ItemStackToInfuseTypeRecipeBuilder(ItemStackIngredient input, InfusionStack output) {
        super(MekanismRecipeSerializers.INFUSION_CONVERSION.getRecipeSerializer());
        this.input = input;
        this.output = output;
    }

    public static ItemStackToInfuseTypeRecipeBuilder infusionConversion(ItemStackIngredient input, InfusionStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This infusion conversion recipe requires a non empty infusion output.");
        }
        return new ItemStackToInfuseTypeRecipeBuilder(input, output);
    }

    @Override
    public ItemStackToInfuseTypeRecipeResult getResult(ResourceLocation id) {
        return new ItemStackToInfuseTypeRecipeResult(id, input, output, advancementBuilder, new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath()), recipeSerializer);
    }

    public static class ItemStackToInfuseTypeRecipeResult extends RecipeResult<ItemStackToInfuseTypeRecipe> {

        private final ItemStackIngredient input;
        private final InfusionStack output;

        public ItemStackToInfuseTypeRecipeResult(ResourceLocation id, ItemStackIngredient input, InfusionStack output, Advancement.Builder advancementBuilder,
              ResourceLocation advancementId, IRecipeSerializer<ItemStackToInfuseTypeRecipe> recipeSerializer) {
            super(id, advancementBuilder, advancementId, recipeSerializer);
            this.input = input;
            this.output = output;
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("input", input.serialize());
            json.add("output", SerializerHelper.serializeInfusionStack(output));
        }
    }
}