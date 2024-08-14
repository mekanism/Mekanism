package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Base class to help implement handling of chemical dissolution recipes.
 */
@NothingNullByDefault
public class ChemicalDissolutionCachedRecipe extends CachedRecipe<ChemicalDissolutionRecipe> {

    private final IOutputHandler<ChemicalStack> outputHandler;
    private final IInputHandler<@NotNull ItemStack> itemInputHandler;
    private final ILongInputHandler<@NotNull ChemicalStack> chemicalInputHandler;
    private final LongSupplier chemicalUsage;
    private long chemicalUsageMultiplier;

    private ItemStack recipeItem = ItemStack.EMPTY;
    private ChemicalStack recipeChemical = ChemicalStack.EMPTY;
    private ChemicalStack output = ChemicalStack.EMPTY;

    /**
     * @param recipe               Recipe.
     * @param recheckAllErrors     Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to
     *                             not do this every tick or if there is no one viewing recipes.
     * @param itemInputHandler     Item input handler.
     * @param chemicalInputHandler Chemical input handler.
     * @param chemicalUsage        Chemical usage multiplier.
     * @param outputHandler        Output handler.
     */
    public ChemicalDissolutionCachedRecipe(ChemicalDissolutionRecipe recipe, BooleanSupplier recheckAllErrors, IInputHandler<@NotNull ItemStack> itemInputHandler,
          ILongInputHandler<@NotNull ChemicalStack> chemicalInputHandler, LongSupplier chemicalUsage, IOutputHandler<ChemicalStack> outputHandler) {
        super(recipe, recheckAllErrors);
        this.itemInputHandler = Objects.requireNonNull(itemInputHandler, "Item input handler cannot be null.");
        this.chemicalInputHandler = Objects.requireNonNull(chemicalInputHandler, "Chemical input handler cannot be null.");
        this.chemicalUsage = Objects.requireNonNull(chemicalUsage, "Chemical usage cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Input handler cannot be null.");
    }

    @Override
    protected void setupVariableValues() {
        chemicalUsageMultiplier = Math.max(chemicalUsage.getAsLong(), 0);
    }

    @Override
    protected void calculateOperationsThisTick(OperationTracker tracker) {
        super.calculateOperationsThisTick(tracker);
        if (tracker.shouldContinueChecking()) {
            recipeItem = itemInputHandler.getRecipeInput(recipe.getItemInput());
            //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
            if (recipeItem.isEmpty()) {
                //No input, we don't know if the recipe matches or not so treat it as not matching
                tracker.mismatchedRecipe();
            } else {
                //Now check the chemical input
                recipeChemical = chemicalInputHandler.getRecipeInput(recipe.getChemicalInput());
                //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputChemical)
                if (recipeChemical.isEmpty()) {
                    //TODO: Allow processing when secondary chemical is empty if the usage multiplier is zero?
                    //Note: we don't force reset based on secondary per tick usages
                    tracker.updateOperations(0);
                    if (!tracker.shouldContinueChecking()) {
                        //If we shouldn't continue checking exit, otherwise see if there is an error with the item
                        // though due to not having a chemical we won't be able to check if there is errors with the output
                        return;
                    }
                }
                //Calculate the current max based on the item input
                itemInputHandler.calculateOperationsCanSupport(tracker, recipeItem);
                if (!recipeChemical.isEmpty() && tracker.shouldContinueChecking()) {
                    //Calculate the current max based on the chemical input, and the given usage amount
                    chemicalInputHandler.calculateOperationsCanSupport(tracker, recipeChemical, chemicalUsageMultiplier);
                    if (tracker.shouldContinueChecking()) {
                        output = recipe.getOutput(recipeItem, recipeChemical);
                        //Calculate the max based on the space in the output
                        outputHandler.calculateOperationsCanSupport(tracker, output);
                    }
                }
            }
        }
    }

    @Override
    public boolean isInputValid() {
        ItemStack itemInput = itemInputHandler.getInput();
        if (!itemInput.isEmpty()) {
            ChemicalStack chemicalStack = chemicalInputHandler.getInput();
            //Ensure that we check that we have enough for that the recipe matches *and* also that we have enough for how much we need to use
            if (!chemicalStack.isEmpty() && recipe.test(itemInput, chemicalStack)) {
                ChemicalStack recipeChemical = chemicalInputHandler.getRecipeInput(recipe.getChemicalInput());
                return !recipeChemical.isEmpty() && chemicalStack.getAmount() >= recipeChemical.getAmount();
            }
        }
        return false;
    }

    @Override
    protected void useResources(int operations) {
        super.useResources(operations);
        if (chemicalUsageMultiplier <= 0) {
            //We don't need to use the chemical
            return;
        } else if (recipeChemical.isEmpty()) {
            //Something went wrong, this if should never really be true if we are in useResources
            return;
        }
        //Note: We should have enough because of the getOperationsThisTick call to reduce it based on amounts
        chemicalInputHandler.use(recipeChemical, operations * chemicalUsageMultiplier);
    }

    @Override
    protected void finishProcessing(int operations) {
        //Validate something didn't go horribly wrong
        if (!recipeItem.isEmpty() && !recipeChemical.isEmpty() && !output.isEmpty()) {
            itemInputHandler.use(recipeItem, operations);
            if (chemicalUsageMultiplier > 0) {
                chemicalInputHandler.use(recipeChemical, operations * chemicalUsageMultiplier);
            }
            outputHandler.handleOutput(output, operations);
        }
    }
}