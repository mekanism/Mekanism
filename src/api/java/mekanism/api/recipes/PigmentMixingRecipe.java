package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import net.minecraft.util.ResourceLocation;

/**
 * Input: Two pigments. The order of them does not matter.
 * <br>
 * Output: PigmentStack
 *
 * @apiNote Pigment Mixers can process this recipe type and the pigments can be put in any order into the mixer.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class PigmentMixingRecipe extends ChemicalChemicalToChemicalRecipe<Pigment, PigmentStack, PigmentStackIngredient> {

    /**
     * @param id         Recipe name.
     * @param leftInput  Left input.
     * @param rightInput Right input.
     * @param output     Output.
     *
     * @apiNote The order of the inputs does not matter.
     */
    public PigmentMixingRecipe(ResourceLocation id, PigmentStackIngredient leftInput, PigmentStackIngredient rightInput, PigmentStack output) {
        super(id, leftInput, rightInput, output);
    }
}