package mekanism.api.recipes;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for defining ItemStack to chemical recipes.
 * <br>
 * Input: ItemStack
 * <br>
 * Output: ChemicalStack
 *
 * @apiNote There are currently three types of ItemStack to Chemical recipe types:
 *  <ul>
 *  <li>Oxidizing: Can be processed in a Chemical Oxidizer.</li>
 *  <li>Chemical Conversion: Can be processed by any slots in Mekanism machines that are able to convert items to chemicals, for example in the Osmium Compressor and a variety of other machines.</li>
 *  <li>Pigment Extracting: Can be processed in a Pigment Extractor.</li>
 * </ul>
 */
@NothingNullByDefault
public abstract class ItemStackToChemicalRecipe extends MekanismRecipe<SingleRecipeInput> implements Predicate<@NotNull ItemStack> {

    @Override
    public abstract boolean test(ItemStack itemStack);

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.item());
    }

    /**
     * Gets the input ingredient.
     */
    public abstract ItemStackIngredient getInput();

    /**
     * Gets a new output based on the given input.
     *
     * @param input Specific input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public abstract ChemicalStack getOutput(ItemStack input);

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<ChemicalStack> getOutputDefinition();

    @Override
    public boolean isIncomplete() {
        return getInput().hasNoMatchingInstances();
    }

    @Override
    public void logMissingTags() {
        getInput().logMissingTags();
    }
}