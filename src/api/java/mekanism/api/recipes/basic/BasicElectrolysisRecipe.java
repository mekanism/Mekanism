package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackSlotDisplay;
import net.minecraft.core.TypedInstance;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public class BasicElectrolysisRecipe extends ElectrolysisRecipe {

    protected final FluidStackIngredient input;
    protected final ChemicalStackTemplate leftChemicalOutput;
    protected final ChemicalStackTemplate rightChemicalOutput;
    protected final int energyMultiplier;//todo double?

    /// @param input               Input.
    /// @param energyMultiplier    Multiplier to the energy cost in relation to the configured hydrogen separating energy cost. Must be at least one.
    /// @param leftChemicalOutput  Left output.
    /// @param rightChemicalOutput Right output.
    public BasicElectrolysisRecipe(FluidStackIngredient input, int energyMultiplier, ChemicalStackTemplate leftChemicalOutput, ChemicalStackTemplate rightChemicalOutput) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.energyMultiplier = energyMultiplier;
        if (energyMultiplier < 1) {
            throw new IllegalArgumentException("Energy multiplier must be at least one.");
        }
        this.leftChemicalOutput = Objects.requireNonNull(leftChemicalOutput, "Left output cannot be null");
        this.rightChemicalOutput = Objects.requireNonNull(rightChemicalOutput, "Right output cannot be null");
    }

    @Override
    public FluidStackIngredient getInput() {
        return input;
    }

    @Override
    public List<ElectrolysisRecipeOutput> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(new ElectrolysisRecipeOutput(leftChemicalOutput, rightChemicalOutput));
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        return new SlotDisplay.Composite(List.of(new ChemicalStackSlotDisplay(leftChemicalOutput), new ChemicalStackSlotDisplay(rightChemicalOutput)));
    }

    @Override
    @Contract(value = "_ -> new", pure = true)
    public ElectrolysisRecipeOutput getOutput(TypedInstance<Fluid> input) {
        return new ElectrolysisRecipeOutput(leftChemicalOutput, rightChemicalOutput);
    }

    @Override
    public int getEnergyMultiplier() {
        return energyMultiplier;
    }

    public ChemicalStackTemplate getLeftChemicalOutput() {
        return leftChemicalOutput;
    }

    public ChemicalStackTemplate getRightChemicalOutput() {
        return rightChemicalOutput;
    }

    @Override
    public RecipeSerializer<BasicElectrolysisRecipe> getSerializer() {
        return MekanismRecipeSerializers.SEPARATING.get();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicElectrolysisRecipe other = (BasicElectrolysisRecipe) o;
        return energyMultiplier == other.energyMultiplier && input.equals(other.input) && leftChemicalOutput.equals(other.leftChemicalOutput) &&
               rightChemicalOutput.equals(other.rightChemicalOutput);
    }

    @Override
    public int hashCode() {
        int result = input.hashCode();
        result = 31 * result + leftChemicalOutput.hashCode();
        result = 31 * result + rightChemicalOutput.hashCode();
        result = 31 * result + energyMultiplier;
        return result;
    }
}