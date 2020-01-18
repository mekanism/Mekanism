package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.inputs.GasStackIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasToItemStackRecipeBuilder extends MekanismRecipeBuilder<GasToItemStackRecipeBuilder> {

    private final GasStackIngredient input;
    private final ItemStack output;

    protected GasToItemStackRecipeBuilder(GasStackIngredient input, ItemStack output) {
        super(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "crystallizing"));
        this.input = input;
        this.output = output;
    }

    public static GasToItemStackRecipeBuilder crystallizing(GasStackIngredient input, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This crystallizing recipe requires a non empty item output.");
        }
        return new GasToItemStackRecipeBuilder(input, output);
    }

    @Override
    public GasToItemStackRecipeResult getResult(ResourceLocation id) {
        return new GasToItemStackRecipeResult(id, input, output, advancementBuilder, new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath()), serializerName);
    }

    public static class GasToItemStackRecipeResult extends RecipeResult {

        private final GasStackIngredient input;
        private final ItemStack output;

        public GasToItemStackRecipeResult(ResourceLocation id, GasStackIngredient input, ItemStack output, Advancement.Builder advancementBuilder,
              ResourceLocation advancementId, ResourceLocation serializerName) {
            super(id, advancementBuilder, advancementId, serializerName);
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