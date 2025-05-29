package mekanism.api.recipes;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleChemicalRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;

/**
 * Base class for defining chemical to chemical recipes.
 * <br>
 * Input: Chemical
 * <br>
 * Output: ChemicalStack
 *
 * @apiNote There are currently two types of Chemical to Chemical recipe types:
 * <ul>
 *     <li>Activating: Can be processed in a Solar Neutron Activator.</li>
 *     <li>Centrifuging: Can be processed in an Isotopic Centrifuge.</li>
 * </ul>
 */
@NothingNullByDefault
public abstract class ChemicalToChemicalRecipe extends MekanismRecipe<SingleChemicalRecipeInput> implements Predicate<ChemicalStack> {

    @Override
    public abstract boolean test(ChemicalStack chemicalStack);

    @Override
    public boolean matches(SingleChemicalRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.chemical());
    }

    /**
     * Gets the input ingredient.
     */
    public abstract ChemicalStackIngredient getInput();

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<ChemicalStack> getOutputDefinition();

    /**
     * Gets a new output based on the given input.
     *
     * @param input Specific input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public abstract ChemicalStack getOutput(ChemicalStack input);

    @Override
    public boolean isIncomplete() {
        return getInput().hasNoMatchingInstances();
    }

    @Override
    public void logMissingTags() {
        getInput().logMissingTags();
    }
}