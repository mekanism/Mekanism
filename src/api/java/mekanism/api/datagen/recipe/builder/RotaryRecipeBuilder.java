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
    private final ChemicalStackIngredient chemicalInput;
    @Nullable
    private final FluidStackIngredient fluidInput;
    private final FluidStack fluidOutput;
    private final ChemicalStack chemicalOutput;

    protected RotaryRecipeBuilder(@Nullable FluidStackIngredient fluidInput, @Nullable ChemicalStackIngredient chemicalInput, ChemicalStack chemicalOutput,
          FluidStack fluidOutput) {
        this.chemicalInput = chemicalInput;
        this.fluidInput = fluidInput;
        this.chemicalOutput = chemicalOutput;
        this.fluidOutput = fluidOutput;
    }

    /**
     * Creates a Rotary recipe builder. For converting a fluid into a chemical.
     *
     * @param fluidInput     Input.
     * @param chemicalOutput Output.
     *
     * @apiNote It is recommended to use {@link #rotary(FluidStackIngredient, ChemicalStackIngredient, ChemicalStack, FluidStack)} over this method in combination with
     * {@link #rotary(ChemicalStackIngredient, FluidStack)} if the conversion will be possible in both directions.
     */
    public static RotaryRecipeBuilder rotary(FluidStackIngredient fluidInput, ChemicalStack chemicalOutput) {
        if (chemicalOutput.isEmpty()) {
            throw new IllegalArgumentException("This rotary condensentrator recipe requires a non empty chemical output.");
        }
        return new RotaryRecipeBuilder(fluidInput, null, chemicalOutput, FluidStack.EMPTY);
    }

    /**
     * Creates a Rotary recipe builder. For converting a chemical into a fluid.
     *
     * @param chemicalInput Input.
     * @param fluidOutput   Output.
     *
     * @apiNote It is recommended to use {@link #rotary(FluidStackIngredient, ChemicalStackIngredient, ChemicalStack, FluidStack)} over this method in combination with
     * {@link #rotary(FluidStackIngredient, ChemicalStack)} if the conversion will be possible in both directions.
     */
    public static RotaryRecipeBuilder rotary(ChemicalStackIngredient chemicalInput, FluidStack fluidOutput) {
        if (fluidOutput.isEmpty()) {
            throw new IllegalArgumentException("This rotary condensentrator recipe requires a non empty fluid output.");
        }
        return new RotaryRecipeBuilder(null, chemicalInput, ChemicalStack.EMPTY, fluidOutput);
    }

    /**
     * Creates a Rotary recipe builder that is capable of converting a fluid into a chemical and a chemical into a fluid.
     *
     * @param fluidInput     Fluid Input. (For fluid to chemical)
     * @param chemicalInput  Chemical Input. (For chemical to fluid)
     * @param chemicalOutput Chemical Output. (For fluid to chemical)
     * @param fluidOutput    Fluid Output. (For chemical to fluid)
     */
    public static RotaryRecipeBuilder rotary(FluidStackIngredient fluidInput, ChemicalStackIngredient chemicalInput, ChemicalStack chemicalOutput, FluidStack fluidOutput) {
        if (chemicalOutput.isEmpty() || fluidOutput.isEmpty()) {
            throw new IllegalArgumentException("This rotary condensentrator recipe requires non empty chemical and fluid outputs.");
        }
        return new RotaryRecipeBuilder(fluidInput, chemicalInput, chemicalOutput, fluidOutput);
    }

    @Override
    protected RotaryRecipe asRecipe() {
        if (fluidInput != null) {
            if (chemicalInput != null) {
                return new BasicRotaryRecipe(fluidInput, chemicalInput, chemicalOutput, fluidOutput);
            }
            return new BasicRotaryRecipe(fluidInput, chemicalOutput);
        } else if (chemicalInput != null) {
            return new BasicRotaryRecipe(chemicalInput, fluidOutput);
        }
        throw new IllegalStateException("Invalid rotary recipe");
    }
}