package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.LongSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.outputs.BoxedChemicalOutputHandler;
import net.minecraft.item.ItemStack;

/**
 * Base class to help implement handling of chemical dissolution recipes.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ChemicalDissolutionCachedRecipe extends CachedRecipe<ChemicalDissolutionRecipe> {

    private final BoxedChemicalOutputHandler outputHandler;
    private final IInputHandler<@NonNull ItemStack> itemInputHandler;
    private final ILongInputHandler<@NonNull GasStack> gasInputHandler;
    private final LongSupplier gasUsage;
    private long gasUsageMultiplier;

    private ItemStack recipeItem = ItemStack.EMPTY;
    private GasStack recipeGas = GasStack.EMPTY;

    /**
     * @param recipe           Recipe.
     * @param itemInputHandler Item input handler.
     * @param gasInputHandler  Chemical input handler.
     * @param gasUsage         Gas usage multiplier.
     * @param outputHandler    Output handler.
     */
    public ChemicalDissolutionCachedRecipe(ChemicalDissolutionRecipe recipe, IInputHandler<@NonNull ItemStack> itemInputHandler,
          ILongInputHandler<@NonNull GasStack> gasInputHandler, LongSupplier gasUsage, BoxedChemicalOutputHandler outputHandler) {
        super(recipe);
        this.itemInputHandler = Objects.requireNonNull(itemInputHandler, "Item input handler cannot be null.");
        this.gasInputHandler = Objects.requireNonNull(gasInputHandler, "Gas input handler cannot be null.");
        this.gasUsage = Objects.requireNonNull(gasUsage, "Gas usage cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Input handler cannot be null.");
    }

    @Override
    protected void setupVariableValues() {
        gasUsageMultiplier = Math.max(gasUsage.getAsLong(), 0);
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax <= 0) {
            //If our parent checks show we can't operate then return so
            return currentMax;
        }
        recipeItem = itemInputHandler.getRecipeInput(recipe.getItemInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
        if (recipeItem.isEmpty()) {
            return -1;
        }
        //Now check the gas input
        recipeGas = gasInputHandler.getRecipeInput(recipe.getGasInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
        if (recipeGas.isEmpty()) {
            //Note: we don't force reset based on secondary per tick usages
            return 0;
        }
        //Calculate the current max based on the item input
        currentMax = itemInputHandler.operationsCanSupport(recipeItem, currentMax);
        if (currentMax <= 0) {
            //If our input can't handle it return that we should be resetting
            //Note: we don't force reset based on secondary per tick usages
            return -1;
        }
        //Calculate the current max based on the gas input, and the given usage amount
        currentMax = gasInputHandler.operationsCanSupport(recipeGas, currentMax, gasUsageMultiplier);
        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeItem, recipeGas), currentMax);
    }

    @Override
    public boolean isInputValid() {
        GasStack gasStack = gasInputHandler.getInput();
        //Ensure that we check that we have enough for that the recipe matches *and* also that we have enough for how much we need to use
        if (!gasStack.isEmpty() && recipe.test(itemInputHandler.getInput(), gasStack)) {
            GasStack recipeGas = gasInputHandler.getRecipeInput(recipe.getGasInput());
            return !recipeGas.isEmpty() && gasStack.getAmount() >= recipeGas.getAmount();
        }
        return false;
    }

    @Override
    protected void useResources(int operations) {
        super.useResources(operations);
        if (gasUsageMultiplier <= 0) {
            //We don't need to use the gas
            return;
        } else if (recipeGas.isEmpty()) {
            //Something went wrong, this if should never really be true if we are in useResources
            return;
        }
        //Note: We should have enough because of the getOperationsThisTick call to reduce it based on amounts
        gasInputHandler.use(recipeGas, operations * gasUsageMultiplier);
    }

    @Override
    protected void finishProcessing(int operations) {
        if (recipeItem.isEmpty() || recipeGas.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        itemInputHandler.use(recipeItem, operations);
        if (gasUsageMultiplier > 0) {
            gasInputHandler.use(recipeGas, operations * gasUsageMultiplier);
        }
        outputHandler.handleOutput(recipe.getOutput(recipeItem, recipeGas), operations);
    }
}