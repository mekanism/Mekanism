package mekanism.api.recipes.cache;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraftforge.fluids.FluidStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidGasToGasCachedRecipe extends CachedRecipe<FluidGasToGasRecipe> {

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final IInputHandler<@NonNull FluidStack> fluidInputHandler;
    private final IInputHandler<@NonNull GasStack> gasInputHandler;

    public FluidGasToGasCachedRecipe(FluidGasToGasRecipe recipe, IInputHandler<@NonNull FluidStack> fluidInputHandler, IInputHandler<@NonNull GasStack> gasInputHandler,
          IOutputHandler<@NonNull GasStack> outputHandler) {
        super(recipe);
        this.fluidInputHandler = fluidInputHandler;
        this.gasInputHandler = gasInputHandler;
        this.outputHandler = outputHandler;
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax == 0) {
            //If our parent checks show we can't operate then return so
            return 0;
        }
        //TODO: This input getting, is only really needed for getting the output
        FluidStack recipeFluid = fluidInputHandler.getRecipeInput(recipe.getFluidInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputFluid)
        if (recipeFluid.isEmpty()) {
            return 0;
        }

        GasStack recipeGas = gasInputHandler.getRecipeInput(recipe.getGasInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
        if (recipeGas.isEmpty()) {
            return 0;
        }

        //Calculate the current max based on the fluid input
        currentMax = fluidInputHandler.operationsCanSupport(recipe.getFluidInput(), currentMax);

        //Calculate the current max based on the gas input
        currentMax = gasInputHandler.operationsCanSupport(recipe.getGasInput(), currentMax);

        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeFluid, recipeGas), currentMax);
    }

    @Override
    public boolean isInputValid() {
        FluidStack fluidStack = fluidInputHandler.getInput();
        if (fluidStack.isEmpty()) {
            return false;
        }
        GasStack gasInput = gasInputHandler.getInput();
        if (gasInput.isEmpty()) {
            return false;
        }
        return recipe.test(fluidStack, gasInput);
    }

    @Override
    protected void finishProcessing(int operations) {
        //TODO: Cache this stuff from when getOperationsThisTick was called?
        FluidStack recipeFluid = fluidInputHandler.getRecipeInput(recipe.getFluidInput());
        if (recipeFluid.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }

        //Now check the gas input
        GasStack recipeGas = gasInputHandler.getRecipeInput(recipe.getGasInput());
        if (recipeGas.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        fluidInputHandler.use(recipeFluid, operations);
        gasInputHandler.use(recipeGas, operations);
        outputHandler.handleOutput(recipe.getOutput(recipeFluid, recipeGas), operations);
    }
}