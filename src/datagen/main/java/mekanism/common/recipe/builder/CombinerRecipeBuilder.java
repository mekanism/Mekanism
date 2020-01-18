package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.CombinerRecipe;
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
public class CombinerRecipeBuilder extends MekanismRecipeBuilder<CombinerRecipe, CombinerRecipeBuilder> {

    private final ItemStackIngredient mainInput;
    private final ItemStackIngredient extraInput;
    private final ItemStack output;

    protected CombinerRecipeBuilder(ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output) {
        super(MekanismRecipeSerializers.COMBINING.getRecipeSerializer());
        this.mainInput = mainInput;
        this.extraInput = extraInput;
        this.output = output;
    }

    public static CombinerRecipeBuilder combining(ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This combining recipe requires a non empty item output.");
        }
        return new CombinerRecipeBuilder(mainInput, extraInput, output);
    }

    @Override
    public CombinerRecipeResult getResult(ResourceLocation id) {
        return new CombinerRecipeResult(id, mainInput, extraInput, output, advancementBuilder,
              new ResourceLocation(id.getNamespace(), "recipes/" + output.getItem().getGroup().getPath() + "/" + id.getPath()), recipeSerializer);
    }

    public static class CombinerRecipeResult extends RecipeResult<CombinerRecipe> {

        private final ItemStackIngredient mainInput;
        private final ItemStackIngredient extraInput;
        private final ItemStack output;

        public CombinerRecipeResult(ResourceLocation id, ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output,
              Advancement.Builder advancementBuilder, ResourceLocation advancementId, IRecipeSerializer<CombinerRecipe> recipeSerializer) {
            super(id, advancementBuilder, advancementId, recipeSerializer);
            this.mainInput = mainInput;
            this.extraInput = extraInput;
            this.output = output;
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("mainInput", mainInput.serialize());
            json.add("extraInput", extraInput.serialize());
            json.add("output", SerializerHelper.serializeItemStack(output));
        }
    }
}