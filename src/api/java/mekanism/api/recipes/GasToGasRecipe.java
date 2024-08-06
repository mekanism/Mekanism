package mekanism.api.recipes;

import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Contract;

/**
 * Input: Gas
 * <br>
 * Output: GasStack
 *
 * @apiNote There are currently two types of Gas to Gas recipe types:
 * <ul>
 *     <li>Activating: Can be processed in a Solar Neutron Activator.</li>
 *     <li>Centrifuging: Can be processed in an Isotopic Centrifuge.</li>
 * </ul>
 */
@NothingNullByDefault
@Deprecated
public abstract class GasToGasRecipe extends ChemicalToChemicalRecipe {

    private final RecipeType<GasToGasRecipe> recipeType;

    protected GasToGasRecipe(RecipeType<GasToGasRecipe> recipeType) {
        this.recipeType = Objects.requireNonNull(recipeType, "Recipe type cannot be null");
    }

    @Override
    public abstract boolean test(ChemicalStack chemicalStack);

    @Override
    public abstract ChemicalStackIngredient getInput();

    @Override
    public abstract List<ChemicalStack> getOutputDefinition();

    @Override
    @Contract(value = "_ -> new", pure = true)
    public abstract ChemicalStack getOutput(ChemicalStack input);

    @Override
    public RecipeType<GasToGasRecipe> getType() {
        return recipeType;
    }
}
