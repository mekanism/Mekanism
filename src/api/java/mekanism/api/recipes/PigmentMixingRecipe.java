package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class PigmentMixingRecipe extends ChemicalChemicalToChemicalRecipe<Pigment, PigmentStack, PigmentStackIngredient> {

    public PigmentMixingRecipe(ResourceLocation id, PigmentStackIngredient leftInput, PigmentStackIngredient rightInput, PigmentStack output) {
        super(id, leftInput, rightInput, output);
    }

    @Override
    public PigmentStack getOutput(PigmentStack input1, PigmentStack input2) {
        return output.copy();
    }
}