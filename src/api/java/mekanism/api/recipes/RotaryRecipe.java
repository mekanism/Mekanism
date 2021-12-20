package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
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

    //TODO - 1.18: Evaluate making ingredients not able to even be built from empty stacks and just switch these to being null here
    private static final GasStackIngredient EMPTY_GAS_INPUT = GasStackIngredient.from(GasStack.EMPTY);
    private static final FluidStackIngredient EMPTY_FLUID_INPUT = FluidStackIngredient.from(FluidStack.EMPTY);
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
        this.gasInput = EMPTY_GAS_INPUT;
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
        this.fluidInput = EMPTY_FLUID_INPUT;
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
     * Gets the fluid input ingredient. This method assumes {@link #hasFluidToGas()} is {@code true}.
     */
    public FluidStackIngredient getFluidInput() {
        return fluidInput;
    }

    /**
     * Gets the gas input ingredient. This method assumes {@link #hasGasToFluid()} is {@code true}.
     */
    public GasStackIngredient getGasInput() {
        return gasInput;
    }

    /**
     * @deprecated Use {@link #getGasOutputDefinition()}.
     */
    @Deprecated//TODO - 1.18: Remove this
    public GasStack getGasOutputRepresentation() {
        return gasOutput;
    }

    /**
     * For JEI, gets the gas output representations to display. This method assumes {@link #hasFluidToGas()} is {@code true}.
     *
     * @return Representation of the gas output, <strong>MUST NOT</strong> be modified.
     */
    public List<GasStack> getGasOutputDefinition() {
        return Collections.singletonList(gasOutput);
    }

    /**
     * @deprecated Use {@link #getFluidOutputDefinition()}.
     */
    @Deprecated//TODO - 1.18: Remove this
    public FluidStack getFluidOutputRepresentation() {
        return fluidOutput;
    }

    /**
     * For JEI, gets the fluid output representations to display. This method assumes {@link #hasGasToFluid()} is {@code true}.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public List<FluidStack> getFluidOutputDefinition() {
        return Collections.singletonList(fluidOutput);
    }

    /**
     * Gets a new gas output based on the given input. This method assumes {@link #hasFluidToGas()} is {@code true}.
     *
     * @param input Specific fluid input.
     *
     * @return New gas output.
     *
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public GasStack getGasOutput(FluidStack input) {
        return gasOutput.copy();
    }

    /**
     * Gets a new fluid output based on the given input. This method assumes {@link #hasGasToFluid()} is {@code true}.
     *
     * @param input Specific gas input.
     *
     * @return New fluid output.
     *
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public FluidStack getFluidOutput(GasStack input) {
        return fluidOutput.copy();
    }

    @Override
    public void write(PacketBuffer buffer) {
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