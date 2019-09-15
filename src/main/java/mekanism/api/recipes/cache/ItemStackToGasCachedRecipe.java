package mekanism.api.recipes.cache;

import java.util.function.Supplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemStackToGasCachedRecipe extends CachedRecipe<ItemStackToGasRecipe> {

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final Supplier<@NonNull ItemStack> inputStack;

    public ItemStackToGasCachedRecipe(ItemStackToGasRecipe recipe, Supplier<@NonNull ItemStack> inputStack, IOutputHandler<@NonNull GasStack> outputHandler) {
        super(recipe);
        this.inputStack = inputStack;
        this.outputHandler = outputHandler;
    }

    private ItemStack getInput() {
        return inputStack.get();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax == 0) {
            //If our parent checks show we can't operate then return so
            return 0;
        }
        ItemStack inputItem = getInput();
        if (inputItem.isEmpty()) {
            return 0;
        }
        ItemStack recipeInput = recipe.getInput().getMatchingInstance(inputItem);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
        if (recipeInput.isEmpty()) {
            return 0;
        }
        //Calculate the current max based on how much input we have to what is needed, capping at what we are told to use as a max
        currentMax = Math.min(inputItem.getCount() / recipeInput.getCount(), currentMax);
        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeInput), currentMax);
    }

    @Override
    public boolean isInputValid() {
        return recipe.test(getInput());
    }

    @Override
    protected void finishProcessing(int operations) {
        //TODO: Cache this stuff from when getOperationsThisTick was called?
        ItemStack inputItem = getInput();
        if (inputItem.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        ItemStack recipeInput = recipe.getInput().getMatchingInstance(inputItem);
        if (recipeInput.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        outputHandler.handleOutput(recipe.getOutput(recipeInput), operations);
    }
}