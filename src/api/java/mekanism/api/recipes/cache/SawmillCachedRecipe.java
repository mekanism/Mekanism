package mekanism.api.recipes.cache;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.item.ItemStack;

/**
 * Base class to help implement handling of sawing recipes.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class SawmillCachedRecipe extends CachedRecipe<SawmillRecipe> {

    private final IOutputHandler<@NonNull ChanceOutput> outputHandler;
    private final IInputHandler<@NonNull ItemStack> inputHandler;

    private ItemStack recipeItem = ItemStack.EMPTY;

    /**
     * @param recipe        Recipe.
     * @param inputHandler  Input handler.
     * @param outputHandler Output handler.
     */
    public SawmillCachedRecipe(SawmillRecipe recipe, IInputHandler<@NonNull ItemStack> inputHandler, IOutputHandler<@NonNull ChanceOutput> outputHandler) {
        super(recipe);
        this.inputHandler = Objects.requireNonNull(inputHandler, "Input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax <= 0) {
            //If our parent checks show we can't operate then return so
            return currentMax;
        }
        recipeItem = inputHandler.getRecipeInput(recipe.getInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
        if (recipeItem.isEmpty()) {
            return -1;
        }
        //Calculate the current max based on the input
        currentMax = inputHandler.operationsCanSupport(recipeItem, currentMax);
        if (currentMax <= 0) {
            //If our input can't handle it return that we should be resetting
            return -1;
        }
        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeItem), currentMax);
    }

    @Override
    public boolean isInputValid() {
        return recipe.test(inputHandler.getInput());
    }

    @Override
    protected void finishProcessing(int operations) {
        if (recipeItem.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        inputHandler.use(recipeItem, operations);
        outputHandler.handleOutput(recipe.getOutput(recipeItem), operations);
    }
}