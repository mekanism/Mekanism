package mekanism.api.datagen.recipe.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.inputs.chemical.ChemicalIngredientDeserializer;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.util.ResourceLocation;

//TODO - 1.18: Get rid of this class and move the helpers to ChemicalChemicalToChemicalRecipeBuilder
@Deprecated
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChemicalInfuserRecipeBuilder extends ChemicalChemicalToChemicalRecipeBuilder<Gas, GasStack, GasStackIngredient> {

    protected ChemicalInfuserRecipeBuilder(GasStackIngredient leftInput, GasStackIngredient rightInput, GasStack output) {
        super(mekSerializer("chemical_infusing"), leftInput, rightInput, output, ChemicalIngredientDeserializer.GAS);
    }

    /**
     * @deprecated Use {@link ChemicalChemicalToChemicalRecipeBuilder#chemicalInfusing(GasStackIngredient, GasStackIngredient, GasStack)} instead.
     */
    @Deprecated
    public static ChemicalInfuserRecipeBuilder chemicalInfusing(GasStackIngredient leftInput, GasStackIngredient rightInput, GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This chemical infusing recipe requires a non empty gas output.");
        }
        return new ChemicalInfuserRecipeBuilder(leftInput, rightInput, output);
    }

    @Override
    protected ChemicalInfuserRecipeResult getResult(ResourceLocation id) {
        return new ChemicalInfuserRecipeResult(id);
    }

    public class ChemicalInfuserRecipeResult extends ChemicalChemicalToChemicalRecipeResult {

        protected ChemicalInfuserRecipeResult(ResourceLocation id) {
            super(id);
        }
    }
}