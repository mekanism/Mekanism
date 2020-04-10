package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.util.ResourceLocation;

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
        return new ItemStackGasToGasRecipeResult(id);
    }

    public class ItemStackGasToGasRecipeResult extends RecipeResult {

        protected ItemStackGasToGasRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add(JsonConstants.ITEM_INPUT, itemInput.serialize());
            json.add(JsonConstants.GAS_INPUT, gasInput.serialize());
            json.add(JsonConstants.OUTPUT, SerializerHelper.serializeGasStack(output));
        }
    }
}