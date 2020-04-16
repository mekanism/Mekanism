package mekanism.api.recipes.cache;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class CombinerCachedRecipe extends CachedRecipe<CombinerRecipe> {

    private final IOutputHandler<@NonNull ItemStack> outputHandler;
    private final IInputHandler<@NonNull ItemStack> inputHandler;
    private final IInputHandler<@NonNull ItemStack> extraInputHandler;

    public CombinerCachedRecipe(CombinerRecipe recipe, IInputHandler<@NonNull ItemStack> inputHandler, IInputHandler<@NonNull ItemStack> extraInputHandler,
          IOutputHandler<@NonNull ItemStack> outputHandler) {
        super(recipe);
        this.inputHandler = inputHandler;
        this.extraInputHandler = extraInputHandler;
        this.outputHandler = outputHandler;
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax <= 0) {
            //If our parent checks show we can't operate then return so
            return currentMax;
        }
        ItemStack recipeMain = inputHandler.getRecipeInput(recipe.getMainInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
        if (recipeMain.isEmpty()) {
            return -1;
        }
        ItemStack recipeExtra = extraInputHandler.getRecipeInput(recipe.getExtraInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
        if (recipeExtra.isEmpty()) {
            return -1;
        }
        //Calculate the current max based on the main input
        currentMax = inputHandler.operationsCanSupport(recipe.getMainInput(), currentMax);
        //Calculate the current max based on the extra input
        currentMax = extraInputHandler.operationsCanSupport(recipe.getExtraInput(), currentMax);
        if (currentMax <= 0) {
            //If our input can't handle it return that we should be resetting
            return -1;
        }
        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeMain, recipeExtra), currentMax);
    }

    @Override
    public boolean isInputValid() {
        return recipe.test(inputHandler.getInput(), extraInputHandler.getInput());
    }

    @Override
    protected void finishProcessing(int operations) {
        ItemStack recipeMain = inputHandler.getRecipeInput(recipe.getMainInput());
        if (recipeMain.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        ItemStack recipeExtra = extraInputHandler.getRecipeInput(recipe.getExtraInput());
        if (recipeExtra.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        inputHandler.use(recipeMain, operations);
        extraInputHandler.use(recipeExtra, operations);
        outputHandler.handleOutput(recipe.getOutput(recipeMain, recipeExtra), operations);
    }
}