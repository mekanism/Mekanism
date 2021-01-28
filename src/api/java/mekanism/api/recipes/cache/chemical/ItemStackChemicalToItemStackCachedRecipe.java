package mekanism.api.recipes.cache.chemical;

import java.util.function.LongSupplier;
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

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemStackChemicalToItemStackCachedRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT>>
      extends CachedRecipe<RECIPE> {

    private final IOutputHandler<@NonNull ItemStack> outputHandler;
    private final IInputHandler<@NonNull ItemStack> itemInputHandler;
    private final ILongInputHandler<@NonNull STACK> chemicalInputHandler;
    private final LongSupplier chemicalUsage;
    private long chemicalUsageMultiplier;

    public ItemStackChemicalToItemStackCachedRecipe(RECIPE recipe, IInputHandler<@NonNull ItemStack> itemInputHandler,
          ILongInputHandler<@NonNull STACK> chemicalInputHandler, LongSupplier chemicalUsage, IOutputHandler<@NonNull ItemStack> outputHandler) {
        super(recipe);
        this.itemInputHandler = itemInputHandler;
        this.chemicalInputHandler = chemicalInputHandler;
        this.chemicalUsage = chemicalUsage;
        this.outputHandler = outputHandler;
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
        ItemStack recipeItem = itemInputHandler.getRecipeInput(recipe.getItemInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
        if (recipeItem.isEmpty()) {
            return -1;
        }
        //Now check the chemical input
        STACK recipeChemical = chemicalInputHandler.getRecipeInput(recipe.getChemicalInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputChemical)
        if (recipeChemical.isEmpty()) {
            //Note: we don't force reset based on secondary per tick usages
            return 0;
        }
        //Calculate the current max based on the item input
        currentMax = itemInputHandler.operationsCanSupport(recipe.getItemInput(), currentMax);
        if (currentMax <= 0) {
            //If our input can't handle it return that we should be resetting
            //Note: we don't force reset based on secondary per tick usages
            return -1;
        }
        //Calculate the current max based on the chemical input, and the given usage amount
        currentMax = chemicalInputHandler.operationsCanSupport(recipe.getChemicalInput(), currentMax, chemicalUsageMultiplier);
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
        STACK recipeChemical = chemicalInputHandler.getRecipeInput(recipe.getChemicalInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputChemical)
        if (recipeChemical.isEmpty()) {
            //Something went wrong, this if should never really be true if we are in useResources
            return;
        }
        //Note: We should have enough because of the getOperationsThisTick call to reduce it based on amounts
        chemicalInputHandler.use(recipeChemical, operations * chemicalUsageMultiplier);
    }

    @Override
    protected void finishProcessing(int operations) {
        //TODO - Performance: Eventually we should look into caching this stuff from when getOperationsThisTick was called?
        // This is especially important as due to the useResources our chemical gets used each tick so we might have finished using
        // it all and won't be able to reference it for our getOutput call
        ItemStack recipeItem = itemInputHandler.getRecipeInput(recipe.getItemInput());
        if (recipeItem.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        //Now check the chemical input
        STACK recipeChemical = chemicalInputHandler.getRecipeInput(recipe.getChemicalInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputChemical)
        if (recipeChemical.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        itemInputHandler.use(recipeItem, operations);
        chemicalInputHandler.use(recipeChemical, operations);
        outputHandler.handleOutput(recipe.getOutput(recipeItem, recipeChemical), operations);
    }
}