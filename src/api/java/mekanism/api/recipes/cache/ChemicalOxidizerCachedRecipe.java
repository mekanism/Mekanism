package mekanism.api.recipes.cache;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalOxidizerRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.BoxedChemicalOutputHandler;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Base class to help implement handling of oxidizing recipes.
 */
@NothingNullByDefault
public class ChemicalOxidizerCachedRecipe extends CachedRecipe<ChemicalOxidizerRecipe> {

    private final IInputHandler<@NotNull ItemStack> inputHandler;

    private final BoxedChemicalOutputHandler outputHandler;

    private ItemStack recipeInput = ItemStack.EMPTY;
    private BoxedChemicalStack output = BoxedChemicalStack.EMPTY;

    /**
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     * @param inputHandler     Input handler.
     * @param outputHandler    Output handler.
     */
    public ChemicalOxidizerCachedRecipe(ChemicalOxidizerRecipe recipe, BooleanSupplier recheckAllErrors,
                                        IInputHandler<@NotNull ItemStack> inputHandler, BoxedChemicalOutputHandler outputHandler) {
        super(recipe, recheckAllErrors);
        this.inputHandler = Objects.requireNonNull(inputHandler, "Input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
    }

    @Override
    protected void calculateOperationsThisTick(OperationTracker tracker) {
        super.calculateOperationsThisTick(tracker);
        if (tracker.shouldContinueChecking()) {
            recipeInput = inputHandler.getRecipeInput(recipe.getInput());
            if (recipeInput.isEmpty()) {
                //No input, we don't know if the recipe matches or not so treat it as not matching
                tracker.mismatchedRecipe();
            } else {
                //Calculate the current max based on the input
                inputHandler.calculateOperationsCanSupport(tracker, recipeInput);
                if (tracker.shouldContinueChecking()) {
                    output = recipe.getOutput(recipeInput);
                    //Calculate the max based on the space in the output
                    outputHandler.calculateOperationsRoomFor(tracker, output);
                }
            }
        }
    }

    @Override
    public boolean isInputValid() {
        ItemStack input = inputHandler.getInput();
        return !input.isEmpty() && recipe.test(input);
    }

    @Override
    protected void finishProcessing(int operations) {
        //Validate something didn't go horribly wrong
        if (!recipeInput.isEmpty() && !output.isEmpty()) {
            inputHandler.use(recipeInput, operations);
            outputHandler.handleOutput(output, operations);
        }
    }
}