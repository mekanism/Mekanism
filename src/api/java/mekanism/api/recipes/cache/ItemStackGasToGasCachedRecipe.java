package mekanism.api.recipes.cache;

import java.util.function.IntSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemStackGasToGasCachedRecipe extends CachedRecipe<ItemStackGasToGasRecipe> {

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final IInputHandler<@NonNull ItemStack> itemInputHandler;
    private final IInputHandler<@NonNull GasStack> gasInputHandler;
    private final IntSupplier gasUsage;

    public ItemStackGasToGasCachedRecipe(ItemStackGasToGasRecipe recipe, IInputHandler<@NonNull ItemStack> itemInputHandler,
          IInputHandler<@NonNull GasStack> gasInputHandler, IntSupplier gasUsage, IOutputHandler<@NonNull GasStack> outputHandler) {
        super(recipe);
        this.itemInputHandler = itemInputHandler;
        this.gasInputHandler = gasInputHandler;
        this.gasUsage = gasUsage;
        this.outputHandler = outputHandler;
    }

    private int getGasUsage() {
        return gasUsage.getAsInt();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax == 0) {
            //If our parent checks show we can't operate then return so
            return 0;
        }
        //TODO: This input getting, is only really needed for getting the output
        ItemStack recipeItem = itemInputHandler.getRecipeInput(recipe.getItemInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
        if (recipeItem.isEmpty()) {
            return 0;
        }

        //Now check the gas input
        GasStack recipeGas = gasInputHandler.getRecipeInput(recipe.getGasInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
        if (recipeGas.isEmpty()) {
            return 0;
        }

        //Calculate the current max based on the item input
        currentMax = itemInputHandler.operationsCanSupport(recipe.getItemInput(), currentMax);

        //Calculate the current max based on the gas input, and the given usage amount
        currentMax = gasInputHandler.operationsCanSupport(recipe.getGasInput(), currentMax, getGasUsage());

        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeItem, recipeGas), currentMax);
    }

    @Override
    public boolean isInputValid() {
        GasStack gas = gasInputHandler.getInput();
        //Ensure that we check that we have enough for that the recipe matches *and* also that we have enough for how much we need to use
        if (!gas.isEmpty() && recipe.test(itemInputHandler.getInput(), gas)) {
            GasStack recipeGas = gasInputHandler.getRecipeInput(recipe.getGasInput());
            //TODO: Decide how to best handle usage, given technically the input is still valid regardless of extra usage
            // we just can't process it yet
            return !recipeGas.isEmpty() && gas.getAmount() >= recipeGas.getAmount();// * getGasUsage();
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
        //TODO: Cache this stuff from when getOperationsThisTick was called? This is especially important as due to the useResources
        // our gas gets used each tick so we might have finished using it all and won't be able to reference it for our getOutput call
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
        //Note: We already extracted our gas so don't need to do so again here
        //gasInputHandler.use(recipeGas, operations);
        outputHandler.handleOutput(recipe.getOutput(recipeItem, recipeGas), operations);
    }
}