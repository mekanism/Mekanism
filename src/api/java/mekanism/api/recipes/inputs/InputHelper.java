package mekanism.api.recipes.inputs;

import com.google.common.primitives.Ints;
import java.util.Objects;
import mekanism.api.AutomationType;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.ingredients.InputIngredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class InputHelper {

    private InputHelper() {
    }

    /**
     * Wrap an inventory slot into an {@link IInputHandler}.
     *
     * @param slot           Slot to wrap.
     * @param notEnoughError The error to apply if the input does not have enough stored for the recipe to be able to perform any operations.
     */
    public static IInputHandler<Item, @NotNull ItemStack> getInputHandler(IInventorySlot slot, RecipeError notEnoughError) {
        Objects.requireNonNull(slot, "Slot cannot be null.");
        Objects.requireNonNull(notEnoughError, "Not enough input error cannot be null.");
        //TODO - 26.1: Make a ResourceInputHandler class to simplify the implementations across resource types
        return new IInputHandler<>() {

            @Override
            public ItemStack getInput() {
                return slot.getResource().toStack(slot.amount());
            }

            @Override
            public ItemStack getRecipeInput(InputIngredient<Item, @NotNull ItemStack> recipeIngredient) {
                ItemStack input = getInput();
                if (input.isEmpty()) {
                    //All recipes currently require that we have an input. If we don't then return that we failed
                    return ItemStack.EMPTY;
                }
                return recipeIngredient.getMatchingInstance(input);
            }

            @Override
            public void use(ItemStack recipeInput, int operations, TransactionContext transaction) {
                if (operations == 0) {
                    //Just exit if we are somehow here at zero operations
                    return;
                }
                //TODO - 26.1: Why do input tanks check the current stack isn't empty instead of the recipe input not being empty?
                // I am guessing that they are theoretically the same "type" if we get to here so that is why
                if (!recipeInput.isEmpty()) {
                    int amount = recipeInput.count() * operations;
                    int extracted = slot.extract(ItemResource.of(recipeInput), amount, transaction, AutomationType.INTERNAL);
                    //TODO - 26.1: We probably should abort if this fails to extract what we expect instead of just logging a warning
                    logMismatchedStackSize(extracted, amount);
                }
            }

            @Override
            public void calculateOperationsCanSupport(OperationTracker tracker, ItemStack recipeInput, int usageMultiplier) {
                //Only calculate if we need to use anything
                if (usageMultiplier > 0) {
                    //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
                    // Note: If we can't, we treat it as we just don't have enough of the input to better support cases
                    // where we may want to allow not having the input be required for recipe matching
                    if (!recipeInput.isEmpty()) {
                        //TODO: Simulate?
                        int operations = slot.amount() / (recipeInput.count() * usageMultiplier);
                        if (operations > 0) {
                            tracker.updateOperations(operations);
                            return;
                        }
                    }
                    // Not enough input to match the recipe, reset the progress
                    tracker.resetProgress(notEnoughError);
                }
            }
        };
    }

    /**
     * Wrap a chemical tank into an {@link ILongInputHandler}.
     *
     * @param tank           Tank to wrap.
     * @param notEnoughError The error to apply if the input does not have enough stored for the recipe to be able to perform any operations.
     */
    public static ILongInputHandler<Chemical, @NotNull ChemicalStack> getInputHandler(IChemicalTank tank, RecipeError notEnoughError) {
        Objects.requireNonNull(tank, "Tank cannot be null.");
        Objects.requireNonNull(notEnoughError, "Not enough input error cannot be null.");
        return new ChemicalInputHandler(tank, notEnoughError);
    }

    /**
     * Wrap a chemical tank for constant usage into an {@link ILongInputHandler}.
     *
     * @param tank Tank to wrap.
     */
    public static ILongInputHandler<Chemical, @NotNull ChemicalStack> getConstantInputHandler(IChemicalTank tank) {
        Objects.requireNonNull(tank, "Tank cannot be null.");
        return new ChemicalInputHandler(tank, RecipeError.NOT_ENOUGH_SECONDARY_INPUT) {
            @Override
            protected void resetProgress(OperationTracker tracker) {
                //Don't reset progress just because we have no output if we have constant usage
                // instead just pause the recipe
                tracker.updateOperations(0);
            }
        };
    }

    /**
     * Wrap a fluid tank into an {@link IInputHandler}.
     *
     * @param tank           Tank to wrap.
     * @param notEnoughError The error to apply if the input does not have enough stored for the recipe to be able to perform any operations.
     */
    public static IInputHandler<Fluid, @NotNull FluidStack> getInputHandler(IFluidTank tank, RecipeError notEnoughError) {
        Objects.requireNonNull(tank, "Tank cannot be null.");
        Objects.requireNonNull(notEnoughError, "Not enough input error cannot be null.");
        return new IInputHandler<>() {

            @NotNull
            @Override
            public FluidStack getInput() {
                return tank.getResource().toStack(tank.amount());
            }

            @NotNull
            @Override
            public FluidStack getRecipeInput(InputIngredient<Fluid, @NotNull FluidStack> recipeIngredient) {
                FluidStack input = getInput();
                if (input.isEmpty()) {
                    //All recipes currently require that we have an input. If we don't then return that we failed
                    return FluidStack.EMPTY;
                }
                return recipeIngredient.getMatchingInstance(input);
            }

            @Override
            public void use(FluidStack recipeInput, int operations, TransactionContext transaction) {
                if (operations == 0 || recipeInput.isEmpty()) {
                    //Just exit if we are somehow here at zero operations
                    // or if something went wrong, this if should never really be true if we got to finishProcessing
                    return;
                }
                FluidStack inputFluid = getInput();
                if (!inputFluid.isEmpty()) {
                    int amount = recipeInput.amount() * operations;
                    int extracted = tank.extract(FluidResource.of(recipeInput), amount, transaction, AutomationType.INTERNAL);
                    //TODO - 26.1: We probably should abort if this fails to extract what we expect instead of just logging a warning
                    logMismatchedStackSize(extracted, amount);
                }
            }

            @Override
            public void calculateOperationsCanSupport(OperationTracker tracker, FluidStack recipeInput, int usageMultiplier) {
                //Only calculate if we need to use anything
                if (usageMultiplier > 0) {
                    //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputFluid)
                    // Note: If we can't, we treat it as we just don't have enough of the input to better support cases
                    // where we may want to allow not having the input be required for recipe matching
                    if (!recipeInput.isEmpty()) {
                        //TODO: Simulate the drain?
                        int operations = getInput().amount() / (recipeInput.amount() * usageMultiplier);
                        if (operations > 0) {
                            tracker.updateOperations(operations);
                            return;
                        }
                    }
                    // Not enough input to match the recipe, reset the progress
                    tracker.resetProgress(notEnoughError);
                }
            }
        };
    }

    private static void logMismatchedStackSize(long actual, long expected) {
        if (expected != actual) {
            MekanismAPI.logger.error("Stack size changed by a different amount ({}) than requested ({}).", actual, expected, new Exception());
        }
    }

    private static class ChemicalInputHandler implements ILongInputHandler<Chemical, ChemicalStack> {

        private final IChemicalTank tank;
        private final RecipeError notEnoughError;

        private ChemicalInputHandler(IChemicalTank tank, RecipeError notEnoughError) {
            this.tank = tank;
            this.notEnoughError = notEnoughError;
        }

        @Override
        public ChemicalStack getInput() {
            return tank.getResource().toStack(tank.amountAsLong());
        }

        @Override
        public ChemicalStack getRecipeInput(InputIngredient<Chemical, ChemicalStack> recipeIngredient) {
            ChemicalStack input = getInput();
            if (input.isEmpty()) {
                //All recipes currently require that we have an input. If we don't then return that we failed
                return ChemicalStack.EMPTY;
            }
            return recipeIngredient.getMatchingInstance(input);
        }

        @Override
        public void use(ChemicalStack recipeInput, long operations, TransactionContext transaction) {
            if (operations == 0 || recipeInput.isEmpty()) {
                //Just exit if we are somehow here at zero operations
                // or if something went wrong, this if should never really be true if we got to finishProcessing
                return;
            }
            ChemicalStack inputChemical = getInput();
            if (!inputChemical.isEmpty()) {
                int amount = Ints.saturatedCast(recipeInput.amount() * operations);
                int extracted = tank.extract(ChemicalResource.of(recipeInput), amount, transaction, AutomationType.INTERNAL);
                //TODO - 26.1: We probably should abort if this fails to extract what we expect instead of just logging a warning
                logMismatchedStackSize(extracted, amount);
            }
        }

        @Override
        public void calculateOperationsCanSupport(OperationTracker tracker, ChemicalStack recipeInput, long usageMultiplier) {
            //Only calculate if we need to use anything
            if (usageMultiplier > 0) {
                //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputChemical)
                // Note: If we can't, we treat it as we just don't have enough of the input to better support cases
                // where we may want to allow not having the input be required for recipe matching
                if (!recipeInput.isEmpty()) {
                    //TODO: Simulate the drain?
                    int operations = Ints.saturatedCast(tank.amountAsLong() / (recipeInput.amount() * usageMultiplier));
                    if (operations > 0) {
                        tracker.updateOperations(operations);
                        return;
                    }
                }
                // Not enough input to match the recipe, reset the progress
                resetProgress(tracker);
            }
        }

        protected void resetProgress(OperationTracker tracker) {
            tracker.resetProgress(notEnoughError);
        }
    }
}