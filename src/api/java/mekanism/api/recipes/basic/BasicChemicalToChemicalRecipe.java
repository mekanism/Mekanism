package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackSlotDisplay;
import net.minecraft.core.TypedInstance;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public abstract class BasicChemicalToChemicalRecipe extends ChemicalToChemicalRecipe {

    private final RecipeType<ChemicalToChemicalRecipe> recipeType;
    protected final ChemicalStackTemplate output;
    private final ChemicalStackIngredient input;

    /// @param input  Input.
    /// @param output Output.
    public BasicChemicalToChemicalRecipe(ChemicalStackIngredient input, ChemicalStackTemplate output, RecipeType<ChemicalToChemicalRecipe> recipeType) {
        this.recipeType = Objects.requireNonNull(recipeType, "Recipe type cannot be null");
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
    }

    @Override
    public final RecipeType<ChemicalToChemicalRecipe> getType() {
        return recipeType;
    }

    @Override
    public ChemicalStackIngredient getInput() {
        return input;
    }

    @Override
    public List<ChemicalStackTemplate> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        return new ChemicalStackSlotDisplay(output);
    }

    @Override
    @Contract(pure = true)
    public ChemicalStackTemplate getOutput(TypedInstance<Chemical> input) {
        return output;
    }

    /// For Serializer usage only. Do not modify the returned stack!
    ///
    /// @return the uncopied output definition
    public ChemicalStackTemplate getOutputRaw() {
        return output;
    }

    @Override
    public boolean equals(@Nullable Object o) {
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