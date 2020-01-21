package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackToInfuseTypeRecipeBuilder extends MekanismRecipeBuilder<ItemStackToInfuseTypeRecipeBuilder> {

    private final ItemStackIngredient input;
    private final InfusionStack output;

    protected ItemStackToInfuseTypeRecipeBuilder(ItemStackIngredient input, InfusionStack output) {
        super(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "infusion_conversion"));
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
    protected ItemStackToInfuseTypeRecipeResult getResult(ResourceLocation id) {
        return new ItemStackToInfuseTypeRecipeResult(id, input, output, advancementBuilder, new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath()), serializerName);
    }

    public static class ItemStackToInfuseTypeRecipeResult extends RecipeResult {

        private final ItemStackIngredient input;
        private final InfusionStack output;

        public ItemStackToInfuseTypeRecipeResult(ResourceLocation id, ItemStackIngredient input, InfusionStack output, Advancement.Builder advancementBuilder,
              ResourceLocation advancementId, ResourceLocation serializerName) {
            super(id, advancementBuilder, advancementId, serializerName);
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