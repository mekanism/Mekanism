package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.ingredients.PigmentStackIngredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public class BasicPigmentMixingRecipe extends PigmentMixingRecipe implements IBasicChemicalOutput<Pigment, PigmentStack> {

    protected final PigmentStackIngredient leftInput;
    protected final PigmentStackIngredient rightInput;
    protected final PigmentStack output;

    /**
     * @param leftInput  Left input.
     * @param rightInput Right input.
     * @param output     Output.
     *
     * @apiNote The order of the inputs does not matter.
     */
    public BasicPigmentMixingRecipe(PigmentStackIngredient leftInput, PigmentStackIngredient rightInput, PigmentStack output) {
        this.leftInput = Objects.requireNonNull(leftInput, "Left input cannot be null.");
        this.rightInput = Objects.requireNonNull(rightInput, "Right input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public boolean test(PigmentStack input1, PigmentStack input2) {
        return (leftInput.test(input1) && rightInput.test(input2)) || (rightInput.test(input1) && leftInput.test(input2));
    }

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public PigmentStack getOutput(PigmentStack input1, PigmentStack input2) {
        return output.copy();
    }

    @Override
    public PigmentStackIngredient getLeftInput() {
        return leftInput;
    }

    @Override
    public PigmentStackIngredient getRightInput() {
        return rightInput;
    }

    @Override
    public List<PigmentStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public PigmentStack getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicPigmentMixingRecipe> getSerializer() {
        return MekanismRecipeSerializers.PIGMENT_MIXING.get();
    }
}