package mekanism.api.recipes.chemical;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;

/**
 * Base class for defining chemical chemical to chemical recipes.
 * <br>
 * Input: Two chemicals of the same chemical type. The order of them does not matter.
 * <br>
 * Output: ChemicalStack of the same chemical type as the input chemicals
 *
 * @param <INGREDIENT> Input Ingredient type
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ChemicalChemicalToChemicalRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>> extends MekanismRecipe implements BiPredicate<@NonNull STACK, @NonNull STACK> {

    private final INGREDIENT leftInput;
    private final INGREDIENT rightInput;
    protected final STACK output;

    /**
     * @param id         Recipe name.
     * @param leftInput  Left input.
     * @param rightInput Right input.
     * @param output     Output.
     *
     * @apiNote The order of the inputs does not matter.
     */
    public ChemicalChemicalToChemicalRecipe(ResourceLocation id, INGREDIENT leftInput, INGREDIENT rightInput, STACK output) {
        super(id);
        this.leftInput = Objects.requireNonNull(leftInput, "Left input cannot be null.");
        this.rightInput = Objects.requireNonNull(rightInput, "Right input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        //noinspection unchecked
        this.output = (STACK) output.copy();
    }

    @Override
    public boolean test(STACK input1, STACK input2) {
        return (leftInput.test(input1) && rightInput.test(input2)) || (rightInput.test(input1) && leftInput.test(input2));
    }

    /**
     * Gets a new output based on the given inputs, the order of these inputs which one is {@code input1} and which one is {@code input2} does not matter.
     *
     * @param input1 Specific "left" input.
     * @param input2 Specific "right" input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the inputs, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in inputs should <strong>NOT</strong> be modified.
     */
    @SuppressWarnings("unchecked")
    @Contract(value = "_, _ -> new", pure = true)
    public STACK getOutput(STACK input1, STACK input2) {
        return (STACK) output.copy();
    }

    /**
     * Gets the left input ingredient.
     */
    public INGREDIENT getLeftInput() {
        return leftInput;
    }

    /**
     * Gets the right input ingredient.
     */
    public INGREDIENT getRightInput() {
        return rightInput;
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public List<STACK> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public boolean isIncomplete() {
        return leftInput.hasNoMatchingInstances() || rightInput.hasNoMatchingInstances();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        leftInput.write(buffer);
        rightInput.write(buffer);
        output.writeToPacket(buffer);
    }
}