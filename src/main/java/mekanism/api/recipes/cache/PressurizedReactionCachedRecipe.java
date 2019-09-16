package mekanism.api.recipes.cache;

import java.util.function.Supplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.apache.commons.lang3.tuple.Pair;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class PressurizedReactionCachedRecipe extends CachedRecipe<PressurizedReactionRecipe> {

    private final IOutputHandler<@NonNull Pair<@NonNull ItemStack, @NonNull GasStack>> outputHandler;
    private final Supplier<@NonNull ItemStack> inputStack;
    private final Supplier<@NonNull FluidTank> fluidInputTank;
    private final Supplier<@NonNull GasTank> gasInputTank;

    public PressurizedReactionCachedRecipe(PressurizedReactionRecipe recipe, Supplier<@NonNull ItemStack> inputStack, Supplier<@NonNull FluidTank> fluidInputTank,
          Supplier<@NonNull GasTank> gasInputTank, IOutputHandler<@NonNull Pair<@NonNull ItemStack, @NonNull GasStack>> outputHandler) {
        super(recipe);
        this.inputStack = inputStack;
        this.fluidInputTank = fluidInputTank;
        this.gasInputTank = gasInputTank;
        this.outputHandler = outputHandler;
    }

    private ItemStack getItemInput() {
        return inputStack.get();
    }

    private FluidTank getFluidTank() {
        return fluidInputTank.get();
    }

    private GasTank getGasTank() {
        return gasInputTank.get();
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
        ItemStack recipeItem = recipe.getInputSolid().getMatchingInstance(inputItem);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
        if (recipeItem.isEmpty()) {
            return 0;
        }

        //Now check the fluid input
        FluidStack inputFluid = getFluidTank().getFluid();
        if (inputFluid == null || inputFluid.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return 0;
        }
        FluidStack recipeFluid = recipe.getInputFluid().getMatchingInstance(inputFluid);
        if (recipeFluid == null || recipeFluid.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
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
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return 0;
        }

        //Calculate the current max based on how much item input we have to what is needed, capping at what we are told to use as a max
        currentMax = Math.min(inputItem.getCount() / recipeItem.getCount(), currentMax);

        //Calculate the current max based on how much fluid input we have to what is needed, capping at what we are told to use as a max
        currentMax = Math.min(inputFluid.amount / recipeFluid.amount, currentMax);

        //Calculate the current max based on how much gas input we have to what is needed, capping at what we are told to use as a max
        currentMax = Math.min(inputGas.amount / recipeGas.amount, currentMax);

        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeItem, recipeFluid, recipeGas), currentMax);
    }

    @Override
    public boolean isInputValid() {
        GasStack gas = getGasTank().getGas();
        if (gas == null || gas.amount == 0) {
            return false;
        }
        FluidStack fluid = getFluidTank().getFluid();
        if (fluid == null || fluid.amount == 0) {
            return false;
        }
        return recipe.test(getItemInput(), fluid, gas);
    }

    @Override
    protected void finishProcessing(int operations) {
        //TODO: Cache this stuff from when getOperationsThisTick was called?
        ItemStack inputItem = getItemInput();
        if (inputItem.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        ItemStack recipeItem = recipe.getInputSolid().getMatchingInstance(inputItem);
        if (recipeItem.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        FluidStack inputFluid = getFluidTank().getFluid();
        if (inputFluid == null || inputFluid.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        FluidStack recipeFluid = recipe.getInputFluid().getMatchingInstance(inputFluid);
        if (recipeFluid == null || recipeFluid.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }

        //Now check the gas input
        GasStack inputGas = getGasTank().getGas();
        if (inputGas == null || inputGas.amount == 0) {
            return;
        }
        GasStack recipeGas = recipe.getGasInput().getMatchingInstance(inputGas);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
        if (recipeGas == null || recipeGas.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        //TODO: Should this be done in some other way than shrink, such as via an IItemHandler, 1.14
        inputItem.shrink(recipeItem.getCount() * operations);
        getFluidTank().drain(inputFluid.amount * operations, true);
        getGasTank().draw(recipeGas.amount * operations, true);
        outputHandler.handleOutput(recipe.getOutput(recipeItem, recipeFluid, recipeGas), operations);
    }
}