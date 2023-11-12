package mekanism.api.recipes;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

/**
 * Input: FluidStack
 * <br>
 * Output: GasStack
 * <br><br>
 * Input: GasStack
 * <br>
 * Output: FluidStack
 *
 * @apiNote Rotary Condensentrators can process this recipe type. Converting from fluid to gas when set to Decondensentrating and converting from gas to fluid when set to
 * Condensentrating.
 */
@NothingNullByDefault
public abstract class RotaryRecipe extends MekanismRecipe {

    /**
     * @return {@code true} if this recipe knows how to convert a gas to a fluid.
     */
    public abstract boolean hasGasToFluid();

    /**
     * @return {@code true} if this recipe knows how to convert a fluid to a gas.
     */
    public abstract boolean hasFluidToGas();

    /**
     * Checks if this recipe can convert fluids to gases, and evaluates this recipe on the given input.
     *
     * @param fluidStack Fluid input.
     *
     * @return {@code true} if the input is valid for this recipe.
     */
    public abstract boolean test(FluidStack fluidStack);

    /**
     * Checks if this recipe can convert gases to fluids, and evaluates this recipe on the given input.
     *
     * @param gasStack Gas input.
     *
     * @return {@code true} if the input is valid for this recipe.
     */
    public abstract boolean test(GasStack gasStack);

    /**
     * Gets the fluid input ingredient.
     *
     * @throws IllegalStateException if {@link #hasFluidToGas()} is {@code false}.
     */
    public abstract FluidStackIngredient getFluidInput();

    /**
     * Gets the gas input ingredient.
     *
     * @throws IllegalStateException if {@link #hasGasToFluid()} is {@code false}.
     */
    public abstract GasStackIngredient getGasInput();

    /**
     * For JEI, gets the gas output representations to display.
     *
     * @return Representation of the gas output, <strong>MUST NOT</strong> be modified.
     *
     * @throws IllegalStateException if {@link #hasFluidToGas()} is {@code false}.
     */
    public abstract List<GasStack> getGasOutputDefinition();

    /**
     * For JEI, gets the fluid output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     *
     * @throws IllegalStateException if {@link #hasGasToFluid()} is {@code false}.
     */
    public abstract List<FluidStack> getFluidOutputDefinition();

    /**
     * Gets a new gas output based on the given input.
     *
     * @param input Specific fluid input.
     *
     * @return New gas output.
     *
     * @throws IllegalStateException if {@link #hasFluidToGas()} is {@code false}.
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public abstract GasStack getGasOutput(FluidStack input);

    /**
     * Gets a new fluid output based on the given input.
     *
     * @param input Specific gas input.
     *
     * @return New fluid output.
     *
     * @throws IllegalStateException if {@link #hasGasToFluid()} is {@code false}.
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public abstract FluidStack getFluidOutput(GasStack input);

    @Override
    public boolean isIncomplete() {
        return (hasFluidToGas() && getFluidInput().hasNoMatchingInstances()) || (hasGasToFluid() && getGasInput().hasNoMatchingInstances());
    }
}
