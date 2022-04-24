package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraftforge.fluids.FluidStack;

/**
 * Base class to help implement handling of rotary recipes.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class RotaryCachedRecipe extends CachedRecipe<RotaryRecipe> {

    private final IOutputHandler<@NonNull GasStack> gasOutputHandler;
    private final IOutputHandler<@NonNull FluidStack> fluidOutputHandler;
    private final IInputHandler<@NonNull FluidStack> fluidInputHandler;
    private final IInputHandler<@NonNull GasStack> gasInputHandler;
    private final BooleanSupplier modeSupplier;

    private FluidStack recipeFluid = FluidStack.EMPTY;
    private GasStack recipeGas = GasStack.EMPTY;
    private FluidStack fluidOutput = FluidStack.EMPTY;
    private GasStack gasOutput = GasStack.EMPTY;

    /**
     * @param recipe             Recipe.
     * @param recheckAllErrors   Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to
     *                           not do this every tick or if there is no one viewing recipes.
     * @param fluidInputHandler  Fluid input handler.
     * @param gasInputHandler    Gas input handler.
     * @param gasOutputHandler   Gas output handler.
     * @param fluidOutputHandler Fluid output handler.
     * @param modeSupplier       Machine handling mode. Returns {@code true} for fluid to gas, and {@code false} for gas to fluid.
     */
    public RotaryCachedRecipe(RotaryRecipe recipe, BooleanSupplier recheckAllErrors, IInputHandler<@NonNull FluidStack> fluidInputHandler,
          IInputHandler<@NonNull GasStack> gasInputHandler, IOutputHandler<@NonNull GasStack> gasOutputHandler, IOutputHandler<@NonNull FluidStack> fluidOutputHandler,
          BooleanSupplier modeSupplier) {
        super(recipe, recheckAllErrors);
        this.fluidInputHandler = Objects.requireNonNull(fluidInputHandler, "Fluid input handler cannot be null.");
        this.gasInputHandler = Objects.requireNonNull(gasInputHandler, "Gas input handler cannot be null.");
        this.gasOutputHandler = Objects.requireNonNull(gasOutputHandler, "Gas output handler cannot be null.");
        this.fluidOutputHandler = Objects.requireNonNull(fluidOutputHandler, "Fluid output handler cannot be null.");
        this.modeSupplier = Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null.");
    }

    @Override
    protected void calculateOperationsThisTick(OperationTracker tracker) {
        super.calculateOperationsThisTick(tracker);
        if (tracker.shouldContinueChecking()) {
            //Mode == true if fluid to gas
            if (modeSupplier.getAsBoolean()) {
                if (!recipe.hasFluidToGas()) {
                    //If our recipe doesn't have a fluid to gas version, return that we cannot operate
                    tracker.mismatchedRecipe();
                } else {
                    //Handle fluid to gas conversion
                    CachedRecipeHelper.oneInputCalculateOperationsThisTick(tracker, fluidInputHandler, recipe::getFluidInput, input -> recipeFluid = input,
                          gasOutputHandler, recipe::getGasOutput, output -> gasOutput = output, FluidStack::isEmpty);
                }
            } else if (!recipe.hasGasToFluid()) {
                //If our recipe doesn't have a gas to fluid version, return that we cannot operate
                tracker.mismatchedRecipe();
            } else {
                //Handle gas to fluid conversion
                CachedRecipeHelper.oneInputCalculateOperationsThisTick(tracker, gasInputHandler, recipe::getGasInput, input -> recipeGas = input,
                      fluidOutputHandler, recipe::getFluidOutput, output -> fluidOutput = output, GasStack::isEmpty);
            }
        }
    }

    @Override
    public boolean isInputValid() {
        //Mode == true if fluid to gas
        if (modeSupplier.getAsBoolean()) {
            if (!recipe.hasFluidToGas()) {
                //If our recipe doesn't have a fluid to gas version, return that we cannot operate
                return false;
            }
            FluidStack fluidStack = fluidInputHandler.getInput();
            return !fluidStack.isEmpty() && recipe.test(fluidStack);
        } else if (!recipe.hasGasToFluid()) {
            //If our recipe doesn't have a gas to fluid version, return that we cannot operate
            return false;
        }
        GasStack gasStack = gasInputHandler.getInput();
        return !gasStack.isEmpty() && recipe.test(gasStack);
    }

    @Override
    protected void finishProcessing(int operations) {
        //Mode == true if fluid to gas
        if (modeSupplier.getAsBoolean()) {
            //Validate something didn't go horribly wrong and the fluid is somehow empty
            if (recipe.hasFluidToGas() && !recipeFluid.isEmpty() && !gasOutput.isEmpty()) {
                fluidInputHandler.use(recipeFluid, operations);
                gasOutputHandler.handleOutput(gasOutput, operations);
            }
        } else if (recipe.hasGasToFluid() && !recipeGas.isEmpty() && !fluidOutput.isEmpty()) {
            //Validate something didn't go horribly wrong and the gas is somehow empty
            gasInputHandler.use(recipeGas, operations);
            fluidOutputHandler.handleOutput(fluidOutput, operations);
        }
    }
}