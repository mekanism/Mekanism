package mekanism.api.recipes.cache;

import java.util.function.Supplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidToFluidCachedRecipe extends CachedRecipe<FluidToFluidRecipe> {

    private final IOutputHandler<@NonNull FluidStack> outputHandler;
    private final Supplier<@NonNull FluidTank> inputTank;

    public FluidToFluidCachedRecipe(FluidToFluidRecipe recipe, Supplier<@NonNull FluidTank> inputTank, IOutputHandler<@NonNull FluidStack> outputHandler) {
        super(recipe);
        this.inputTank = inputTank;
        this.outputHandler = outputHandler;
    }

    private FluidTank getInputTank() {
        return inputTank.get();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax == 0) {
            //If our parent checks show we can't operate then return so
            return 0;
        }
        FluidStack inputFluid = getInputTank().getFluid();
        if (inputFluid == null || inputFluid.amount == 0) {
            return 0;
        }
        FluidStack recipeInput = recipe.getInput().getMatchingInstance(inputFluid);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputFluid)
        if (recipeInput == null || recipeInput.amount == 0) {
            //TODO: 1.14 make this check about being empty instead
            return 0;
        }
        //Calculate the current max based on how much input we have to what is needed, capping at what we are told to use as a max
        currentMax = Math.min(inputFluid.amount / recipeInput.amount, currentMax);
        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeInput), currentMax);
    }

    @Override
    public boolean isInputValid() {
        FluidStack fluid = getInputTank().getFluid();
        return fluid != null && recipe.test(fluid);
    }

    @Override
    protected void finishProcessing(int operations) {
        //TODO: Cache this stuff from when getOperationsThisTick was called?
        FluidStack inputFluid = getInputTank().getFluid();
        if (inputFluid == null || inputFluid.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        FluidStack recipeInput = recipe.getInput().getMatchingInstance(inputFluid);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputFluid)
        if (recipeInput == null || recipeInput.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        outputHandler.handleOutput(recipe.getOutput(recipeInput), operations);
    }
}