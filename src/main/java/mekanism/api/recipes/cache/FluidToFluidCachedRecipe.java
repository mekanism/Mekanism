package mekanism.api.recipes.cache;

import java.util.function.Supplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.common.config.MekanismConfig;
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
    public boolean hasResourcesForTick() {
        FluidStack fluid = getInputTank().getFluid();
        return fluid != null && recipe.test(fluid);
    }

    @Override
    protected void finishProcessing(int operations) {
        //TODO: Move most of this stuff to the passed in IntToIntFunction. Though we need to decide how it will handle it for
        // the fractional inputs it seems the thermal evap controller used to handle

        FluidTank inputTank = getInputTank();
        //TODO: Don't pass null
        FluidStack output = recipe.getOutput(null);
        //TODO: Properly get the current fluid that is being used, we should cache this in the recipe, in case our
        // stored fluid in the tank becomes empty so we know what our input was, and so that we know the proper amount
        // We can then invalidate it the type itself doesn't match. (And not worry for now if there are multiple ingredients
        // listed in the recipe that are the same just with different amounts)
        FluidStack input = recipe.getInput().getRepresentations();
        int inputAmount = input.amount;

        int outputNeeded = outputTank.getCapacity() - outputTank.getFluidAmount();
        double outputRatio = (double) output.amount / (double) inputAmount;
        double tempMult = Math.max(0, getTemperature()) * MekanismConfig.current().general.evaporationTempMultiplier.val();
        double inputToUse = tempMult * inputAmount * ((float) getHeight() / (float) maxHeight);
        inputToUse = Math.min(inputTank.getFluidAmount(), inputToUse);
        inputToUse = Math.min(inputToUse, outputNeeded / outputRatio);

        lastGain = inputToUse / (double) inputAmount;

        //TODO: Below this might stay in finish processing and above not???
        partialInput += inputToUse;

        if (partialInput >= 1) {
            int inputInt = (int) Math.floor(partialInput);
            inputTank.drain(inputInt, true);
            partialInput %= 1;
            partialOutput += (double) inputInt / inputAmount;
        }

        if (partialOutput >= 1) {
            int outputInt = (int) Math.floor(partialOutput);
            FluidStack copy = output.copy();
            copy.amount = outputInt;
            addToOutput.apply(copy, false);
            partialOutput %= 1;
        }

        //lastGain = 0 instead of setActive false

        //addToOutput.apply(recipe.getOutput(getInputTank().getFluid()), false);
    }
}