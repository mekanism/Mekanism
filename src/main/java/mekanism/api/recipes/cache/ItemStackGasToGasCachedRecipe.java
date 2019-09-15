package mekanism.api.recipes.cache;

import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemStackGasToGasCachedRecipe extends CachedRecipe<ItemStackGasToGasRecipe> {

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final Supplier<@NonNull ItemStack> inputStack;
    private final Supplier<@NonNull GasTank> inputTank;
    private final IntSupplier gasUsage;

    public ItemStackGasToGasCachedRecipe(ItemStackGasToGasRecipe recipe, Supplier<@NonNull ItemStack> inputStack, Supplier<@NonNull GasTank> inputTank,
          IntSupplier gasUsage, IOutputHandler<@NonNull GasStack> outputHandler) {
        super(recipe);
        this.inputStack = inputStack;
        this.inputTank = inputTank;
        this.gasUsage = gasUsage;
        this.outputHandler = outputHandler;
    }

    @Nonnull
    private ItemStack getItemInput() {
        return inputStack.get();
    }

    @Nonnull
    private GasTank getGasTank() {
        return inputTank.get();
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
        ItemStack inputItem = getItemInput();
        if (inputItem.isEmpty()) {
            return 0;
        }
        ItemStack recipeItem = recipe.getItemInput().getMatchingInstance(inputItem);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
        if (recipeItem.isEmpty()) {
            return 0;
        }

        //Now check the gas input
        GasStack inputGas = getGasTank().getGas();
        if (inputGas == null || inputGas.amount == 0) {
            return 0;
        }
        GasStack recipeGas = recipe.getGasInput().getMatchingInstance(inputGas);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
        if (recipeGas == null || recipeGas.amount == 0) {
            return 0;
        }

        //Calculate the current max based on how much item input we have to what is needed, capping at what we are told to use as a max
        currentMax = Math.min(inputItem.getCount() / recipeItem.getCount(), currentMax);

        //Calculate the current max based on how much gas input we have to what is needed, capping at what we are told to use as a max
        //NOTE: We multiply the required gas amount by our gas usage amount
        //TODO: Should we be multiplying this by gas usage or somehow transition it to a new system
        currentMax = Math.min(inputGas.amount / (recipeGas.amount * getGasUsage()), currentMax);

        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeItem, recipeGas), currentMax);
    }

    @Override
    public boolean isInputValid() {
        GasTank gasTank = getGasTank();
        GasStack gas = gasTank.getGas();
        //Ensure that we check that we have enough for that the recipe matches *and* also that we have enough for how much we need to use
        return gas != null && recipe.test(getItemInput(), gas) && gasTank.getStored() >= gas.amount * getGasUsage();
    }

    @Override
    protected void useResources(int operations) {
        super.useResources(operations);
        GasTank gasTank = getGasTank();
        GasStack gas = gasTank.getGas();
        if (gas != null && gas.amount > 0) {
            gasTank.draw(gas.amount * getGasUsage(), true);
        }
        //TODO: Else throw some error? It really should already have the needed amount due to the hasResourceForTick call
        // but it may make sense to check anyways
    }

    @Override
    protected void finishProcessing(int operations) {
        //TODO: Cache this stuff from when getOperationsThisTick was called? This is especially important as due to the useResources
        // our gas gets used each tick so we might have finished using it all and won't be able to reference it for our getOutput call
        ItemStack inputItem = getItemInput();
        if (inputItem.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        ItemStack recipeItem = recipe.getItemInput().getMatchingInstance(inputItem);
        if (recipeItem.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }

        //Now check the gas input
        GasStack inputGas = getGasTank().getGas();
        if (inputGas == null || inputGas.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        GasStack recipeGas = recipe.getGasInput().getMatchingInstance(inputGas);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
        if (recipeGas == null || recipeGas.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        outputHandler.handleOutput(recipe.getOutput(recipeItem, recipeGas), operations);
    }
}