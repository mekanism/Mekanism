package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
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
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class RotaryRecipe extends MekanismRecipe {

    private final GasStackIngredient gasInput;
    private final FluidStackIngredient fluidInput;
    private final FluidStack fluidOutput;
    private final GasStack gasOutput;
    private final boolean hasGasToFluid;
    private final boolean hasFluidToGas;

    /**
     * Rotary recipe that converts a fluid into a gas.
     *
     * @param id         Recipe name.
     * @param fluidInput Fluid input.
     * @param gasOutput  Gas output.
     *
     * @apiNote It is recommended to use {@link #RotaryRecipe(ResourceLocation, FluidStackIngredient, GasStackIngredient, GasStack, FluidStack)} over this constructor in
     * combination with {@link #RotaryRecipe(ResourceLocation, GasStackIngredient, FluidStack)} and making two separate recipes if the conversion will be possible in both
     * directions.
     */
    public RotaryRecipe(ResourceLocation id, FluidStackIngredient fluidInput, GasStack gasOutput) {
        super(id);
        this.fluidInput = Objects.requireNonNull(fluidInput, "Fluid input cannot be null.");
        Objects.requireNonNull(gasOutput, "Gas output cannot be null.");
        if (gasOutput.isEmpty()) {
            throw new IllegalArgumentException("Gas output cannot be empty.");
        }
        this.gasOutput = gasOutput.copy();
        //noinspection ConstantConditions we safety check it being null behind require hasGasToFluid
        this.gasInput = null;
        this.fluidOutput = FluidStack.EMPTY;
        this.hasGasToFluid = false;
        this.hasFluidToGas = true;
    }

    /**
     * Rotary recipe that converts a gas into a fluid.
     *
     * @param id          Recipe name.
     * @param gasInput    Gas input.
     * @param fluidOutput Fluid output.
     *
     * @apiNote It is recommended to use {@link #RotaryRecipe(ResourceLocation, FluidStackIngredient, GasStackIngredient, GasStack, FluidStack)} over this constructor in
     * combination with {@link #RotaryRecipe(ResourceLocation, FluidStackIngredient, GasStack)} and making two separate recipes if the conversion will be possible in both
     * directions.
     */
    public RotaryRecipe(ResourceLocation id, GasStackIngredient gasInput, FluidStack fluidOutput) {
        super(id);
        this.gasInput = Objects.requireNonNull(gasInput, "Gas input cannot be null.");
        Objects.requireNonNull(fluidOutput, "Fluid output cannot be null.");
        if (fluidOutput.isEmpty()) {
            throw new IllegalArgumentException("Fluid output cannot be empty.");
        }
        this.fluidOutput = fluidOutput.copy();
        //noinspection ConstantConditions we safety check it being null behind require hasFluidToGas
        this.fluidInput = null;
        this.gasOutput = GasStack.EMPTY;
        this.hasGasToFluid = true;
        this.hasFluidToGas = false;
    }

    /**
     * Rotary recipe that is capable of converting a fluid into a gas and a gas into a fluid.
     *
     * @param id          Recipe name.
     * @param fluidInput  Fluid input.
     * @param gasInput    Gas input.
     * @param gasOutput   Gas output.
     * @param fluidOutput Fluid output.
     *
     * @apiNote It is recommended to use this constructor over using {@link #RotaryRecipe(ResourceLocation, FluidStackIngredient, GasStack)} and {@link
     * #RotaryRecipe(ResourceLocation, GasStackIngredient, FluidStack)} in combination and creating two recipes if the conversion will be possible in both directions.
     */
    public RotaryRecipe(ResourceLocation id, FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput) {
        super(id);
        this.gasInput = Objects.requireNonNull(gasInput, "Gas input cannot be null.");
        this.fluidInput = Objects.requireNonNull(fluidInput, "Fluid input cannot be null.");
        Objects.requireNonNull(gasOutput, "Gas output cannot be null.");
        Objects.requireNonNull(fluidOutput, "Fluid output cannot be null.");
        if (gasOutput.isEmpty()) {
            throw new IllegalArgumentException("Gas output cannot be empty.");
        } else if (fluidOutput.isEmpty()) {
            throw new IllegalArgumentException("Fluid output cannot be empty.");
        }
        this.gasOutput = gasOutput.copy();
        this.fluidOutput = fluidOutput.copy();
        this.hasGasToFluid = true;
        this.hasFluidToGas = true;
    }

    /**
     * @return {@code true} if this recipe knows how to convert a gas to a fluid.
     */
    public boolean hasGasToFluid() {
        return hasGasToFluid;
    }

    /**
     * @return {@code true} if this recipe knows how to convert a fluid to a gas.
     */
    public boolean hasFluidToGas() {
        return hasFluidToGas;
    }

    /**
     * @throws IllegalStateException if {@link #hasGasToFluid()} is {@code false}.
     */
    protected void assertHasGasToFluid() {
        if (!hasGasToFluid()) {
            throw new IllegalStateException("This recipe has no gas to fluid conversion.");
        }
    }

    /**
     * @throws IllegalStateException if {@link #hasFluidToGas()} is {@code false}.
     */
    protected void assertHasFluidToGas() {
        if (!hasFluidToGas()) {
            throw new IllegalStateException("This recipe has no fluid to gas conversion.");
        }
    }

    /**
     * Checks if this recipe can convert fluids to gases, and evaluates this recipe on the given input.
     *
     * @param fluidStack Fluid input.
     *
     * @return {@code true} if the input is valid for this recipe.
     */
    public boolean test(FluidStack fluidStack) {
        return hasFluidToGas() && fluidInput.test(fluidStack);
    }

    /**
     * Checks if this recipe can convert gases to fluids, and evaluates this recipe on the given input.
     *
     * @param gasStack Gas input.
     *
     * @return {@code true} if the input is valid for this recipe.
     */
    public boolean test(GasStack gasStack) {
        return hasGasToFluid() && gasInput.test(gasStack);
    }

    /**
     * Gets the fluid input ingredient.
     *
     * @throws IllegalStateException if {@link #hasFluidToGas()} is {@code false}.
     */
    public FluidStackIngredient getFluidInput() {
        assertHasFluidToGas();
        return fluidInput;
    }

    /**
     * Gets the gas input ingredient.
     *
     * @throws IllegalStateException if {@link #hasGasToFluid()} is {@code false}.
     */
    public GasStackIngredient getGasInput() {
        assertHasGasToFluid();
        return gasInput;
    }

    /**
     * For JEI, gets the gas output representations to display.
     *
     * @return Representation of the gas output, <strong>MUST NOT</strong> be modified.
     *
     * @throws IllegalStateException if {@link #hasFluidToGas()} is {@code false}.
     */
    public List<GasStack> getGasOutputDefinition() {
        assertHasFluidToGas();
        return Collections.singletonList(gasOutput);
    }

    /**
     * For JEI, gets the fluid output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     *
     * @throws IllegalStateException if {@link #hasGasToFluid()} is {@code false}.
     */
    public List<FluidStack> getFluidOutputDefinition() {
        assertHasGasToFluid();
        return Collections.singletonList(fluidOutput);
    }

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
    public GasStack getGasOutput(FluidStack input) {
        assertHasFluidToGas();
        return gasOutput.copy();
    }

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
    public FluidStack getFluidOutput(GasStack input) {
        assertHasGasToFluid();
        return fluidOutput.copy();
    }

    @Override
    public boolean isIncomplete() {
        return (hasFluidToGas && fluidInput.hasNoMatchingInstances()) || (hasGasToFluid && gasInput.hasNoMatchingInstances());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(hasFluidToGas);
        if (hasFluidToGas) {
            fluidInput.write(buffer);
            gasOutput.writeToPacket(buffer);
        }
        buffer.writeBoolean(hasGasToFluid);
        if (hasGasToFluid) {
            gasInput.write(buffer);
            fluidOutput.writeToPacket(buffer);
        }
    }
}