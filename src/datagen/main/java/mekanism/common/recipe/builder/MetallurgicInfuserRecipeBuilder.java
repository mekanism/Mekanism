package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.inputs.InfusionIngredient;
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
public class MetallurgicInfuserRecipeBuilder extends MekanismRecipeBuilder<MetallurgicInfuserRecipe, MetallurgicInfuserRecipeBuilder> {

    private final ItemStackIngredient itemInput;
    private final InfusionIngredient infusionInput;
    private final ItemStack output;

    protected MetallurgicInfuserRecipeBuilder(ItemStackIngredient itemInput, InfusionIngredient infusionInput, ItemStack output) {
        super(MekanismRecipeSerializers.METALLURGIC_INFUSING.getRecipeSerializer());
        this.itemInput = itemInput;
        this.infusionInput = infusionInput;
        this.output = output;
    }

    public static MetallurgicInfuserRecipeBuilder metallurgicInfusing(ItemStackIngredient itemInput, InfusionIngredient infusionInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This metallurgic infusing recipe requires a non empty output.");
        }
        return new MetallurgicInfuserRecipeBuilder(itemInput, infusionInput, output);
    }

    @Override
    public MetallurgicInfuserRecipeResult getResult(ResourceLocation id) {
        return new MetallurgicInfuserRecipeResult(id, itemInput, infusionInput, output, advancementBuilder,
              new ResourceLocation(id.getNamespace(), "recipes/" + output.getItem().getGroup().getPath() + "/" + id.getPath()), recipeSerializer);
    }

    public static class MetallurgicInfuserRecipeResult extends RecipeResult<MetallurgicInfuserRecipe> {

        private final ItemStackIngredient itemInput;
        private final InfusionIngredient infusionInput;
        private final ItemStack output;

        public MetallurgicInfuserRecipeResult(ResourceLocation id, ItemStackIngredient itemInput, InfusionIngredient infusionInput, ItemStack output,
              Advancement.Builder advancementBuilder, ResourceLocation advancementId, IRecipeSerializer<MetallurgicInfuserRecipe> recipeSerializer) {
            super(id, advancementBuilder, advancementId, recipeSerializer);
            this.itemInput = itemInput;
            this.infusionInput = infusionInput;
            this.output = output;
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("itemInput", itemInput.serialize());
            json.add("infusionInput", infusionInput.serialize());
            json.add("output", SerializerHelper.serializeItemStack(output));
        }
    }
}