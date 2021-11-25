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

    /**
     * @param recipe             Recipe.
     * @param fluidInputHandler  Fluid input handler.
     * @param gasInputHandler    Gas input handler.
     * @param gasOutputHandler   Gas output handler.
     * @param fluidOutputHandler Fluid output handler.
     * @param modeSupplier       Machine handling mode. Returns {@code true} for fluid to gas, and {@code false} for gas to fluid.
     */
    public RotaryCachedRecipe(RotaryRecipe recipe, IInputHandler<@NonNull FluidStack> fluidInputHandler, IInputHandler<@NonNull GasStack> gasInputHandler,
          IOutputHandler<@NonNull GasStack> gasOutputHandler, IOutputHandler<@NonNull FluidStack> fluidOutputHandler, BooleanSupplier modeSupplier) {
        super(recipe);
        this.fluidInputHandler = Objects.requireNonNull(fluidInputHandler, "Fluid input handler cannot be null.");
        this.gasInputHandler = Objects.requireNonNull(gasInputHandler, "Gas input handler cannot be null.");
        this.gasOutputHandler = Objects.requireNonNull(gasOutputHandler, "Gas output handler cannot be null.");
        this.fluidOutputHandler = Objects.requireNonNull(fluidOutputHandler, "Fluid output handler cannot be null.");
        this.modeSupplier = Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null.");
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax <= 0) {
            //If our parent checks show we can't operate then return so
            return currentMax;
        }
        //Mode == true if fluid to gas
        if (modeSupplier.getAsBoolean()) {
            if (!recipe.hasFluidToGas()) {
                //If our recipe doesn't have a fluid to gas version, return that we cannot operate
                return -1;
            }
            //Handle fluid to gas conversion
            recipeFluid = fluidInputHandler.getRecipeInput(recipe.getFluidInput());
            //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputFluid)
            if (recipeFluid.isEmpty()) {
                return -1;
            }
            //Calculate the current max based on the fluid input
            currentMax = fluidInputHandler.operationsCanSupport(recipeFluid, currentMax);
            if (currentMax <= 0) {
                //If our input can't handle it return that we should be resetting
                return -1;
            }
            //Calculate the max based on the space in the output
            return gasOutputHandler.operationsRoomFor(recipe.getGasOutput(recipeFluid), currentMax);
        }
        if (!recipe.hasGasToFluid()) {
            //If our recipe doesn't have a gas to fluid version, return that we cannot operate
            return -1;
        }
        //Handle gas to fluid conversion
        recipeGas = gasInputHandler.getRecipeInput(recipe.getGasInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(recipeGas)
        if (recipeGas.isEmpty()) {
            return -1;
        }
        //Calculate the current max based on the gas input
        currentMax = gasInputHandler.operationsCanSupport(recipeGas, currentMax);
        if (currentMax <= 0) {
            //If our input can't handle it return that we should be resetting
            return -1;
        }
        //Calculate the max based on the space in the output
        return fluidOutputHandler.operationsRoomFor(recipe.getFluidOutput(recipeGas), currentMax);
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
            if (fluidStack.isEmpty()) {
                return false;
            }
            return recipe.test(fluidStack);
        }
        if (!recipe.hasGasToFluid()) {
            //If our recipe doesn't have a gas to fluid version, return that we cannot operate
            return false;
        }
        GasStack gasStack = gasInputHandler.getInput();
        if (gasStack.isEmpty()) {
            return false;
        }
        return recipe.test(gasStack);
    }

    @Override
    protected void finishProcessing(int operations) {
        //Mode == true if fluid to gas
        if (modeSupplier.getAsBoolean()) {
            if (recipe.hasFluidToGas()) {
                if (recipeFluid.isEmpty()) {
                    //Something went wrong, this if should never really be true if we got to finishProcessing
                    return;
                }
                fluidInputHandler.use(recipeFluid, operations);
                gasOutputHandler.handleOutput(recipe.getGasOutput(recipeFluid), operations);
            }
        } else if (recipe.hasGasToFluid()) {
            if (recipeGas.isEmpty()) {
                //Something went wrong, this if should never really be true if we got to finishProcessing
                return;
            }
            gasInputHandler.use(recipeGas, operations);
            fluidOutputHandler.handleOutput(recipe.getFluidOutput(recipeGas), operations);
        }
    }
}