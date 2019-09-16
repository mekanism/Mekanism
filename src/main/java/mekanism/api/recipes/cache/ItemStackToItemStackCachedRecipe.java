package mekanism.api.recipes.cache;

import java.util.function.Supplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;

//TODO: Look into making some things have a common super class, such as all the ones that have an ItemStack as an input
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemStackToItemStackCachedRecipe extends CachedRecipe<ItemStackToItemStackRecipe> {

    private final IOutputHandler<@NonNull ItemStack> outputHandler;
    private final Supplier<@NonNull ItemStack> inputStack;

    public ItemStackToItemStackCachedRecipe(ItemStackToItemStackRecipe recipe, Supplier<@NonNull ItemStack> inputStack, IOutputHandler<@NonNull ItemStack> outputHandler) {
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
        ItemStack recipeItem = recipe.getInput().getMatchingInstance(inputItem);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
        if (recipeItem.isEmpty()) {
            return 0;
        }

        //Calculate the current max based on how much item input we have to what is needed, capping at what we are told to use as a max
        currentMax = Math.min(inputItem.getCount() / recipeItem.getCount(), currentMax);

        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeItem), currentMax);
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
        ItemStack recipeItem = recipe.getInput().getMatchingInstance(inputItem);
        if (recipeItem.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        //TODO: Should this be done in some other way than shrink, such as via an IItemHandler, 1.14
        inputItem.shrink(recipeItem.getCount() * operations);
        outputHandler.handleOutput(recipe.getOutput(recipeItem), operations);
    }
}