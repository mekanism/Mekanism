package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackSlotDisplay;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackSlotDisplay;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public class BasicRotaryRecipe extends RotaryRecipe {

    @Nullable
    protected final ChemicalStackIngredient chemicalInput;
    @Nullable
    protected final FluidStackIngredient fluidInput;
    @Nullable
    private final FluidStackTemplate fluidOutput;
    @Nullable
    private final ChemicalStackTemplate chemicalOutput;

    /// Rotary recipe that converts a fluid into a chemical.
    ///
    /// @param fluidInput     Fluid input.
    /// @param chemicalOutput Chemical output.
    ///
    /// @apiNote It is recommended to use [#BasicRotaryRecipe(FluidStackIngredient, ChemicalStackIngredient, ChemicalStackTemplate, FluidStackTemplate)] over this
    /// constructor in combination with [#BasicRotaryRecipe(ChemicalStackIngredient, FluidStackTemplate)] and making two separate recipes if the conversion will be
    /// possible in both directions.
    public BasicRotaryRecipe(FluidStackIngredient fluidInput, ChemicalStackTemplate chemicalOutput) {
        this.fluidInput = Objects.requireNonNull(fluidInput, "Fluid input cannot be null.");
        this.chemicalOutput = Objects.requireNonNull(chemicalOutput, "Chemical output cannot be null.");
        this.chemicalInput = null;
        this.fluidOutput = null;
    }

    /// Rotary recipe that converts a chemical into a fluid.
    ///
    /// @param chemicalInput Chemical input.
    /// @param fluidOutput   Fluid output.
    ///
    /// @apiNote It is recommended to use [#BasicRotaryRecipe(FluidStackIngredient, ChemicalStackIngredient, ChemicalStackTemplate, FluidStackTemplate)] over this
    /// constructor in combination with [#BasicRotaryRecipe(FluidStackIngredient, ChemicalStackTemplate)] and making two separate recipes if the conversion will be
    /// possible in both directions.
    public BasicRotaryRecipe(ChemicalStackIngredient chemicalInput, FluidStackTemplate fluidOutput) {
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        this.fluidOutput = Objects.requireNonNull(fluidOutput, "Fluid output cannot be null.");
        this.fluidInput = null;
        this.chemicalOutput = null;
    }

    /// Rotary recipe that is capable of converting a fluid into a chemical and a chemical into a fluid.
    ///
    /// @param fluidInput     Fluid input.
    /// @param chemicalInput  Chemical input.
    /// @param chemicalOutput Chemical output.
    /// @param fluidOutput    Fluid output.
    ///
    /// @apiNote It is recommended to use this constructor over using [#BasicRotaryRecipe(FluidStackIngredient, ChemicalStackTemplate)] and
    /// [#BasicRotaryRecipe(ChemicalStackIngredient, FluidStackTemplate)] in combination and creating two recipes if the conversion will be possible in both directions.
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

    @Override
    public boolean test(FluidStack fluidStack) {
        return hasFluidToChemical() && getFluidInput().test(fluidStack);
    }

    @Override
    public boolean test(ChemicalStack chemicalStack) {
        return hasChemicalToFluid() && getChemicalInput().test(chemicalStack);
    }

    @Override
    public FluidStackIngredient getFluidInput() {
        return Objects.requireNonNull(fluidInput, "This recipe has no fluid to chemical conversion.");
    }

    @Override
    public ChemicalStackIngredient getChemicalInput() {
        return Objects.requireNonNull(chemicalInput, "This recipe has no chemical to fluid conversion.");
    }

    @Override
    public List<ChemicalStackTemplate> getChemicalOutputDefinition(ContextMap contextMap) {
        ChemicalStackTemplate output = Objects.requireNonNull(chemicalOutput, "This recipe has no fluid to chemical conversion.");
        return Collections.singletonList(output);
    }

    @Override
    public SlotDisplay getChemicalOutputDisplay() {
        return chemicalOutput == null ? SlotDisplay.Empty.INSTANCE : new ChemicalStackSlotDisplay(chemicalOutput);
    }

    @Override
    public List<FluidStackTemplate> getFluidOutputDefinition(ContextMap contextMap) {
        FluidStackTemplate output = Objects.requireNonNull(fluidOutput, "This recipe has no chemical to fluid conversion.");
        return Collections.singletonList(output);
    }

    @Override
    public SlotDisplay getFluidOutputDisplay() {
        return fluidOutput == null ? SlotDisplay.Empty.INSTANCE : new FluidStackSlotDisplay(fluidOutput);
    }

    @Override
    @Contract(pure = true)
    public ChemicalStackTemplate getChemicalOutput(FluidStack input) {
        return Objects.requireNonNull(chemicalOutput, "This recipe has no fluid to chemical conversion.");
    }

    @Override
    @Contract(pure = true)
    public FluidStackTemplate getFluidOutput(ChemicalStack input) {
        return Objects.requireNonNull(fluidOutput, "This recipe has no chemical to fluid conversion.");
    }

    /// For Serializer use. DO NOT MODIFY RETURN VALUE.
    ///
    /// @return the uncopied basic input or an empty optional if the recipe doesn't support chemical to fluid recipes.
    public Optional<ChemicalStackIngredient> getChemicalInputRaw() {
        return Optional.ofNullable(chemicalInput);
    }

    /// For Serializer use.
    ///
    /// @return the uncopied basic output
    public Optional<ChemicalStackTemplate> getChemicalOutputRaw() {
        return Optional.ofNullable(chemicalOutput);
    }

    /// For Serializer use. DO NOT MODIFY RETURN VALUE.
    ///
    /// @return the uncopied basic input or an empty optional if the recipe doesn't support fluid to chemical recipes.
    public Optional<FluidStackIngredient> getFluidInputRaw() {
        return Optional.ofNullable(fluidInput);
    }

    /// For Serializer use.
    ///
    /// @return the uncopied basic output
    public Optional<FluidStackTemplate> getFluidOutputRaw() {
        return Optional.ofNullable(fluidOutput);
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
        return Objects.equals(chemicalInput, other.chemicalInput) && Objects.equals(fluidOutput, other.fluidOutput) &&
               Objects.equals(fluidInput, other.fluidInput) && Objects.equals(chemicalOutput, other.chemicalOutput);
    }

    @Override
    public int hashCode() {
        int hash;
        if (fluidInput != null && chemicalOutput != null) {//hasFluidToChemical
            hash = 31 * fluidInput.hashCode() + chemicalOutput.hashCode();
        } else {
            hash = 1;
        }
        if (chemicalInput != null && fluidOutput != null) {//hasChemicalToFluid
            hash = 31 * hash + chemicalInput.hashCode();
            hash = 31 * hash + fluidOutput.hashCode();
        }
        return hash;
    }
}