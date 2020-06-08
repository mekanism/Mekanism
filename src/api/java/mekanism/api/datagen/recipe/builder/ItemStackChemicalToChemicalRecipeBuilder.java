package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.ChemicalIngredientDeserializer;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackChemicalToChemicalRecipeBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>> extends MekanismRecipeBuilder<ItemStackChemicalToChemicalRecipeBuilder<CHEMICAL, STACK, INGREDIENT>> {

    private final ChemicalIngredientDeserializer<CHEMICAL, STACK, INGREDIENT> outputSerializer;
    private final ItemStackIngredient itemInput;
    private final INGREDIENT chemicalInput;
    private final STACK output;

    protected ItemStackChemicalToChemicalRecipeBuilder(ResourceLocation serializerName, ItemStackIngredient itemInput, INGREDIENT chemicalInput, STACK output,
          ChemicalIngredientDeserializer<CHEMICAL, STACK, INGREDIENT> outputSerializer) {
        super(serializerName);
        this.itemInput = itemInput;
        this.chemicalInput = chemicalInput;
        this.output = output;
        this.outputSerializer = outputSerializer;
    }

    public static ItemStackChemicalToChemicalRecipeBuilder<Gas, GasStack, GasStackIngredient> dissolution(ItemStackIngredient itemInput, GasStackIngredient gasInput,
          GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This dissolution chamber recipe requires a non empty gas output.");
        }
        return new ItemStackChemicalToChemicalRecipeBuilder<>(mekSerializer("dissolution"), itemInput, gasInput, output, ChemicalIngredientDeserializer.GAS);
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
            //TODO - V10: Either make this a param, or change it to CHEMICAL_INPUT
            json.add(JsonConstants.GAS_INPUT, chemicalInput.serialize());
            json.add(JsonConstants.OUTPUT, outputSerializer.serializeStack(output));
        }
    }
}