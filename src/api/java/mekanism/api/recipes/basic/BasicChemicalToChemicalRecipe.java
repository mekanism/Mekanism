package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public abstract class BasicChemicalToChemicalRecipe extends ChemicalToChemicalRecipe {

    private final RecipeType<ChemicalToChemicalRecipe> recipeType;
    protected final ChemicalStack output;
    private final ChemicalStackIngredient input;

    /**
     * @param input  Input.
     * @param output Output.
     */
    public BasicChemicalToChemicalRecipe(ChemicalStackIngredient input, ChemicalStack output, RecipeType<ChemicalToChemicalRecipe> recipeType) {
        this.recipeType = Objects.requireNonNull(recipeType, "Recipe type cannot be null");
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public final RecipeType<ChemicalToChemicalRecipe> getType() {
        return recipeType;
    }

    @Override
    public boolean test(ChemicalStack chemicalStack) {
        return input.test(chemicalStack);
    }

    @Override
    public ChemicalStackIngredient getInput() {
        return input;
    }

    @Override
    public List<ChemicalStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    @Contract(value = "_ -> new", pure = true)
    public ChemicalStack getOutput(ChemicalStack input) {
        return output.copy();
    }

    /**
     * For Serializer usage only. Do not modify the returned stack!
     *
     * @return the uncopied output definition
     */
    public ChemicalStack getOutputRaw() {
        return output;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicChemicalToChemicalRecipe other = (BasicChemicalToChemicalRecipe) o;
        //Note: We don't need to compare the recipe type as that gets covered by the explicit class type check above
        return output.equals(other.output) && input.equals(other.input);
    }

    @Override
    public int hashCode() {
        int result = output.hashCode();
        result = 31 * result + input.hashCode();
        return result;
    }
}