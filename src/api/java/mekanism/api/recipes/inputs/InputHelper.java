package mekanism.api.recipes.inputs;

import java.util.Objects;
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

    /**
     * Wrap an inventory slot into an {@link IInputHandler}.
     *
     * @param slot Slot to wrap.
     */
    public static IInputHandler<@NonNull ItemStack> getInputHandler(IInventorySlot slot) {
        Objects.requireNonNull(slot, "Slot cannot be null.");
        return new IInputHandler<@NonNull ItemStack>() {

            @Nonnull
            @Override
            public ItemStack getInput() {
                return slot.getStack();
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
                    logMismatchedStackSize(slot.shrinkStack(amount, Action.EXECUTE), amount);
                }
            }

            @Override
            public int operationsCanSupport(@Nonnull ItemStack recipeInput, int currentMax, int usageMultiplier) {
                if (currentMax <= 0 || usageMultiplier <= 0) {
                    //Short circuit that if we already can't perform any operations or don't want to use any, just return
                    return currentMax;
                }
                if (recipeInput.isEmpty()) {
                    //If the input is empty that means there is no ingredient that matches
                    return 0;
                }
                //TODO: Simulate?
                return Math.min(getInput().getCount() / (recipeInput.getCount() * usageMultiplier), currentMax);
            }
        };
    }

    /**
     * Wrap a chemical tank into an {@link ILongInputHandler}.
     *
     * @param tank Tank to wrap.
     */
    public static <STACK extends ChemicalStack<?>> ILongInputHandler<@NonNull STACK> getInputHandler(IChemicalTank<?, STACK> tank) {
        Objects.requireNonNull(tank, "Tank cannot be null.");
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
                if (operations == 0 || recipeInput.isEmpty()) {
                    //Just exit if we are somehow here at zero operations
                    // or if something went wrong, this if should never really be true if we got to finishProcessing
                    return;
                }
                STACK inputGas = getInput();
                if (!inputGas.isEmpty()) {
                    long amount = recipeInput.getAmount() * operations;
                    logMismatchedStackSize(tank.shrinkStack(amount, Action.EXECUTE), amount);
                }
            }

            @Override
            public int operationsCanSupport(@Nonnull STACK recipeInput, int currentMax, long usageMultiplier) {
                if (currentMax <= 0 || usageMultiplier <= 0) {
                    //Short circuit that if we already can't perform any operations or don't want to use any, just return
                    return currentMax;
                }
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

    /**
     * Wrap a fluid tank into an {@link IInputHandler}.
     *
     * @param tank Tank to wrap.
     */
    public static IInputHandler<@NonNull FluidStack> getInputHandler(IExtendedFluidTank tank) {
        Objects.requireNonNull(tank, "Tank cannot be null.");
        return new IInputHandler<@NonNull FluidStack>() {

            @Nonnull
            @Override
            public FluidStack getInput() {
                return tank.getFluid();
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
                if (operations == 0 || recipeInput.isEmpty()) {
                    //Just exit if we are somehow here at zero operations
                    // or if something went wrong, this if should never really be true if we got to finishProcessing
                    return;
                }
                FluidStack inputFluid = getInput();
                if (!inputFluid.isEmpty()) {
                    int amount = recipeInput.getAmount() * operations;
                    logMismatchedStackSize(tank.shrinkStack(amount, Action.EXECUTE), amount);
                }
            }

            @Override
            public int operationsCanSupport(@Nonnull FluidStack recipeInput, int currentMax, int usageMultiplier) {
                if (currentMax <= 0 || usageMultiplier <= 0) {
                    //Short circuit that if we already can't perform any operations or don't want to use any, just return
                    return currentMax;
                }
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