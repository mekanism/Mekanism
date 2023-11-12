package mekanism.api.recipes;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Input: FluidStack
 * <br>
 * Left Output: GasStack
 * <br>
 * Right Output: GasStack
 *
 * @apiNote Electrolytic Separators can process this recipe type.
 */
@NothingNullByDefault
public abstract class ElectrolysisRecipe extends MekanismRecipe implements Predicate<@NotNull FluidStack> {

    /**
     * Gets the input ingredient.
     */
    public abstract FluidStackIngredient getInput();

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<ElectrolysisRecipeOutput> getOutputDefinition();

    @Override
    public abstract boolean test(FluidStack fluidStack);

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
    @Contract(value = "_ -> new", pure = true)
    public abstract ElectrolysisRecipeOutput getOutput(FluidStack input);

    /**
     * Gets the multiplier to the energy cost in relation to the configured hydrogen separating energy cost.
     */
    public abstract FloatingLong getEnergyMultiplier();

    @Override
    public boolean isIncomplete() {
        return getInput().hasNoMatchingInstances();
    }

    @Override
    public final RecipeType<ElectrolysisRecipe> getType() {
        return MekanismRecipeTypes.TYPE_SEPARATING.get();
    }

    public record ElectrolysisRecipeOutput(@NotNull GasStack left, @NotNull GasStack right) {

        public ElectrolysisRecipeOutput {
            Objects.requireNonNull(left, "Left output cannot be null.");
            Objects.requireNonNull(right, "Right output cannot be null.");
            if (left.isEmpty()) {
                throw new IllegalArgumentException("Left output cannot be empty.");
            } else if (right.isEmpty()) {
                throw new IllegalArgumentException("Right output cannot be empty.");
            }
        }
    }
}
