package mekanism.api.recipes;

import java.util.List;
import java.util.function.BiPredicate;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class ChemicalDissolutionRecipe extends MekanismRecipe implements BiPredicate<@NotNull ItemStack, @NotNull GasStack> {

    /**
     * Gets the input item ingredient.
     */
    public abstract ItemStackIngredient getItemInput();

    /**
     * Gets the input gas ingredient.
     */
    public abstract GasStackIngredient getGasInput();

    /**
     * Gets a new output based on the given inputs.
     *
     * @param inputItem Specific item input.
     * @param inputGas  Specific gas input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the inputs, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in inputs should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public abstract BoxedChemicalStack getOutput(ItemStack inputItem, GasStack inputGas);

    @Override
    public abstract boolean test(ItemStack itemStack, GasStack gasStack);

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<BoxedChemicalStack> getOutputDefinition();

    @Override
    public boolean isIncomplete() {
        return getItemInput().hasNoMatchingInstances() || getGasInput().hasNoMatchingInstances();
    }
}
