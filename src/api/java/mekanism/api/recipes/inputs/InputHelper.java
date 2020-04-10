package mekanism.api.recipes.inputs;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class InputHelper {

    public static IInputHandler<@NonNull ItemStack> getInputHandler(@Nonnull IInventorySlot inventorySlot) {
        return new IInputHandler<@NonNull ItemStack>() {

            @Override
            public @NonNull ItemStack getInput() {
                return inventorySlot.getStack();
            }

            @Override
            public @NonNull ItemStack getRecipeInput(InputIngredient<@NonNull ItemStack> recipeIngredient) {
                ItemStack input = getInput();
                if (input.isEmpty()) {
                    //All recipes currently require that we have an input. If we don't then return that we failed
                    return ItemStack.EMPTY;
                }
                return recipeIngredient.getMatchingInstance(input);
            }

            @Override
            public void use(@NonNull ItemStack recipeInput, int operations) {
                if (operations == 0) {
                    //Just exit if we are somehow here at zero operations
                    return;
                }
                if (!recipeInput.isEmpty()) {
                    int amount = recipeInput.getCount() * operations;
                    if (inventorySlot.shrinkStack(amount, Action.EXECUTE) != amount) {
                        //TODO: Print error/warning that something went wrong
                    }
                }
            }

            @Override
            public int operationsCanSupport(InputIngredient<@NonNull ItemStack> recipeIngredient, int currentMax, int usageMultiplier) {
                if (currentMax <= 0 || usageMultiplier == 0) {
                    //Short circuit that if we already can't perform any operations or don't want to use any, just return
                    return currentMax;
                }
                ItemStack recipeInput = getRecipeInput(recipeIngredient);
                if (recipeInput.isEmpty()) {
                    //If the input is empty that means there is no ingredient that matches
                    return 0;
                }
                //TODO: Simulate?
                return Math.min(getInput().getCount() / (recipeInput.getCount() * usageMultiplier), currentMax);
            }
        };
    }

    //TODO: IGasHandler?
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> IInputHandler<@NonNull STACK> getInputHandler(@Nonnull IChemicalTank<CHEMICAL, STACK> tank) {
        return new IInputHandler<@NonNull STACK>() {

            @Override
            public @NonNull STACK getInput() {
                return tank.getStack();
            }

            @Override
            public @NonNull STACK getRecipeInput(InputIngredient<@NonNull STACK> recipeIngredient) {
                STACK input = getInput();
                //TODO: Make this be an is empty check, and return empty instead of null
                if (input.isEmpty()) {
                    //All recipes currently require that we have an input. If we don't then return that we failed
                    return tank.getEmptyStack();
                }
                return recipeIngredient.getMatchingInstance(input);
            }

            @Override
            public void use(@NonNull STACK recipeInput, int operations) {
                if (operations == 0) {
                    //Just exit if we are somehow here at zero operations
                    return;
                }
                if (recipeInput.isEmpty()) {
                    //Something went wrong, this if should never really be true if we got to finishProcessing
                    return;
                }
                STACK inputGas = getInput();
                if (!inputGas.isEmpty()) {
                    long amount = recipeInput.getAmount() * operations;
                    if (tank.shrinkStack(amount, Action.EXECUTE) != amount) {
                        //TODO: Print error/warning that something went wrong
                    }
                }
            }

            @Override
            public int operationsCanSupport(InputIngredient<@NonNull STACK> recipeIngredient, int currentMax, int usageMultiplier) {
                if (currentMax <= 0 || usageMultiplier == 0) {
                    //Short circuit that if we already can't perform any operations or don't want to use any, just return
                    return currentMax;
                }
                STACK recipeInput = getRecipeInput(recipeIngredient);
                //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
                if (recipeInput.isEmpty()) {
                    //If the input is empty that means there is no ingredient that matches
                    return 0;
                }
                //TODO: Simulate the drain?
                return Math.min(MathUtils.clampToInt(getInput().getAmount() / (recipeInput.getAmount() * usageMultiplier)), currentMax);
            }
        };
    }

    public static IInputHandler<@NonNull FluidStack> getInputHandler(@Nonnull IExtendedFluidTank fluidTank) {
        return new IInputHandler<@NonNull FluidStack>() {

            @Override
            public @NonNull FluidStack getInput() {
                return fluidTank.getFluid();
            }

            @Override
            public @NonNull FluidStack getRecipeInput(InputIngredient<@NonNull FluidStack> recipeIngredient) {
                FluidStack input = getInput();
                if (input.isEmpty()) {
                    //All recipes currently require that we have an input. If we don't then return that we failed
                    return FluidStack.EMPTY;
                }
                return recipeIngredient.getMatchingInstance(input);
            }

            @Override
            public void use(@NonNull FluidStack recipeInput, int operations) {
                if (operations == 0) {
                    //Just exit if we are somehow here at zero operations
                    return;
                }
                if (recipeInput.isEmpty()) {
                    //Something went wrong, this if should never really be true if we got to finishProcessing
                    return;
                }
                FluidStack inputFluid = getInput();
                if (!inputFluid.isEmpty()) {
                    int amount = recipeInput.getAmount() * operations;
                    if (fluidTank.shrinkStack(amount, Action.EXECUTE) != amount) {
                        //TODO: Print error/warning that something went wrong
                    }
                }
            }

            @Override
            public int operationsCanSupport(InputIngredient<@NonNull FluidStack> recipeIngredient, int currentMax, int usageMultiplier) {
                if (currentMax <= 0 || usageMultiplier == 0) {
                    //Short circuit that if we already can't perform any operations or don't want to use any, just return
                    return currentMax;
                }
                FluidStack recipeInput = getRecipeInput(recipeIngredient);
                //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputFluid)
                if (recipeInput.isEmpty()) {
                    //If the input is empty that means there is no ingredient that matches
                    return 0;
                }
                //TODO: Simulate the drain?
                return Math.min(getInput().getAmount() / (recipeInput.getAmount() * usageMultiplier), currentMax);
            }
        };
    }
}