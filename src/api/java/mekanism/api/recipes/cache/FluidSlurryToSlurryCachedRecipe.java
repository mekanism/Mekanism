package mekanism.api.recipes.cache;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraftforge.fluids.FluidStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidSlurryToSlurryCachedRecipe extends CachedRecipe<FluidSlurryToSlurryRecipe> {

    private final IOutputHandler<@NonNull SlurryStack> outputHandler;
    private final IInputHandler<@NonNull FluidStack> fluidInputHandler;
    private final IInputHandler<@NonNull SlurryStack> slurryInputHandler;

    public FluidSlurryToSlurryCachedRecipe(FluidSlurryToSlurryRecipe recipe, IInputHandler<@NonNull FluidStack> fluidInputHandler,
          IInputHandler<@NonNull SlurryStack> slurryInputHandler, IOutputHandler<@NonNull SlurryStack> outputHandler) {
        super(recipe);
        this.fluidInputHandler = fluidInputHandler;
        this.slurryInputHandler = slurryInputHandler;
        this.outputHandler = outputHandler;
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax <= 0) {
            //If our parent checks show we can't operate then return so
            return currentMax;
        }
        FluidStack recipeFluid = fluidInputHandler.getRecipeInput(recipe.getFluidInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputFluid)
        if (recipeFluid.isEmpty()) {
            return -1;
        }
        SlurryStack recipeSlurry = slurryInputHandler.getRecipeInput(recipe.getSlurryInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputSlurry)
        if (recipeSlurry.isEmpty()) {
            return -1;
        }
        //Calculate the current max based on the fluid input
        currentMax = fluidInputHandler.operationsCanSupport(recipe.getFluidInput(), currentMax);
        //Calculate the current max based on the slurry input
        currentMax = slurryInputHandler.operationsCanSupport(recipe.getSlurryInput(), currentMax);
        if (currentMax <= 0) {
            //If our input can't handle it return that we should be resetting
            return -1;
        }
        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeFluid, recipeSlurry), currentMax);
    }

    @Override
    public boolean isInputValid() {
        FluidStack fluidStack = fluidInputHandler.getInput();
        if (fluidStack.isEmpty()) {
            return false;
        }
        SlurryStack slurryInput = slurryInputHandler.getInput();
        if (slurryInput.isEmpty()) {
            return false;
        }
        return recipe.test(fluidStack, slurryInput);
    }

    @Override
    protected void finishProcessing(int operations) {
        //TODO - Performance: Eventually we should look into caching this stuff from when getOperationsThisTick was called?
        FluidStack recipeFluid = fluidInputHandler.getRecipeInput(recipe.getFluidInput());
        if (recipeFluid.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }

        //Now check the slurry input
        SlurryStack recipeSlurry = slurryInputHandler.getRecipeInput(recipe.getSlurryInput());
        if (recipeSlurry.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        fluidInputHandler.use(recipeFluid, operations);
        slurryInputHandler.use(recipeSlurry, operations);
        outputHandler.handleOutput(recipe.getOutput(recipeFluid, recipeSlurry), operations);
    }
}