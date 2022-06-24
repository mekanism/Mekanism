package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RotaryRecipeBuilder extends MekanismRecipeBuilder<RotaryRecipeBuilder> {

    private final RecipeDirection direction;
    @Nullable
    private final GasStackIngredient gasInput;
    @Nullable
    private final FluidStackIngredient fluidInput;
    private final FluidStack fluidOutput;
    private final GasStack gasOutput;

    protected RotaryRecipeBuilder(@Nullable FluidStackIngredient fluidInput, @Nullable GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput, RecipeDirection direction) {
        super(mekSerializer("rotary"));
        this.direction = direction;
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
     * @apiNote It is recommended to use {@link #rotary(FluidStackIngredient, GasStackIngredient, GasStack, FluidStack)} over this method in combination with {@link
     * #rotary(GasStackIngredient, FluidStack)} if the conversion will be possible in both directions.
     */
    public static RotaryRecipeBuilder rotary(FluidStackIngredient fluidInput, GasStack gasOutput) {
        if (gasOutput.isEmpty()) {
            throw new IllegalArgumentException("This rotary condensentrator recipe requires a non empty gas output.");
        }
        return new RotaryRecipeBuilder(fluidInput, null, gasOutput, FluidStack.EMPTY, RecipeDirection.FLUID_TO_GAS);
    }

    /**
     * Creates a Rotary recipe builder. For converting a gas into a fluid.
     *
     * @param gasInput    Input.
     * @param fluidOutput Output.
     *
     * @apiNote It is recommended to use {@link #rotary(FluidStackIngredient, GasStackIngredient, GasStack, FluidStack)} over this method in combination with {@link
     * #rotary(FluidStackIngredient, GasStack)} if the conversion will be possible in both directions.
     */
    public static RotaryRecipeBuilder rotary(GasStackIngredient gasInput, FluidStack fluidOutput) {
        if (fluidOutput.isEmpty()) {
            throw new IllegalArgumentException("This rotary condensentrator recipe requires a non empty fluid output.");
        }
        return new RotaryRecipeBuilder(null, gasInput, GasStack.EMPTY, fluidOutput, RecipeDirection.GAS_TO_FLUID);
    }

    /**
     * Creates a Rotary recipe builder that is capable of converting a fluid into a gas and a gas into a fluid.
     *
     * @param fluidInput  Fluid Input. (For fluid to gas)
     * @param gasInput    Gas Input. (For gas to fluid)
     * @param gasOutput   Gas Output. (For fluid to gas)
     * @param fluidOutput Fluid Output. (For gas to fluid)
     */
    public static RotaryRecipeBuilder rotary(FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput) {
        if (gasOutput.isEmpty() || fluidOutput.isEmpty()) {
            throw new IllegalArgumentException("This rotary condensentrator recipe requires non empty gas and fluid outputs.");
        }
        return new RotaryRecipeBuilder(fluidInput, gasInput, gasOutput, fluidOutput, RecipeDirection.BOTH);
    }

    @Override
    protected RotaryRecipeResult getResult(ResourceLocation id) {
        return new RotaryRecipeResult(id);
    }

    public class RotaryRecipeResult extends RecipeResult {

        protected RotaryRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            if (direction.hasFluidToGas && fluidInput != null) {
                json.add(JsonConstants.FLUID_INPUT, fluidInput.serialize());
                json.add(JsonConstants.GAS_OUTPUT, SerializerHelper.serializeGasStack(gasOutput));
            }
            if (direction.hasGasToFluid && gasInput != null) {
                json.add(JsonConstants.GAS_INPUT, gasInput.serialize());
                json.add(JsonConstants.FLUID_OUTPUT, SerializerHelper.serializeFluidStack(fluidOutput));
            }
        }
    }

    private enum RecipeDirection {
        FLUID_TO_GAS(true, false),
        GAS_TO_FLUID(false, true),
        BOTH(true, true);

        private final boolean hasFluidToGas;
        private final boolean hasGasToFluid;

        RecipeDirection(boolean hasFluidToGas, boolean hasGasToFluid) {
            this.hasFluidToGas = hasFluidToGas;
            this.hasGasToFluid = hasGasToFluid;
        }
    }
}