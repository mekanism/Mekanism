package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackGasToGasRecipeBuilder extends MekanismRecipeBuilder<ItemStackGasToGasRecipeBuilder> {

    private final ItemStackIngredient itemInput;
    private final GasStackIngredient gasInput;
    private final GasStack output;

    protected ItemStackGasToGasRecipeBuilder(ItemStackIngredient itemInput, GasStackIngredient gasInput, GasStack output) {
        super(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "dissolution"));
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
    protected ItemStackGasToGasRecipeResult getResult(ResourceLocation id) {
        return new ItemStackGasToGasRecipeResult(id, itemInput, gasInput, output, conditions, advancementBuilder,
              new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath()), serializerName);
    }

    public static class ItemStackGasToGasRecipeResult extends RecipeResult {

        private final ItemStackIngredient itemInput;
        private final GasStackIngredient gasInput;
        private final GasStack output;

        public ItemStackGasToGasRecipeResult(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, GasStack output,
              List<ICondition> conditions, Advancement.Builder advancementBuilder, ResourceLocation advancementId, ResourceLocation serializerName) {
            super(id, conditions, advancementBuilder, advancementId, serializerName);
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