package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BasicRotaryRecipe extends RotaryRecipe {

    protected final ChemicalStackIngredient gasInput;
    protected final FluidStackIngredient fluidInput;
    protected final FluidStack fluidOutput;
    protected final ChemicalStack gasOutput;
    protected final boolean hasGasToFluid;
    protected final boolean hasFluidToGas;

    /**
     * Rotary recipe that converts a fluid into a gas.
     *
     * @param fluidInput Fluid input.
     * @param gasOutput  Gas output.
     *
     * @apiNote It is recommended to use {@link #BasicRotaryRecipe(FluidStackIngredient, ChemicalStackIngredient, ChemicalStack, FluidStack)} over this constructor in combination
     * with {@link #BasicRotaryRecipe(ChemicalStackIngredient, FluidStack)} and making two separate recipes if the conversion will be possible in both directions.
     */
    public BasicRotaryRecipe(FluidStackIngredient fluidInput, ChemicalStack gasOutput) {
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
     * @param gasInput    Gas input.
     * @param fluidOutput Fluid output.
     *
     * @apiNote It is recommended to use {@link #BasicRotaryRecipe(FluidStackIngredient, ChemicalStackIngredient, ChemicalStack, FluidStack)} over this constructor in combination
     * with {@link #BasicRotaryRecipe(FluidStackIngredient, ChemicalStack)} and making two separate recipes if the conversion will be possible in both directions.
     */
    public BasicRotaryRecipe(ChemicalStackIngredient gasInput, FluidStack fluidOutput) {
        this.gasInput = Objects.requireNonNull(gasInput, "Gas input cannot be null.");
        Objects.requireNonNull(fluidOutput, "Fluid output cannot be null.");
        if (fluidOutput.isEmpty()) {
            throw new IllegalArgumentException("Fluid output cannot be empty.");
        }
        this.fluidOutput = fluidOutput.copy();
        //noinspection ConstantConditions we safety check it being null behind require hasFluidToGas
        this.fluidInput = null;
        this.gasOutput = ChemicalStack.EMPTY;
        this.hasGasToFluid = true;
        this.hasFluidToGas = false;
    }

    /**
     * Rotary recipe that is capable of converting a fluid into a gas and a gas into a fluid.
     *
     * @param fluidInput  Fluid input.
     * @param gasInput    Gas input.
     * @param gasOutput   Gas output.
     * @param fluidOutput Fluid output.
     *
     * @apiNote It is recommended to use this constructor over using {@link #BasicRotaryRecipe(FluidStackIngredient, ChemicalStack)} and
     * {@link #BasicRotaryRecipe(ChemicalStackIngredient, FluidStack)} in combination and creating two recipes if the conversion will be possible in both directions.
     */
    public BasicRotaryRecipe(FluidStackIngredient fluidInput, ChemicalStackIngredient gasInput, ChemicalStack gasOutput, FluidStack fluidOutput) {
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

    @Override
    public boolean hasGasToFluid() {
        return hasGasToFluid;
    }

    @Override
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

    @Override
    public boolean test(FluidStack fluidStack) {
        return hasFluidToGas() && fluidInput.test(fluidStack);
    }

    @Override
    public boolean test(ChemicalStack gasStack) {
        return hasGasToFluid() && gasInput.test(gasStack);
    }

    @Override
    public FluidStackIngredient getFluidInput() {
        assertHasFluidToGas();
        return fluidInput;
    }

    @Override
    public ChemicalStackIngredient getGasInput() {
        assertHasGasToFluid();
        return gasInput;
    }

    @Override
    public List<ChemicalStack> getGasOutputDefinition() {
        assertHasFluidToGas();
        return Collections.singletonList(gasOutput);
    }

    @Override
    public List<FluidStack> getFluidOutputDefinition() {
        assertHasGasToFluid();
        return Collections.singletonList(fluidOutput);
    }

    @Override
    @Contract(value = "_ -> new", pure = true)
    public ChemicalStack getGasOutput(FluidStack input) {
        assertHasFluidToGas();
        return gasOutput.copy();
    }

    @Override
    @Contract(value = "_ -> new", pure = true)
    public FluidStack getFluidOutput(ChemicalStack input) {
        assertHasGasToFluid();
        return fluidOutput.copy();
    }

    /**
     * For Serializer use. DO NOT MODIFY RETURN VALUE.
     *
     * @return the uncopied basic input, {@code null} if the recipe doesn't support gas to fluid recipes.
     */
    @Nullable
    public ChemicalStackIngredient getGasInputRaw() {
        return gasInput;
    }

    /**
     * For Serializer use. DO NOT MODIFY RETURN VALUE.
     *
     * @return the uncopied basic output
     */
    public ChemicalStack getGasOutputRaw() {
        return this.gasOutput;
    }

    /**
     * For Serializer use. DO NOT MODIFY RETURN VALUE.
     *
     * @return the uncopied basic input, {@code null} if the recipe doesn't support fluid to gas recipes.
     */
    @Nullable
    public FluidStackIngredient getFluidInputRaw() {
        return fluidInput;
    }

    /**
     * For Serializer use. DO NOT MODIFY RETURN VALUE.
     *
     * @return the uncopied basic output
     */
    public FluidStack getFluidOutputRaw() {
        return this.fluidOutput;
    }

    @Override
    public RecipeSerializer<BasicRotaryRecipe> getSerializer() {
        return MekanismRecipeSerializers.ROTARY.get();
    }
}