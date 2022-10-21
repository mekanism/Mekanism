package mekanism.api.recipes.chemical;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for defining chemical to chemical recipes.
 * <br>
 * Input: Chemical
 * <br>
 * Output: ChemicalStack of the same chemical type as the input chemical
 *
 * @param <INGREDIENT> Input Ingredient type
 */
@NothingNullByDefault
public abstract class ChemicalToChemicalRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>> extends MekanismRecipe implements Predicate<@NotNull STACK> {

    private final INGREDIENT input;
    protected final STACK output;

    /**
     * @param id     Recipe name.
     * @param input  Input.
     * @param output Output.
     */
    public ChemicalToChemicalRecipe(ResourceLocation id, INGREDIENT input, STACK output) {
        super(id);
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        //noinspection unchecked
        this.output = (STACK) output.copy();
    }

    @Override
    public boolean test(STACK chemicalStack) {
        return input.test(chemicalStack);
    }

    /**
     * Gets the input ingredient.
     */
    public INGREDIENT getInput() {
        return input;
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public List<STACK> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    /**
     * Gets a new output based on the given input.
     *
     * @param input Specific input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @SuppressWarnings("unchecked")
    @Contract(value = "_ -> new", pure = true)
    public STACK getOutput(STACK input) {
        return (STACK) output.copy();
    }

    @Override
    public boolean isIncomplete() {
        return input.hasNoMatchingInstances();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        input.write(buffer);
        output.writeToPacket(buffer);
    }
}