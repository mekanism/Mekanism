package mekanism.api.recipes.cache;

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

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ChemicalDissolutionCachedRecipe extends CachedRecipe<ChemicalDissolutionRecipe> {

    private final BoxedChemicalOutputHandler outputHandler;
    private final IInputHandler<@NonNull ItemStack> itemInputHandler;
    private final ILongInputHandler<@NonNull GasStack> gasInputHandler;
    private final LongSupplier gasUsage;

    public ChemicalDissolutionCachedRecipe(ChemicalDissolutionRecipe recipe, IInputHandler<@NonNull ItemStack> itemInputHandler,
          ILongInputHandler<@NonNull GasStack> chemicalInputHandler, LongSupplier gasUsage, BoxedChemicalOutputHandler outputHandler) {
        super(recipe);
        this.itemInputHandler = itemInputHandler;
        this.gasInputHandler = chemicalInputHandler;
        this.gasUsage = gasUsage;
        this.outputHandler = outputHandler;
    }

    private long getGasUsage() {
        return gasUsage.getAsLong();
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
        //Now check the gas input
        GasStack recipeGas = gasInputHandler.getRecipeInput(recipe.getGasInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
        if (recipeGas.isEmpty()) {
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
        //Calculate the current max based on the gas input, and the given usage amount
        currentMax = gasInputHandler.operationsCanSupport(recipe.getGasInput(), currentMax, getGasUsage());
        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeItem, recipeGas), currentMax);
    }

    @Override
    public boolean isInputValid() {
        GasStack gasStack = gasInputHandler.getInput();
        //Ensure that we check that we have enough for that the recipe matches *and* also that we have enough for how much we need to use
        if (!gasStack.isEmpty() && recipe.test(itemInputHandler.getInput(), gasStack)) {
            GasStack recipeGas = gasInputHandler.getRecipeInput(recipe.getGasInput());
            //TODO: Decide how to best handle usage, given technically the input is still valid regardless of extra usage
            // we just can't process it yet
            return !recipeGas.isEmpty() && gasStack.getAmount() >= recipeGas.getAmount();// * getGasUsage();
        }
        return false;
    }

    @Override
    protected void useResources(int operations) {
        super.useResources(operations);
        GasStack recipeGas = gasInputHandler.getRecipeInput(recipe.getGasInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
        if (recipeGas.isEmpty()) {
            //Something went wrong, this if should never really be true if we are in useResources
            return;
        }
        //TODO: Verify we actually have enough and should be passing operations like this?
        gasInputHandler.use(recipeGas, operations * getGasUsage());
        //TODO: Else throw some error? It really should already have the needed amount due to the hasResourceForTick call
        // but it may make sense to check anyways
    }

    @Override
    protected void finishProcessing(int operations) {
        //TODO - Performance: Eventually we should look into caching this stuff from when getOperationsThisTick was called?
        // This is especially important as due to the useResources our gas gets used each tick so we might have finished using
        // it all and won't be able to reference it for our getOutput call
        ItemStack recipeItem = itemInputHandler.getRecipeInput(recipe.getItemInput());
        if (recipeItem.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        //Now check the gas input
        GasStack recipeGas = gasInputHandler.getRecipeInput(recipe.getGasInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
        if (recipeGas.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        itemInputHandler.use(recipeItem, operations);
        gasInputHandler.use(recipeGas, operations);
        outputHandler.handleOutput(recipe.getOutput(recipeItem, recipeGas), operations);
    }
}