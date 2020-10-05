package mekanism.api.recipes.inputs;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

@ParametersAreNonnullByDefault
public class InputHelper {

    private InputHelper() {
    }

    public static IInputHandler<@NonNull ItemStack> getInputHandler(IInventorySlot inventorySlot) {
        return new IInputHandler<@NonNull ItemStack>() {

            @Nonnull
            @Override
            public ItemStack getInput() {
                return inventorySlot.getStack();
            }

            @Nonnull
            @Override
            public ItemStack getRecipeInput(InputIngredient<@NonNull ItemStack> recipeIngredient) {
                ItemStack input = getInput();
                if (input.isEmpty()) {
                    //All recipes currently require that we have an input. If we don't then return that we failed
                    return ItemStack.EMPTY;
                }
                return recipeIngredient.getMatchingInstance(input);
            }

            @Override
            public void use(@Nonnull ItemStack recipeInput, int operations) {
                if (operations == 0) {
                    //Just exit if we are somehow here at zero operations
                    return;
                }
                if (!recipeInput.isEmpty()) {
                    int amount = recipeInput.getCount() * operations;
                    logMismatchedStackSize(inventorySlot.shrinkStack(amount, Action.EXECUTE), amount);
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

    public static <STACK extends ChemicalStack<?>> ILongInputHandler<@NonNull STACK> getInputHandler(IChemicalTank<?, STACK> tank) {
        return new ILongInputHandler<@NonNull STACK>() {

            @Nonnull
            @Override
            public STACK getInput() {
                return tank.getStack();
            }

            @Nonnull
            @Override
            public STACK getRecipeInput(InputIngredient<@NonNull STACK> recipeIngredient) {
                STACK input = getInput();
                if (input.isEmpty()) {
                    //All recipes currently require that we have an input. If we don't then return that we failed
                    return tank.getEmptyStack();
                }
                return recipeIngredient.getMatchingInstance(input);
            }

            @Override
            public void use(@Nonnull STACK recipeInput, long operations) {
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
                    logMismatchedStackSize(tank.shrinkStack(amount, Action.EXECUTE), amount);
                }
            }

            @Override
            public int operationsCanSupport(InputIngredient<@NonNull STACK> recipeIngredient, int currentMax, long usageMultiplier) {
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

    public static IInputHandler<@NonNull FluidStack> getInputHandler(IExtendedFluidTank fluidTank) {
        return new IInputHandler<@NonNull FluidStack>() {

            @Nonnull
            @Override
            public FluidStack getInput() {
                return fluidTank.getFluid();
            }

            @Nonnull
            @Override
            public FluidStack getRecipeInput(InputIngredient<@NonNull FluidStack> recipeIngredient) {
                FluidStack input = getInput();
                if (input.isEmpty()) {
                    //All recipes currently require that we have an input. If we don't then return that we failed
                    return FluidStack.EMPTY;
                }
                return recipeIngredient.getMatchingInstance(input);
            }

            @Override
            public void use(@Nonnull FluidStack recipeInput, int operations) {
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
                    logMismatchedStackSize(fluidTank.shrinkStack(amount, Action.EXECUTE), amount);
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

    private static void logMismatchedStackSize(long actual, long expected) {
        if (expected != actual) {
            MekanismAPI.logger.error("Stack size changed by a different amount ({}) than requested ({}).", actual, expected, new Exception());
        }
    }
}