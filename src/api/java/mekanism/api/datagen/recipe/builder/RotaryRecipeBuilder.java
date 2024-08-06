package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.basic.BasicRotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RotaryRecipeBuilder extends MekanismRecipeBuilder<RotaryRecipeBuilder> {

    @Nullable
    private final ChemicalStackIngredient gasInput;
    @Nullable
    private final FluidStackIngredient fluidInput;
    private final FluidStack fluidOutput;
    private final ChemicalStack gasOutput;

    protected RotaryRecipeBuilder(@Nullable FluidStackIngredient fluidInput, @Nullable ChemicalStackIngredient gasInput, ChemicalStack gasOutput, FluidStack fluidOutput) {
        this.gasInput = gasInput;
        this.fluidInput = fluidInput;
        this.gasOutput = gasOutput;
        this.fluidOutput = fluidOutput;
    }

    /**
     * Creates a Rotary recipe builder. For converting a fluid into a gas.
     *
     * @param fluidInput Input.
     * @param gasOutput  Output.
     *
     * @apiNote It is recommended to use {@link #rotary(FluidStackIngredient, ChemicalStackIngredient, ChemicalStack, FluidStack)} over this method in combination with
     * {@link #rotary(ChemicalStackIngredient, FluidStack)} if the conversion will be possible in both directions.
     */
    public static RotaryRecipeBuilder rotary(FluidStackIngredient fluidInput, ChemicalStack gasOutput) {
        if (gasOutput.isEmpty()) {
            throw new IllegalArgumentException("This rotary condensentrator recipe requires a non empty gas output.");
        }
        return new RotaryRecipeBuilder(fluidInput, null, gasOutput, FluidStack.EMPTY);
    }

    /**
     * Creates a Rotary recipe builder. For converting a gas into a fluid.
     *
     * @param gasInput    Input.
     * @param fluidOutput Output.
     *
     * @apiNote It is recommended to use {@link #rotary(FluidStackIngredient, ChemicalStackIngredient, ChemicalStack, FluidStack)} over this method in combination with
     * {@link #rotary(FluidStackIngredient, ChemicalStack)} if the conversion will be possible in both directions.
     */
    public static RotaryRecipeBuilder rotary(ChemicalStackIngredient gasInput, FluidStack fluidOutput) {
        if (fluidOutput.isEmpty()) {
            throw new IllegalArgumentException("This rotary condensentrator recipe requires a non empty fluid output.");
        }
        return new RotaryRecipeBuilder(null, gasInput, ChemicalStack.EMPTY, fluidOutput);
    }

    /**
     * Creates a Rotary recipe builder that is capable of converting a fluid into a gas and a gas into a fluid.
     *
     * @param fluidInput  Fluid Input. (For fluid to gas)
     * @param gasInput    Gas Input. (For gas to fluid)
     * @param gasOutput   Gas Output. (For fluid to gas)
     * @param fluidOutput Fluid Output. (For gas to fluid)
     */
    public static RotaryRecipeBuilder rotary(FluidStackIngredient fluidInput, ChemicalStackIngredient gasInput, ChemicalStack gasOutput, FluidStack fluidOutput) {
        if (gasOutput.isEmpty() || fluidOutput.isEmpty()) {
            throw new IllegalArgumentException("This rotary condensentrator recipe requires non empty gas and fluid outputs.");
        }
        return new RotaryRecipeBuilder(fluidInput, gasInput, gasOutput, fluidOutput);
    }

    @Override
    protected RotaryRecipe asRecipe() {
        if (fluidInput != null) {
            if (gasInput != null) {
                return new BasicRotaryRecipe(fluidInput, gasInput, gasOutput, fluidOutput);
            }
            return new BasicRotaryRecipe(fluidInput, gasOutput);
        } else if (gasInput != null) {
            return new BasicRotaryRecipe(gasInput, fluidOutput);
        }
        throw new IllegalStateException("Invalid rotary recipe");
    }
}