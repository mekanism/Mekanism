package mekanism.api.recipes;

import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
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
public abstract class ItemStackToGasRecipe extends ItemStackToChemicalRecipe {

    private final RecipeType<ItemStackToGasRecipe> recipeType;

    protected ItemStackToGasRecipe(RecipeType<ItemStackToGasRecipe> recipeType) {
        this.recipeType = Objects.requireNonNull(recipeType, "Recipe type cannot be null");
    }

    @Override
    public abstract boolean test(ItemStack itemStack);

    @Override
    public abstract ItemStackIngredient getInput();

    @Override
    @Contract(value = "_ -> new", pure = true)
    public abstract ChemicalStack getOutput(ItemStack input);

    @Override
    public abstract List<ChemicalStack> getOutputDefinition();

    @Override
    public final RecipeType<ItemStackToGasRecipe> getType() {
        return recipeType;
    }
}
