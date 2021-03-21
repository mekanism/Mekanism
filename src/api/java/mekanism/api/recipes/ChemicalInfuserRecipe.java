package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ChemicalInfuserRecipe extends ChemicalChemicalToChemicalRecipe<Gas, GasStack, GasStackIngredient> {

    public ChemicalInfuserRecipe(ResourceLocation id, GasStackIngredient leftInput, GasStackIngredient rightInput, GasStack output) {
        super(id, leftInput, rightInput, output);
    }

    @Override
    public GasStack getOutput(GasStack input1, GasStack input2) {
        return output.copy();
    }
}