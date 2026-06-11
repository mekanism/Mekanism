package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.ItemStackChemicalToObjectRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/// Base class to help implement handling of item chemical to object recipes. Unlike [TwoInputCachedRecipe] this variant has constant chemical usage.
///
/// @since 10.7.0
public class ItemStackConstantChemicalToObjectCachedRecipe<OUTPUT, RECIPE extends ItemStackChemicalToObjectRecipe<OUTPUT>> extends CachedRecipe<RECIPE> {

    private final IOutputHandler<OUTPUT> outputHandler;
    private final IInputHandler<Item, ItemStack> itemInputHandler;
    private final IInputHandler<Chemical, ChemicalStack> chemicalInputHandler;
    private final ChemicalUsageMultiplier chemicalUsage;
    private final IntConsumer chemicalUsedSoFarChanged;
    private int chemicalUsageMultiplier;
    private int chemicalUsedSoFar;

    private ItemStack recipeItem = ItemStack.EMPTY;
    private ChemicalStack recipeChemical = ChemicalStack.EMPTY;
    //Note: Shouldn't be null in places it is actually used, but we mark it as nullable, so we don't have to initialize it
    @Nullable
    private OUTPUT output;

    /// @param recipe                   Recipe.
    /// @param recheckAllErrors         Returns `true` if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to
    /// to not do this every tick or if there is no one viewing recipes.
    /// @param itemInputHandler         Item input handler.
    /// @param chemicalInputHandler     Chemical input handler.
    /// @param chemicalUsage            Chemical usage multiplier.
    /// @param chemicalUsedSoFarChanged Called when the number chemical usage so far changes.
    /// @param outputHandler            Output handler.
    public ItemStackConstantChemicalToObjectCachedRecipe(RECIPE recipe, BooleanSupplier recheckAllErrors, IInputHandler<Item, ItemStack> itemInputHandler,
          IInputHandler<Chemical, ChemicalStack> chemicalInputHandler, ChemicalUsageMultiplier chemicalUsage, IntConsumer chemicalUsedSoFarChanged,
          IOutputHandler<OUTPUT> outputHandler) {
        super(recipe, recheckAllErrors);
        this.itemInputHandler = Objects.requireNonNull(itemInputHandler, "Item input handler cannot be null.");
        this.chemicalInputHandler = Objects.requireNonNull(chemicalInputHandler, "Chemical input handler cannot be null.");
        this.chemicalUsage = Objects.requireNonNull(chemicalUsage, "Chemical usage cannot be null.");
        this.chemicalUsedSoFarChanged = Objects.requireNonNull(chemicalUsedSoFarChanged, "Chemical used so far changed handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
    }

    /// Sets the amount of chemical that have been used so far. This is used to allow [CachedRecipe] holders to persist and load recipe progress.
    ///
    /// @param chemicalUsedSoFar Amount of chemical that has been used so far.
    public void loadSavedUsageSoFar(int chemicalUsedSoFar) {
        if (chemicalUsedSoFar > 0) {
            this.chemicalUsedSoFar = chemicalUsedSoFar;
        }
    }

    @Override
    protected void setupVariableValues() {
        chemicalUsageMultiplier = Math.max(chemicalUsage.getToUse(chemicalUsedSoFar, getOperatingTicks()), 0);
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
                return !recipeChemical.isEmpty() && chemicalStack.amount() >= recipeChemical.amount();
            }
        }
        return false;
    }

    @Override
    protected boolean useResources(int operations, TransactionContext transaction) {
        if (!super.useResources(operations, transaction)) {
            return false;
        }
        //Note: We should have enough because of the getOperationsThisTick call to reduce it based on amounts
        int toUse = operations * chemicalUsageMultiplier;
        if (!chemicalInputHandler.use(recipeChemical, toUse, transaction)) {
            return false;
        }
        if (toUse > 0) {
            chemicalUsedSoFar += toUse;
            chemicalUsedSoFarChanged.accept(chemicalUsedSoFar);
        }
        return true;
    }

    @Override
    protected void resetCache() {
        super.resetCache();
        chemicalUsedSoFar = 0;
        chemicalUsedSoFarChanged.accept(chemicalUsedSoFar);
    }

    @Override
    protected boolean finishProcessing(int operations, TransactionContext transaction) {
        return itemInputHandler.use(recipeItem, operations, transaction) &&
               //Note: If chemicalUsageMultiplier is zero, this use call will return true
               chemicalInputHandler.use(recipeChemical, operations * chemicalUsageMultiplier, transaction) &&
               outputHandler.handleOutput(output, operations, transaction);
    }

    @FunctionalInterface
    public interface ChemicalUsageMultiplier {

        int getToUse(int usedSoFar, int operatingTicks);

        static ChemicalUsageMultiplier constantUse(IntSupplier baseTotalUsage, IntSupplier ticksRequired) {
            return (usedSoFar, operatingTicks) -> {
                int baseRemaining = baseTotalUsage.getAsInt() - usedSoFar;
                int remainingTicks = ticksRequired.getAsInt() - operatingTicks;
                if (baseRemaining < remainingTicks) {
                    //If we already used more than we would need to use (due to removing speed upgrades or adding gas upgrades)
                    // then just don't use any gas this tick
                    return 0;
                } else if (baseRemaining == remainingTicks) {
                    return 1;
                }
                return Math.max(MathUtils.clampToInt(baseRemaining / (double) remainingTicks), 0);
            };
        }
    }

    /// @param recipe                   Recipe.
    /// @param recheckAllErrors         Returns `true` if processing should be continued even if an error is hit in order to gather all the errors. It is recommended
    ///                                 to not do this every tick or if there is no one viewing recipes.
    /// @param itemInputHandler         Item input handler.
    /// @param chemicalInputHandler     Chemical input handler.
    /// @param chemicalUsage            Chemical usage multiplier.
    /// @param chemicalUsedSoFarChanged Called when the number chemical usage so far changes.
    /// @param outputHandler            Output handler.
    ///
    /// @since 10.8.0
    public static <OUTPUT, RECIPE extends ItemStackChemicalToObjectRecipe<OUTPUT>> ItemStackConstantChemicalToObjectCachedRecipe<OUTPUT, RECIPE> create(RECIPE recipe,
          BooleanSupplier recheckAllErrors, IInputHandler<Item, ItemStack> itemInputHandler, IInputHandler<Chemical, ChemicalStack> chemicalInputHandler,
          ChemicalUsageMultiplier chemicalUsage, IntConsumer chemicalUsedSoFarChanged, IOutputHandler<OUTPUT> outputHandler) {
        return new ItemStackConstantChemicalToObjectCachedRecipe<>(recipe, recheckAllErrors, itemInputHandler, chemicalInputHandler, chemicalUsage, chemicalUsedSoFarChanged,
              outputHandler);
    }
}