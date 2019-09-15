package mekanism.api.recipes.cache;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class CombinerCachedRecipe extends CachedRecipe<CombinerRecipe> {

    private final IOutputHandler<@NonNull ItemStack> outputHandler;
    private final Supplier<@NonNull ItemStack> inputStack;
    private final Supplier<@NonNull ItemStack> extraStack;

    public CombinerCachedRecipe(CombinerRecipe recipe, Supplier<@NonNull ItemStack> inputStack, Supplier<@NonNull ItemStack> extraStack,
          IOutputHandler<@NonNull ItemStack> outputHandler) {
        super(recipe);
        this.inputStack = inputStack;
        this.extraStack = extraStack;
        this.outputHandler = outputHandler;
    }

    @Nonnull
    private ItemStack getMainInput() {
        return inputStack.get();
    }

    @Nonnull
    private ItemStack getExtraInput() {
        return extraStack.get();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax == 0) {
            //If our parent checks show we can't operate then return so
            return 0;
        }
        ItemStack inputMain = getMainInput();
        if (inputMain.isEmpty()) {
            return 0;
        }
        ItemStack recipeMain = recipe.getMainInput().getMatchingInstance(inputMain);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputMain, inputExtra)
        if (recipeMain.isEmpty()) {
            return 0;
        }

        //Now check the extra input
        ItemStack inputExtra = getExtraInput();
        if (inputExtra.isEmpty()) {
            return 0;
        }
        ItemStack recipeExtra = recipe.getExtraInput().getMatchingInstance(inputExtra);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputMain, inputExtra)
        if (recipeExtra.isEmpty()) {
            return 0;
        }

        //Calculate the current max based on how much main item input we have to what is needed, capping at what we are told to use as a max
        currentMax = Math.min(inputMain.getCount() / recipeMain.getCount(), currentMax);

        //Calculate the current max based on how much extra item input we have to what is needed, capping at what we are told to use as a max
        currentMax = Math.min(inputExtra.getCount() / recipeExtra.getCount(), currentMax);

        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeMain, recipeExtra), currentMax);
    }

    @Override
    public boolean isInputValid() {
        return recipe.test(getMainInput(), getExtraInput());
    }

    @Override
    protected void finishProcessing(int operations) {
        ItemStack inputMain = getMainInput();
        if (inputMain.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        ItemStack recipeMain = recipe.getMainInput().getMatchingInstance(inputMain);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputMain, inputExtra)
        if (recipeMain.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }

        //Now check the extra input
        ItemStack inputExtra = getExtraInput();
        if (inputExtra.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        ItemStack recipeExtra = recipe.getExtraInput().getMatchingInstance(inputExtra);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputMain, inputExtra)
        if (recipeExtra.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }

        outputHandler.handleOutput(recipe.getOutput(recipeMain, recipeExtra), operations);
    }
}