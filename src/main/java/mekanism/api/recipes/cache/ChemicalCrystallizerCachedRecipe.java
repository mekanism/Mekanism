package mekanism.api.recipes.cache;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ChemicalCrystallizerCachedRecipe extends CachedRecipe<ChemicalCrystallizerRecipe> {

    private final IOutputHandler<@NonNull ItemStack> outputHandler;
    private final Supplier<@NonNull GasTank> inputTank;

    public ChemicalCrystallizerCachedRecipe(ChemicalCrystallizerRecipe recipe, Supplier<@NonNull GasTank> inputTank, IOutputHandler<@NonNull ItemStack> outputHandler) {
        super(recipe);
        this.inputTank = inputTank;
        this.outputHandler = outputHandler;
    }

    @Nonnull
    private GasTank getGasTank() {
        return inputTank.get();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax == 0) {
            //If our parent checks show we can't operate then return so
            return 0;
        }
        GasStack inputGas = getGasTank().getGas();
        if (inputGas == null || inputGas.amount == 0) {
            return 0;
        }
        GasStack recipeGas = recipe.getInput().getMatchingInstance(inputGas);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
        if (recipeGas == null || recipeGas.amount == 0) {
            return 0;
        }
        //Calculate the current max based on how much gas input we have to what is needed, capping at what we are told to use as a max
        currentMax = Math.min(inputGas.amount / recipeGas.amount, currentMax);

        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeGas), currentMax);
    }

    @Override
    public boolean isInputValid() {
        GasTank gasTank = getGasTank();
        GasStack gasInput = gasTank.getGas();
        return gasInput != null && recipe.test(gasInput);
    }

    @Override
    protected void finishProcessing(int operations) {
        //TODO: Cache this stuff from when getOperationsThisTick was called?
        GasStack inputGas = getGasTank().getGas();
        if (inputGas == null || inputGas.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        GasStack recipeGas = recipe.getInput().getMatchingInstance(inputGas);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
        if (recipeGas == null || recipeGas.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        getGasTank().draw(recipeGas.amount * operations, true);
        outputHandler.handleOutput(recipe.getOutput(recipeGas), operations);
    }
}