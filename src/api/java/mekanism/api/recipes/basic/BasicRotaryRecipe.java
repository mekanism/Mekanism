package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public class BasicRotaryRecipe extends RotaryRecipe {

    protected final ChemicalStackIngredient chemicalInput;
    protected final FluidStackIngredient fluidInput;
    @Nullable
    private final FluidStackTemplate fluidOutput;
    @Nullable
    private final ChemicalStackTemplate chemicalOutput;

    /**
     * Rotary recipe that converts a fluid into a chemical.
     *
     * @param fluidInput     Fluid input.
     * @param chemicalOutput Chemical output.
     *
     * @apiNote It is recommended to use {@link #BasicRotaryRecipe(FluidStackIngredient, ChemicalStackIngredient, ChemicalStackTemplate, FluidStackTemplate)} over this constructor in
     * combination with {@link #BasicRotaryRecipe(ChemicalStackIngredient, FluidStackTemplate)} and making two separate recipes if the conversion will be possible in both
     * directions.
     */
    public BasicRotaryRecipe(FluidStackIngredient fluidInput, ChemicalStackTemplate chemicalOutput) {
        this.fluidInput = Objects.requireNonNull(fluidInput, "Fluid input cannot be null.");
        this.chemicalOutput = Objects.requireNonNull(chemicalOutput, "Chemical output cannot be null.");
        //noinspection ConstantConditions we safety check it being null behind require hasChemicalToFluid
        this.chemicalInput = null;
        this.fluidOutput = null;
    }

    /**
     * Rotary recipe that converts a chemical into a fluid.
     *
     * @param chemicalInput Chemical input.
     * @param fluidOutput   Fluid output.
     *
     * @apiNote It is recommended to use {@link #BasicRotaryRecipe(FluidStackIngredient, ChemicalStackIngredient, ChemicalStackTemplate, FluidStackTemplate)} over this constructor in
     * combination with {@link #BasicRotaryRecipe(FluidStackIngredient, ChemicalStackTemplate)} and making two separate recipes if the conversion will be possible in both
     * directions.
     */
    public BasicRotaryRecipe(ChemicalStackIngredient chemicalInput, FluidStackTemplate fluidOutput) {
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        this.fluidOutput = Objects.requireNonNull(fluidOutput, "Fluid output cannot be null.");
        //noinspection ConstantConditions we safety check it being null behind require hasFluidToChemical
        this.fluidInput = null;
        this.chemicalOutput = null;
    }

    /**
     * Rotary recipe that is capable of converting a fluid into a chemical and a chemical into a fluid.
     *
     * @param fluidInput     Fluid input.
     * @param chemicalInput  Chemical input.
     * @param chemicalOutput Chemical output.
     * @param fluidOutput    Fluid output.
     *
     * @apiNote It is recommended to use this constructor over using {@link #BasicRotaryRecipe(FluidStackIngredient, ChemicalStackTemplate)} and
     * {@link #BasicRotaryRecipe(ChemicalStackIngredient, FluidStackTemplate)} in combination and creating two recipes if the conversion will be possible in both directions.
     */
    public BasicRotaryRecipe(FluidStackIngredient fluidInput, ChemicalStackIngredient chemicalInput, ChemicalStackTemplate chemicalOutput, FluidStackTemplate fluidOutput) {
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        this.fluidInput = Objects.requireNonNull(fluidInput, "Fluid input cannot be null.");
        this.chemicalOutput = Objects.requireNonNull(chemicalOutput, "Chemical output cannot be null.");
        this.fluidOutput = Objects.requireNonNull(fluidOutput, "Fluid output cannot be null.");
    }

    @Override
    public final boolean hasChemicalToFluid() {
        return this.fluidOutput != null;
    }

    @Override
    public final boolean hasFluidToChemical() {
        return this.chemicalOutput != null;
    }

    /**
     * @throws IllegalStateException if {@link #hasChemicalToFluid()} is {@code false}.
     */
    private void assertHasChemicalToFluid() {
        if (!hasChemicalToFluid()) {
            throw new IllegalStateException("This recipe has no chemical to fluid conversion.");
        }
    }

    /**
     * @throws IllegalStateException if {@link #hasFluidToChemical()} is {@code false}.
     */
    private void assertHasFluidToChemical() {
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
    public List<ChemicalStackTemplate> getChemicalOutputDefinition() {
        assertHasFluidToChemical();
        return Collections.singletonList(chemicalOutput);
    }

    @Override
    public List<FluidStackTemplate> getFluidOutputDefinition() {
        assertHasChemicalToFluid();
        return Collections.singletonList(fluidOutput);
    }

    @Override
    @Contract(value = "_ -> new", pure = true)
    public ChemicalStackTemplate getChemicalOutput(FluidStack input) {
        assertHasFluidToChemical();
        return chemicalOutput;
    }

    @Override
    @Contract(pure = true)
    public FluidStackTemplate getFluidOutput(ChemicalStack input) {
        assertHasChemicalToFluid();
        return fluidOutput;
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
     * For Serializer use.
     *
     * @return the uncopied basic output
     */
    @Nullable
    public ChemicalStackTemplate getChemicalOutputRaw() {
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
     * For Serializer use.
     *
     * @return the uncopied basic output
     */
    @Nullable
    public FluidStackTemplate getFluidOutputRaw() {
        return this.fluidOutput;
    }

    @Override
    public RecipeSerializer<BasicRotaryRecipe> getSerializer() {
        return MekanismRecipeSerializers.ROTARY.get();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicRotaryRecipe other = (BasicRotaryRecipe) o;
        if (hasChemicalToFluid() == other.hasChemicalToFluid() && hasFluidToChemical() == other.hasFluidToChemical()) {
            boolean equal = true;
            if (hasChemicalToFluid()) {
                equal = chemicalInput.equals(other.chemicalInput) && Objects.equals(fluidOutput, other.fluidOutput);
            }
            if (hasFluidToChemical()) {
                equal |= fluidInput.equals(other.fluidInput) && Objects.equals(chemicalOutput, other.chemicalOutput);
            }
            return equal;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash;
        if (hasFluidToChemical()) {
            hash = 31 * fluidInput.hashCode() + chemicalOutput.hashCode();
        } else {
            hash = 1;
        }
        if (hasChemicalToFluid()) {
            hash = 31 * hash + chemicalInput.hashCode();
            //TODO - 26.1: Validate this is fine in relation to direct codecs
            hash = 31 * hash + fluidOutput.hashCode();
        }
        return hash;
    }
}