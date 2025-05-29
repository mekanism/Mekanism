package mekanism.api.recipes;

import java.util.List;
import java.util.function.BiPredicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.BiChemicalRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;

/**
 * Base class for defining chemical+chemical to chemical recipes.
 * <br>
 * Input: Two chemicals. The order of them does not matter.
 * <br>
 * Output: ChemicalStack
 *
 * @apiNote There are currently two types of Chemical+Chemical to Chemical recipe types:
 * <ul>
 *     <li>Chemical Infusing: Can be processed in a Chemical Infuser.</li>
 *     <li>Pigment Mixing: Can be processed in a Pigment Mixer.</li>
 * </ul>
 */
@NothingNullByDefault
public abstract class ChemicalChemicalToChemicalRecipe extends MekanismRecipe<BiChemicalRecipeInput> implements BiPredicate<ChemicalStack, ChemicalStack> {

    @Override
    public abstract boolean test(ChemicalStack input1, ChemicalStack input2);

    @Override
    public boolean matches(BiChemicalRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.left(), input.right());
    }

    /**
     * Gets a new output based on the given inputs, the order of these inputs which one is {@code input1} and which one is {@code input2} does not matter.
     *
     * @param input1 Specific "left" input.
     * @param input2 Specific "right" input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the inputs, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in inputs should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public abstract ChemicalStack getOutput(ChemicalStack input1, ChemicalStack input2);

    /**
     * Gets the left input ingredient.
     */
    public abstract ChemicalStackIngredient getLeftInput();

    /**
     * Gets the right input ingredient.
     */
    public abstract ChemicalStackIngredient getRightInput();

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<ChemicalStack> getOutputDefinition();

    @Override
    public boolean isIncomplete() {
        return getLeftInput().hasNoMatchingInstances() || getRightInput().hasNoMatchingInstances();
    }

    @Override
    public void logMissingTags() {
        getLeftInput().logMissingTags();
        getRightInput().logMissingTags();
    }
}