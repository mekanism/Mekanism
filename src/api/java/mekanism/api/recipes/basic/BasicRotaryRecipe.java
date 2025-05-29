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

    protected final ChemicalStackIngredient chemicalInput;
    protected final FluidStackIngredient fluidInput;
    protected final FluidStack fluidOutput;
    protected final ChemicalStack chemicalOutput;
    protected final boolean hasChemicalToFluid;
    protected final boolean hasFluidToChemical;

    /**
     * Rotary recipe that converts a fluid into a chemical.
     *
     * @param fluidInput     Fluid input.
     * @param chemicalOutput Chemical output.
     *
     * @apiNote It is recommended to use {@link #BasicRotaryRecipe(FluidStackIngredient, ChemicalStackIngredient, ChemicalStack, FluidStack)} over this constructor in
     * combination with {@link #BasicRotaryRecipe(ChemicalStackIngredient, FluidStack)} and making two separate recipes if the conversion will be possible in both
     * directions.
     */
    public BasicRotaryRecipe(FluidStackIngredient fluidInput, ChemicalStack chemicalOutput) {
        this.fluidInput = Objects.requireNonNull(fluidInput, "Fluid input cannot be null.");
        Objects.requireNonNull(chemicalOutput, "Chemical output cannot be null.");
        if (chemicalOutput.isEmpty()) {
            throw new IllegalArgumentException("Chemical output cannot be empty.");
        }
        this.chemicalOutput = chemicalOutput.copy();
        //noinspection ConstantConditions we safety check it being null behind require hasChemicalToFluid
        this.chemicalInput = null;
        this.fluidOutput = FluidStack.EMPTY;
        this.hasChemicalToFluid = false;
        this.hasFluidToChemical = true;
    }

    /**
     * Rotary recipe that converts a chemical into a fluid.
     *
     * @param chemicalInput Chemical input.
     * @param fluidOutput   Fluid output.
     *
     * @apiNote It is recommended to use {@link #BasicRotaryRecipe(FluidStackIngredient, ChemicalStackIngredient, ChemicalStack, FluidStack)} over this constructor in
     * combination with {@link #BasicRotaryRecipe(FluidStackIngredient, ChemicalStack)} and making two separate recipes if the conversion will be possible in both
     * directions.
     */
    public BasicRotaryRecipe(ChemicalStackIngredient chemicalInput, FluidStack fluidOutput) {
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        Objects.requireNonNull(fluidOutput, "Fluid output cannot be null.");
        if (fluidOutput.isEmpty()) {
            throw new IllegalArgumentException("Fluid output cannot be empty.");
        }
        this.fluidOutput = fluidOutput.copy();
        //noinspection ConstantConditions we safety check it being null behind require hasFluidToChemical
        this.fluidInput = null;
        this.chemicalOutput = ChemicalStack.EMPTY;
        this.hasChemicalToFluid = true;
        this.hasFluidToChemical = false;
    }

    /**
     * Rotary recipe that is capable of converting a fluid into a chemical and a chemical into a fluid.
     *
     * @param fluidInput     Fluid input.
     * @param chemicalInput  Chemical input.
     * @param chemicalOutput Chemical output.
     * @param fluidOutput    Fluid output.
     *
     * @apiNote It is recommended to use this constructor over using {@link #BasicRotaryRecipe(FluidStackIngredient, ChemicalStack)} and
     * {@link #BasicRotaryRecipe(ChemicalStackIngredient, FluidStack)} in combination and creating two recipes if the conversion will be possible in both directions.
     */
    public BasicRotaryRecipe(FluidStackIngredient fluidInput, ChemicalStackIngredient chemicalInput, ChemicalStack chemicalOutput, FluidStack fluidOutput) {
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        this.fluidInput = Objects.requireNonNull(fluidInput, "Fluid input cannot be null.");
        Objects.requireNonNull(chemicalOutput, "Chemical output cannot be null.");
        Objects.requireNonNull(fluidOutput, "Fluid output cannot be null.");
        if (chemicalOutput.isEmpty()) {
            throw new IllegalArgumentException("Chemical output cannot be empty.");
        } else if (fluidOutput.isEmpty()) {
            throw new IllegalArgumentException("Fluid output cannot be empty.");
        }
        this.chemicalOutput = chemicalOutput.copy();
        this.fluidOutput = fluidOutput.copy();
        this.hasChemicalToFluid = true;
        this.hasFluidToChemical = true;
    }

    @Override
    public boolean hasChemicalToFluid() {
        return hasChemicalToFluid;
    }

    @Override
    public boolean hasFluidToChemical() {
        return hasFluidToChemical;
    }

    /**
     * @throws IllegalStateException if {@link #hasChemicalToFluid()} is {@code false}.
     */
    protected void assertHasChemicalToFluid() {
        if (!hasChemicalToFluid()) {
            throw new IllegalStateException("This recipe has no chemical to fluid conversion.");
        }
    }

    /**
     * @throws IllegalStateException if {@link #hasFluidToChemical()} is {@code false}.
     */
    protected void assertHasFluidToChemical() {
        if (!hasFluidToChemical()) {
            throw new IllegalStateException("This recipe has no fluid to chemical conversion.");
        }
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return hasFluidToChemical() && fluidInput.test(fluidStack);
    }

    @Override
    public boolean test(ChemicalStack chemicalStack) {
        return hasChemicalToFluid() && chemicalInput.test(chemicalStack);
    }

    @Override
    public FluidStackIngredient getFluidInput() {
        assertHasFluidToChemical();
        return fluidInput;
    }

    @Override
    public ChemicalStackIngredient getChemicalInput() {
        assertHasChemicalToFluid();
        return chemicalInput;
    }

    @Override
    public List<ChemicalStack> getChemicalOutputDefinition() {
        assertHasFluidToChemical();
        return Collections.singletonList(chemicalOutput);
    }

    @Override
    public List<FluidStack> getFluidOutputDefinition() {
        assertHasChemicalToFluid();
        return Collections.singletonList(fluidOutput);
    }

    @Override
    @Contract(value = "_ -> new", pure = true)
    public ChemicalStack getChemicalOutput(FluidStack input) {
        assertHasFluidToChemical();
        return chemicalOutput.copy();
    }

    @Override
    @Contract(value = "_ -> new", pure = true)
    public FluidStack getFluidOutput(ChemicalStack input) {
        assertHasChemicalToFluid();
        return fluidOutput.copy();
    }

    /**
     * For Serializer use. DO NOT MODIFY RETURN VALUE.
     *
     * @return the uncopied basic input, {@code null} if the recipe doesn't support chemical to fluid recipes.
     */
    @Nullable
    public ChemicalStackIngredient getChemicalInputRaw() {
        return chemicalInput;
    }

    /**
     * For Serializer use. DO NOT MODIFY RETURN VALUE.
     *
     * @return the uncopied basic output
     */
    public ChemicalStack getChemicalOutputRaw() {
        return this.chemicalOutput;
    }

    /**
     * For Serializer use. DO NOT MODIFY RETURN VALUE.
     *
     * @return the uncopied basic input, {@code null} if the recipe doesn't support fluid to chemical recipes.
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

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicRotaryRecipe other = (BasicRotaryRecipe) o;
        if (hasChemicalToFluid == other.hasChemicalToFluid && hasFluidToChemical == other.hasFluidToChemical) {
            boolean equal = true;
            if (hasChemicalToFluid) {
                equal = chemicalInput.equals(other.chemicalInput) && FluidStack.matches(fluidOutput, other.fluidOutput);
            }
            if (hasFluidToChemical) {
                equal |= fluidInput.equals(other.fluidInput) && chemicalOutput.equals(other.chemicalOutput);
            }
            return equal;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash;
        if (hasFluidToChemical) {
            hash = 31 * fluidInput.hashCode() + chemicalOutput.hashCode();
        } else {
            hash = 1;
        }
        if (hasChemicalToFluid) {
            hash = 31 * hash + chemicalInput.hashCode();
            hash = 31 * hash + FluidStack.hashFluidAndComponents(fluidOutput);
            hash = 31 * hash + fluidOutput.getAmount();
        }
        return hash;
    }
}