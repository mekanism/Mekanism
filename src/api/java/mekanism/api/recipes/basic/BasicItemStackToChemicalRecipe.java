package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public abstract class BasicItemStackToChemicalRecipe extends ItemStackToChemicalRecipe implements IBasicChemicalOutput {

    protected final ItemStackIngredient input;
    protected final ChemicalStackTemplate output;
    private final RecipeType<ItemStackToChemicalRecipe> recipeType;

    /**
     * @param input  Input.
     * @param output Output.
     */
    public BasicItemStackToChemicalRecipe(ItemStackIngredient input, ChemicalStackTemplate output, RecipeType<ItemStackToChemicalRecipe> recipeType) {
        this.recipeType = Objects.requireNonNull(recipeType, "Recipe type cannot be null");
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
    }

    @Override
    public ItemStackIngredient getInput() {
        return input;
    }

    @Override
    @Contract(value = "_ -> new", pure = true)
    public ChemicalStackTemplate getOutput(TypedInstance<Item> input) {
        return output;
    }

    @Override
    public List<ChemicalStackTemplate> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public ChemicalStackTemplate getOutputRaw() {
        return output;
    }

    @Override
    public final RecipeType<ItemStackToChemicalRecipe> getType() {
        return recipeType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicItemStackToChemicalRecipe other = (BasicItemStackToChemicalRecipe) o;
        //Note: We don't need to compare the recipe type as that gets covered by the explicit class type check above
        return input.equals(other.input) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int result = input.hashCode();
        result = 31 * result + output.hashCode();
        return result;
    }
}