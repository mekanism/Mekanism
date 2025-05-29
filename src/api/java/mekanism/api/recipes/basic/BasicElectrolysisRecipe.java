package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public class BasicElectrolysisRecipe extends ElectrolysisRecipe {

    protected final FluidStackIngredient input;
    protected final ChemicalStack leftChemicalOutput;
    protected final ChemicalStack rightChemicalOutput;
    protected final long energyMultiplier;//todo double?

    /**
     * @param input               Input.
     * @param energyMultiplier    Multiplier to the energy cost in relation to the configured hydrogen separating energy cost. Must be at least one.
     * @param leftChemicalOutput  Left output.
     * @param rightChemicalOutput Right output.
     */
    public BasicElectrolysisRecipe(FluidStackIngredient input, long energyMultiplier, ChemicalStack leftChemicalOutput, ChemicalStack rightChemicalOutput) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.energyMultiplier = energyMultiplier;
        if (energyMultiplier < 1) {
            throw new IllegalArgumentException("Energy multiplier must be at least one.");
        }
        Objects.requireNonNull(leftChemicalOutput, "Left output cannot be null");
        Objects.requireNonNull(rightChemicalOutput, "Right output cannot be null");
        if (leftChemicalOutput.isEmpty()) {
            throw new IllegalArgumentException("Left output cannot be empty.");
        } else if (rightChemicalOutput.isEmpty()) {
            throw new IllegalArgumentException("Right output cannot be empty.");
        }
        this.leftChemicalOutput = leftChemicalOutput.copy();
        this.rightChemicalOutput = rightChemicalOutput.copy();
    }

    @Override
    public FluidStackIngredient getInput() {
        return input;
    }

    @Override
    public List<ElectrolysisRecipeOutput> getOutputDefinition() {
        return Collections.singletonList(new ElectrolysisRecipeOutput(leftChemicalOutput, rightChemicalOutput));
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return this.input.test(fluidStack);
    }

    @Override
    @Contract(value = "_ -> new", pure = true)
    public ElectrolysisRecipeOutput getOutput(FluidStack input) {
        return new ElectrolysisRecipeOutput(leftChemicalOutput.copy(), rightChemicalOutput.copy());
    }

    @Override
    public long getEnergyMultiplier() {
        return energyMultiplier;
    }

    @Override
    public String getGroup() {
        return "electrolytic_separator";
    }

    public ChemicalStack getLeftChemicalOutput() {
        return leftChemicalOutput;
    }

    public ChemicalStack getRightChemicalOutput() {
        return rightChemicalOutput;
    }

    @Override
    public RecipeSerializer<BasicElectrolysisRecipe> getSerializer() {
        return MekanismRecipeSerializers.SEPARATING.get();
    }

    @Override
    public boolean equals(Object o) {
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
        result = 31 * result + Long.hashCode(energyMultiplier);
        return result;
    }
}