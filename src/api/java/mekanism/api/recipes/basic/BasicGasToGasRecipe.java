package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public abstract class BasicGasToGasRecipe extends GasToGasRecipe {

    protected final GasStack output;
    private final GasStackIngredient input;

    /**
     * @param input  Input.
     * @param output Output.
     */
    public BasicGasToGasRecipe(GasStackIngredient input, GasStack output, RecipeType<GasToGasRecipe> recipeType) {
        super(recipeType);
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public boolean test(GasStack chemicalStack) {
        return input.test(chemicalStack);
    }

    @Override
    public GasStackIngredient getInput() {
        return input;
    }

    @Override
    public List<GasStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    @Contract(value = "_ -> new", pure = true)
    public GasStack getOutput(GasStack input) {
        return output.copy();
    }

    /**
     * For Serializer usage only. Do not modify the returned stack!
     *
     * @return the uncopied output definition
     */
    public GasStack getOutputRaw() {
        return output;
    }
}