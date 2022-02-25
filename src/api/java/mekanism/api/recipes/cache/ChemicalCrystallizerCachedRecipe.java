package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.inputs.BoxedChemicalInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.world.item.ItemStack;

/**
 * Base class to help implement handling of crystallizing recipes.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ChemicalCrystallizerCachedRecipe extends CachedRecipe<ChemicalCrystallizerRecipe> {

    private final IOutputHandler<@NonNull ItemStack> outputHandler;
    private final BoxedChemicalInputHandler inputHandler;

    private BoxedChemicalStack recipeInput = BoxedChemicalStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;

    /**
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     * @param inputHandler     Input handler.
     * @param outputHandler    Output handler.
     */
    public ChemicalCrystallizerCachedRecipe(ChemicalCrystallizerRecipe recipe, BooleanSupplier recheckAllErrors, BoxedChemicalInputHandler inputHandler,
          IOutputHandler<@NonNull ItemStack> outputHandler) {
        super(recipe, recheckAllErrors);
        this.inputHandler = Objects.requireNonNull(inputHandler, "Input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
    }

    @Override
    protected void calculateOperationsThisTick(OperationTracker tracker) {
        super.calculateOperationsThisTick(tracker);
        if (tracker.shouldContinueChecking()) {
            recipeInput = inputHandler.getRecipeInput(recipe.getInput());
            //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputChemical)
            if (recipeInput.isEmpty()) {
                //No input, we don't know if the recipe matches or not so treat it as not matching
                tracker.mismatchedRecipe();
            } else {
                //Calculate the current max based on the input
                inputHandler.calculateOperationsCanSupport(tracker, recipeInput);
                if (tracker.shouldContinueChecking()) {
                    output = recipe.getOutput(recipeInput);
                    //Calculate the max based on the space in the output
                    outputHandler.calculateOperationsCanSupport(tracker, output);
                }
            }
        }
    }

    @Override
    public boolean isInputValid() {
        BoxedChemicalStack input = inputHandler.getInput();
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