package mekanism.api.recipes;

import java.util.List;
import java.util.function.BiPredicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for defining item chemical to item recipes.
 * <br>
 * Input: ItemStack
 * <br>
 * Input: Chemical
 * <br>
 * Output: OUTPUT
 *
 * @since 10.7.0
 */
@NothingNullByDefault
public abstract class ItemStackChemicalToObjectRecipe<OUTPUT> extends MekanismRecipe<SingleItemChemicalRecipeInput> implements BiPredicate<@NotNull ItemStack, ChemicalStack> {

    /**
     * Represents whether this recipe consumes the chemical each tick.
     *
     * @since 10.7.0
     */
    public abstract boolean perTickUsage();

    /**
     * Gets the input item ingredient.
     */
    public abstract ItemStackIngredient getItemInput();

    /**
     * Gets the input chemical ingredient.
     */
    public abstract ChemicalStackIngredient getChemicalInput();

    /**
     * Gets a new output based on the given inputs.
     *
     * @param inputItem     Specific item input.
     * @param inputChemical Specific chemical input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the inputs, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in inputs should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public abstract OUTPUT getOutput(ItemStack inputItem, ChemicalStack inputChemical);


    @Override
    public abstract boolean test(ItemStack itemStack, ChemicalStack chemicalStack);

    @Override
    public boolean matches(SingleItemChemicalRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.item(), input.chemical());
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<@NotNull OUTPUT> getOutputDefinition();

    @Override
    public boolean isIncomplete() {
        return getItemInput().hasNoMatchingInstances() || getChemicalInput().hasNoMatchingInstances();
    }

    @Override
    public void logMissingTags() {
        getItemInput().logMissingTags();
        getChemicalInput().logMissingTags();
    }
}