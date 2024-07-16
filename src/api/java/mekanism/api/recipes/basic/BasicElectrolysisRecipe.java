package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public class BasicElectrolysisRecipe extends ElectrolysisRecipe {

    protected final FluidStackIngredient input;
    protected final GasStack leftGasOutput;
    protected final GasStack rightGasOutput;
    protected final long energyMultiplier;//todo double?

    /**
     * @param input            Input.
     * @param energyMultiplier Multiplier to the energy cost in relation to the configured hydrogen separating energy cost. Must be at least one.
     * @param leftGasOutput    Left output.
     * @param rightGasOutput   Right output.
     */
    public BasicElectrolysisRecipe(FluidStackIngredient input, long energyMultiplier, GasStack leftGasOutput, GasStack rightGasOutput) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.energyMultiplier = energyMultiplier;
        if (energyMultiplier < 1) {
            throw new IllegalArgumentException("Energy multiplier must be at least one.");
        }
        Objects.requireNonNull(leftGasOutput, "Left output cannot be null");
        Objects.requireNonNull(rightGasOutput, "Right output cannot be null");
        if (leftGasOutput.isEmpty()) {
            throw new IllegalArgumentException("Left output cannot be empty.");
        } else if (rightGasOutput.isEmpty()) {
            throw new IllegalArgumentException("Right output cannot be empty.");
        }
        this.leftGasOutput = leftGasOutput.copy();
        this.rightGasOutput = rightGasOutput.copy();
    }

    @Override
    public FluidStackIngredient getInput() {
        return input;
    }

    @Override
    public List<ElectrolysisRecipeOutput> getOutputDefinition() {
        return Collections.singletonList(new ElectrolysisRecipeOutput(leftGasOutput, rightGasOutput));
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return this.input.test(fluidStack);
    }

    @Override
    @Contract(value = "_ -> new", pure = true)
    public ElectrolysisRecipeOutput getOutput(FluidStack input) {
        return new ElectrolysisRecipeOutput(leftGasOutput.copy(), rightGasOutput.copy());
    }

    @Override
    public long getEnergyMultiplier() {
        return energyMultiplier;
    }

    @Override
    public String getGroup() {
        return "electrolytic_separator";
    }

    public GasStack getLeftGasOutput() {
        return leftGasOutput;
    }

    public GasStack getRightGasOutput() {
        return rightGasOutput;
    }

    @Override
    public RecipeSerializer<BasicElectrolysisRecipe> getSerializer() {
        return MekanismRecipeSerializers.SEPARATING.get();
    }
}