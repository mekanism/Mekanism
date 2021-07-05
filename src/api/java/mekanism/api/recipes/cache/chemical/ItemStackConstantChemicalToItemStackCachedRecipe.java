package mekanism.api.recipes.cache.chemical;

import java.util.Objects;
import java.util.function.LongSupplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.item.ItemStack;

/**
 * Base class to help implement handling of chemical chemical to chemical recipes. Unlike {@link ItemStackChemicalToItemStackCachedRecipe} this variant has constant
 * chemical usage.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemStackConstantChemicalToItemStackCachedRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT>>
      extends CachedRecipe<RECIPE> {

    private final IOutputHandler<@NonNull ItemStack> outputHandler;
    private final IInputHandler<@NonNull ItemStack> itemInputHandler;
    private final ILongInputHandler<@NonNull STACK> chemicalInputHandler;
    private final LongSupplier chemicalUsage;
    private long chemicalUsageMultiplier;

    private ItemStack recipeItem = ItemStack.EMPTY;
    @Nullable//Note: Shouldn't be null in places it is actually used, but we mark it as nullable so we don't have to initialize it
    private STACK recipeChemical;

    /**
     * @param recipe               Recipe.
     * @param itemInputHandler     Item input handler.
     * @param chemicalInputHandler Chemical input handler.
     * @param chemicalUsage        Chemical usage multiplier, must return values of at least one.
     * @param outputHandler        Output handler.
     */
    public ItemStackConstantChemicalToItemStackCachedRecipe(RECIPE recipe, IInputHandler<@NonNull ItemStack> itemInputHandler,
          ILongInputHandler<@NonNull STACK> chemicalInputHandler, LongSupplier chemicalUsage, IOutputHandler<@NonNull ItemStack> outputHandler) {
        super(recipe);
        this.itemInputHandler = Objects.requireNonNull(itemInputHandler, "Item input handler cannot be null.");
        this.chemicalInputHandler = Objects.requireNonNull(chemicalInputHandler, "Chemical input handler cannot be null.");
        this.chemicalUsage = Objects.requireNonNull(chemicalUsage, "Chemical usage cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
    }

    @Override
    protected void setupVariableValues() {
        chemicalUsageMultiplier = chemicalUsage.getAsLong();
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
        //Now check the chemical input
        recipeChemical = chemicalInputHandler.getRecipeInput(recipe.getChemicalInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputChemical)
        if (recipeChemical.isEmpty()) {
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
        //Calculate the current max based on the chemical input, and the given usage amount
        currentMax = chemicalInputHandler.operationsCanSupport(recipeChemical, currentMax, chemicalUsageMultiplier);
        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeItem, recipeChemical), currentMax);
    }

    @Override
    public boolean isInputValid() {
        STACK chemicalStack = chemicalInputHandler.getInput();
        //Ensure that we check that we have enough for that the recipe matches *and* also that we have enough for how much we need to use
        if (!chemicalStack.isEmpty() && recipe.test(itemInputHandler.getInput(), chemicalStack)) {
            STACK recipeChemical = chemicalInputHandler.getRecipeInput(recipe.getChemicalInput());
            return !recipeChemical.isEmpty() && chemicalStack.getAmount() >= recipeChemical.getAmount();
        }
        return false;
    }

    @Override
    protected void useResources(int operations) {
        super.useResources(operations);
        if (recipeChemical == null || recipeChemical.isEmpty()) {
            //Something went wrong, this if should never really be true if we are in useResources
            return;
        }
        //Note: We should have enough because of the getOperationsThisTick call to reduce it based on amounts
        chemicalInputHandler.use(recipeChemical, operations * chemicalUsageMultiplier);
    }

    @Override
    protected void finishProcessing(int operations) {
        if (recipeItem.isEmpty() || recipeChemical == null || recipeChemical.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        itemInputHandler.use(recipeItem, operations);
        chemicalInputHandler.use(recipeChemical, operations);
        outputHandler.handleOutput(recipe.getOutput(recipeItem, recipeChemical), operations);
    }
}