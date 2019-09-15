package mekanism.api.recipes.cache;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidGasToGasCachedRecipe extends CachedRecipe<FluidGasToGasRecipe> {

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final Supplier<@NonNull FluidTank> fluidTank;
    private final Supplier<@NonNull GasTank> gasTank;

    public FluidGasToGasCachedRecipe(FluidGasToGasRecipe recipe, Supplier<@NonNull FluidTank> fluidTank, Supplier<@NonNull GasTank> gasTank,
          IOutputHandler<@NonNull GasStack> outputHandler) {
        super(recipe);
        this.fluidTank = fluidTank;
        this.gasTank = gasTank;
        this.outputHandler = outputHandler;
    }

    @Nonnull
    private GasTank getGasTank() {
        return gasTank.get();
    }

    @Nonnull
    private FluidTank getFluidTank() {
        return fluidTank.get();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax == 0) {
            //If our parent checks show we can't operate then return so
            return 0;
        }

        FluidStack inputFluid = getFluidTank().getFluid();
        if (inputFluid == null || inputFluid.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return 0;
        }
        FluidStack recipeFluid = recipe.getFluidInput().getMatchingInstance(inputFluid);
        if (recipeFluid == null || recipeFluid.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return 0;
        }

        //Now check the gas input
        GasStack inputGas = getGasTank().getGas();
        if (inputGas == null || inputGas.amount == 0) {
            return 0;
        }
        GasStack recipeGas = recipe.getGasInput().getMatchingInstance(inputGas);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
        if (recipeGas == null || recipeGas.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return 0;
        }

        //Calculate the current max based on how much fluid input we have to what is needed, capping at what we are told to use as a max
        currentMax = Math.min(inputFluid.amount / recipeFluid.amount, currentMax);

        //Calculate the current max based on how much gas input we have to what is needed, capping at what we are told to use as a max
        //NOTE: We multiply the required gas amount by our gas usage amount
        //TODO: Should we be multiplying this by gas usage or somehow transition it to a new system
        currentMax = Math.min(inputGas.amount / recipeGas.amount, currentMax);

        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeFluid, recipeGas), currentMax);
    }

    @Override
    public boolean isInputValid() {
        GasStack gasInput = getGasTank().getGas();
        if (gasInput == null) {
            return false;
        }
        FluidStack fluidStack = getFluidTank().getFluid();
        if (fluidStack == null || fluidStack.amount == 0) {
            return false;
        }
        return recipe.test(fluidStack, gasInput);
    }

    @Override
    protected void finishProcessing(int operations) {
        //TODO: Cache this stuff from when getOperationsThisTick was called?
        FluidStack inputFluid = getFluidTank().getFluid();
        if (inputFluid == null || inputFluid.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        FluidStack recipeFluid = recipe.getFluidInput().getMatchingInstance(inputFluid);
        if (recipeFluid == null || recipeFluid.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }

        //Now check the gas input
        GasStack inputGas = getGasTank().getGas();
        if (inputGas == null || inputGas.amount == 0) {
            return;
        }
        GasStack recipeGas = recipe.getGasInput().getMatchingInstance(inputGas);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
        if (recipeGas == null || recipeGas.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        outputHandler.handleOutput(recipe.getOutput(recipeFluid, recipeGas), operations);
    }
}