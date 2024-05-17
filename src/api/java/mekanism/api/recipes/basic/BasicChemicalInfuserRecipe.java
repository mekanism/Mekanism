package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public class BasicChemicalInfuserRecipe extends ChemicalInfuserRecipe implements IBasicChemicalOutput<Gas, GasStack> {

    protected final GasStackIngredient leftInput;
    protected final GasStackIngredient rightInput;
    protected final GasStack output;

    /**
     * @param leftInput  Left input.
     * @param rightInput Right input.
     * @param output     Output.
     *
     * @apiNote The order of the inputs does not matter.
     */
    public BasicChemicalInfuserRecipe(GasStackIngredient leftInput, GasStackIngredient rightInput, GasStack output) {
        this.leftInput = Objects.requireNonNull(leftInput, "Left input cannot be null.");
        this.rightInput = Objects.requireNonNull(rightInput, "Right input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public boolean test(GasStack input1, GasStack input2) {
        return (leftInput.test(input1) && rightInput.test(input2)) || (rightInput.test(input1) && leftInput.test(input2));
    }

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public GasStack getOutput(GasStack input1, GasStack input2) {
        return output.copy();
    }

    @Override
    public GasStackIngredient getLeftInput() {
        return leftInput;
    }

    @Override
    public GasStackIngredient getRightInput() {
        return rightInput;
    }

    @Override
    public List<GasStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public GasStack getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicChemicalInfuserRecipe> getSerializer() {
        return MekanismRecipeSerializers.CHEMICAL_INFUSING.get();
    }
}