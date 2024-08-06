package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public class BasicPigmentMixingRecipe extends PigmentMixingRecipe implements IBasicChemicalOutput {

    protected final ChemicalStackIngredient leftInput;
    protected final ChemicalStackIngredient rightInput;
    protected final ChemicalStack output;

    /**
     * @param leftInput  Left input.
     * @param rightInput Right input.
     * @param output     Output.
     *
     * @apiNote The order of the inputs does not matter.
     */
    public BasicPigmentMixingRecipe(ChemicalStackIngredient leftInput, ChemicalStackIngredient rightInput, ChemicalStack output) {
        this.leftInput = Objects.requireNonNull(leftInput, "Left input cannot be null.");
        this.rightInput = Objects.requireNonNull(rightInput, "Right input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public boolean test(ChemicalStack input1, ChemicalStack input2) {
        return (leftInput.test(input1) && rightInput.test(input2)) || (rightInput.test(input1) && leftInput.test(input2));
    }

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public ChemicalStack getOutput(ChemicalStack input1, ChemicalStack input2) {
        return output.copy();
    }

    @Override
    public ChemicalStackIngredient getLeftInput() {
        return leftInput;
    }

    @Override
    public ChemicalStackIngredient getRightInput() {
        return rightInput;
    }

    @Override
    public List<ChemicalStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public ChemicalStack getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicPigmentMixingRecipe> getSerializer() {
        return MekanismRecipeSerializers.PIGMENT_MIXING.get();
    }
}