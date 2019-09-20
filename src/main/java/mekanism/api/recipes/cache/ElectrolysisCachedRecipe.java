package mekanism.api.recipes.cache;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ElectrolysisCachedRecipe extends CachedRecipe<ElectrolysisRecipe> {

    private final IOutputHandler<@NonNull Pair<GasStack, GasStack>> outputHandler;
    private final IInputHandler<@NonNull FluidStack> inputHandler;

    public ElectrolysisCachedRecipe(ElectrolysisRecipe recipe, IInputHandler<@NonNull FluidStack> inputHandler, IOutputHandler<@NonNull Pair<GasStack, GasStack>> outputHandler) {
        super(recipe);
        this.inputHandler = inputHandler;
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
        FluidStack recipeFluid = inputHandler.getRecipeInput(recipe.getInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputFluid)
        if (recipeFluid == null || recipeFluid.amount == 0) {
            return 0;
        }
        //Calculate the current max based on the fluid input
        currentMax = inputHandler.operationsCanSupport(recipe.getInput(), currentMax);
        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeFluid), currentMax);
    }

    @Override
    public boolean isInputValid() {
        FluidStack fluidStack = inputHandler.getInput();
        return fluidStack != null && recipe.test(fluidStack);
    }

    @Override
    protected void finishProcessing(int operations) {
        //TODO: Cache this stuff from when getOperationsThisTick was called?
        FluidStack recipeFluid = inputHandler.getRecipeInput(recipe.getInput());
        if (recipeFluid == null || recipeFluid.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        inputHandler.use(recipeFluid, operations);
        outputHandler.handleOutput(recipe.getOutput(recipeFluid), operations);
    }
}