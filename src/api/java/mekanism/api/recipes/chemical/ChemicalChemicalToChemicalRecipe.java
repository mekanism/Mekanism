package mekanism.api.recipes.chemical;

import java.util.List;
import java.util.function.BiPredicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.BiChemicalRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for defining chemical+chemical to chemical recipes.
 * <br>
 * Input: Two chemicals of the same chemical type. The order of them does not matter.
 * <br>
 * Output: ChemicalStack of the same chemical type as the input chemicals
 *
 * @param <INGREDIENT> Input Ingredient type
 */
@NothingNullByDefault
public abstract class ChemicalChemicalToChemicalRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK, ?>> extends MekanismRecipe<BiChemicalRecipeInput<CHEMICAL, STACK>>
      implements BiPredicate<@NotNull STACK, @NotNull STACK> {

    @Override
    public abstract boolean test(STACK input1, STACK input2);

    @Override
    public boolean matches(BiChemicalRecipeInput<CHEMICAL, STACK> input, Level level) {
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
    public abstract STACK getOutput(STACK input1, STACK input2);

    /**
     * Gets the left input ingredient.
     */
    public abstract INGREDIENT getLeftInput();

    /**
     * Gets the right input ingredient.
     */
    public abstract INGREDIENT getRightInput();

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<STACK> getOutputDefinition();

    @Override
    public boolean isIncomplete() {
        return getLeftInput().hasNoMatchingInstances() || getRightInput().hasNoMatchingInstances();
    }

}