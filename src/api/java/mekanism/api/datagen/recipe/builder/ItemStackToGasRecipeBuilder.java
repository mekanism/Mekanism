package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackToGasRecipeBuilder extends MekanismRecipeBuilder<ItemStackToGasRecipeBuilder> {

    private final ItemStackIngredient input;
    private final GasStack output;

    protected ItemStackToGasRecipeBuilder(ItemStackIngredient input, GasStack output, ResourceLocation serializerName) {
        super(serializerName);
        this.input = input;
        this.output = output;
    }

    public static ItemStackToGasRecipeBuilder gasConversion(ItemStackIngredient input, GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This gas conversion recipe requires a non empty gas output.");
        }
        return new ItemStackToGasRecipeBuilder(input, output, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "gas_conversion"));
    }

    public static ItemStackToGasRecipeBuilder oxidizing(ItemStackIngredient input, GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This oxidizing recipe requires a non empty gas output.");
        }
        return new ItemStackToGasRecipeBuilder(input, output, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "oxidizing"));
    }

    @Override
    public ItemStackToGasRecipeResult getResult(ResourceLocation id) {
        return new ItemStackToGasRecipeResult(id, input, output, advancementBuilder, new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath()), serializerName);
    }

    public static class ItemStackToGasRecipeResult extends RecipeResult {

        private final ItemStackIngredient input;
        private final GasStack output;

        public ItemStackToGasRecipeResult(ResourceLocation id, ItemStackIngredient input, GasStack output, Advancement.Builder advancementBuilder,
              ResourceLocation advancementId, ResourceLocation serializerName) {
            super(id, advancementBuilder, advancementId, serializerName);
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