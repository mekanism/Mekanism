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
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.inputs.chemical.ChemicalIngredientDeserializer;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChemicalChemicalToChemicalRecipeBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>> extends MekanismRecipeBuilder<ChemicalChemicalToChemicalRecipeBuilder<CHEMICAL, STACK, INGREDIENT>> {

    private final ChemicalIngredientDeserializer<CHEMICAL, STACK, ?> outputSerializer;
    private final INGREDIENT leftInput;
    private final INGREDIENT rightInput;
    private final STACK output;

    protected ChemicalChemicalToChemicalRecipeBuilder(ResourceLocation serializerName, INGREDIENT leftInput, INGREDIENT rightInput, STACK output,
          ChemicalIngredientDeserializer<CHEMICAL, STACK, ?> outputSerializer) {
        super(serializerName);
        this.leftInput = leftInput;
        this.rightInput = rightInput;
        this.output = output;
        this.outputSerializer = outputSerializer;
    }

    /**
     * Creates a Chemical Infusing recipe builder.
     *
     * @param leftInput  Left input.
     * @param rightInput Right input.
     * @param output     Output.
     */
    public static ChemicalChemicalToChemicalRecipeBuilder<Gas, GasStack, GasStackIngredient> chemicalInfusing(GasStackIngredient leftInput, GasStackIngredient rightInput,
          GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This chemical infusing recipe requires a non empty gas output.");
        }
        return new ChemicalChemicalToChemicalRecipeBuilder<>(mekSerializer("chemical_infusing"), leftInput, rightInput, output, ChemicalIngredientDeserializer.GAS);
    }

    /**
     * Creates a Pigment Mixing recipe builder.
     *
     * @param leftInput  Left input.
     * @param rightInput Right input.
     * @param output     Output.
     */
    public static ChemicalChemicalToChemicalRecipeBuilder<Pigment, PigmentStack, PigmentStackIngredient> pigmentMixing(PigmentStackIngredient leftInput,
          PigmentStackIngredient rightInput, PigmentStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This pigment mixing recipe requires a non empty gas output.");
        }
        return new ChemicalChemicalToChemicalRecipeBuilder<>(mekSerializer("pigment_mixing"), leftInput, rightInput, output, ChemicalIngredientDeserializer.PIGMENT);
    }

    @Override
    protected ChemicalChemicalToChemicalRecipeResult getResult(ResourceLocation id) {
        return new ChemicalChemicalToChemicalRecipeResult(id);
    }

    public class ChemicalChemicalToChemicalRecipeResult extends RecipeResult {

        protected ChemicalChemicalToChemicalRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(@Nonnull JsonObject json) {
            json.add(JsonConstants.LEFT_INPUT, leftInput.serialize());
            json.add(JsonConstants.RIGHT_INPUT, rightInput.serialize());
            json.add(JsonConstants.OUTPUT, outputSerializer.serializeStack(output));
        }
    }
}