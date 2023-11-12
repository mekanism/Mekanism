package mekanism.api.recipes;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;

/**
 * Input: ItemStack
 * <br>
 * Output: GasStack
 *
 * @apiNote There are currently two types of ItemStack to Gas recipe types:
 * <ul>
 *     <li>Oxidizing: Can be processed in a Chemical Oxidizer.</li>
 *     <li>Gas Conversion: Can be processed by any slots in Mekanism machines that are able to convert items to gases, for example in the Osmium Compressor and a variety of other machines.</li>
 * </ul>
 */
@NothingNullByDefault
public abstract class ItemStackToGasRecipe extends ItemStackToChemicalRecipe<Gas, GasStack> {

    @Override
    public abstract boolean test(ItemStack itemStack);

    @Override
    public abstract ItemStackIngredient getInput();

    @Override
    @Contract(value = "_ -> new", pure = true)
    public abstract GasStack getOutput(ItemStack input);

    @Override
    public abstract List<GasStack> getOutputDefinition();
}
