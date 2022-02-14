package mekanism.api.recipes.outputs;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.ElectrolysisRecipe.ElectrolysisRecipeOutput;
import mekanism.api.recipes.PressurizedReactionRecipe.PressurizedReactionRecipeOutput;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OutputHelper {

    private OutputHelper() {
    }

    /**
     * Wrap a chemical tank into an {@link IOutputHandler}.
     *
     * @param tank                Tank to wrap.
     * @param notEnoughSpaceError The error to apply if the output causes the recipe to not be able to perform any operations.
     */
    public static <STACK extends ChemicalStack<?>> IOutputHandler<@NonNull STACK> getOutputHandler(IChemicalTank<?, STACK> tank, RecipeError notEnoughSpaceError) {
        Objects.requireNonNull(tank, "Tank cannot be null.");
        Objects.requireNonNull(notEnoughSpaceError, "Not enough space error cannot be null.");
        return new IOutputHandler<>() {

            @Override
            public void handleOutput(STACK toOutput, int operations) {
                OutputHelper.handleOutput(tank, toOutput, operations);
            }

            @Override
            public void calculateOperationsCanSupport(OperationTracker tracker, STACK toOutput) {
                OutputHelper.calculateOperationsCanSupport(tracker, notEnoughSpaceError, tank, toOutput);
            }
        };
    }

    /**
     * Wrap a fluid tank into an {@link IOutputHandler}.
     *
     * @param tank                Tank to wrap.
     * @param notEnoughSpaceError The error to apply if the output causes the recipe to not be able to perform any operations.
     */
    public static IOutputHandler<@NonNull FluidStack> getOutputHandler(IExtendedFluidTank tank, RecipeError notEnoughSpaceError) {
        Objects.requireNonNull(tank, "Tank cannot be null.");
        Objects.requireNonNull(notEnoughSpaceError, "Not enough space error cannot be null.");
        return new IOutputHandler<>() {

            @Override
            public void handleOutput(FluidStack toOutput, int operations) {
                OutputHelper.handleOutput(tank, toOutput, operations);
            }

            @Override
            public void calculateOperationsCanSupport(OperationTracker tracker, FluidStack toOutput) {
                OutputHelper.calculateOperationsCanSupport(tracker, notEnoughSpaceError, tank, toOutput);
            }
        };
    }

    /**
     * Wrap an inventory slot into an {@link IOutputHandler}.
     *
     * @param slot                Slot to wrap.
     * @param notEnoughSpaceError The error to apply if the output causes the recipe to not be able to perform any operations.
     */
    public static IOutputHandler<@NonNull ItemStack> getOutputHandler(IInventorySlot slot, RecipeError notEnoughSpaceError) {
        Objects.requireNonNull(slot, "Slot cannot be null.");
        Objects.requireNonNull(notEnoughSpaceError, "Not enough space error cannot be null.");
        return new IOutputHandler<>() {

            @Override
            public void handleOutput(ItemStack toOutput, int operations) {
                OutputHelper.handleOutput(slot, toOutput, operations);
            }

            @Override
            public void calculateOperationsCanSupport(OperationTracker tracker, ItemStack toOutput) {
                OutputHelper.calculateOperationsCanSupport(tracker, notEnoughSpaceError, slot, toOutput);
            }
        };
    }

    /**
     * Wraps two inventory slots, a "main" slot, and a "secondary" slot into an {@link IOutputHandler} for handling {@link ChanceOutput}s.
     *
     * @param mainSlot                         Main slot to wrap.
     * @param secondarySlot                    Secondary slot to wrap.
     * @param mainSlotNotEnoughSpaceError      The error to apply if the main output causes the recipe to not be able to perform any operations.
     * @param secondarySlotNotEnoughSpaceError The error to apply if the secondary output causes the recipe to not be able to perform any operations.
     */
    public static IOutputHandler<@NonNull ChanceOutput> getOutputHandler(IInventorySlot mainSlot, RecipeError mainSlotNotEnoughSpaceError,
          IInventorySlot secondarySlot, RecipeError secondarySlotNotEnoughSpaceError) {
        Objects.requireNonNull(mainSlot, "Main slot cannot be null.");
        Objects.requireNonNull(secondarySlot, "Secondary/Extra slot cannot be null.");
        Objects.requireNonNull(mainSlotNotEnoughSpaceError, "Main slot not enough space error cannot be null.");
        Objects.requireNonNull(secondarySlotNotEnoughSpaceError, "Secondary/Extra slot not enough space error cannot be null.");
        return new IOutputHandler<>() {

            @Override
            public void handleOutput(ChanceOutput toOutput, int operations) {
                OutputHelper.handleOutput(mainSlot, toOutput.getMainOutput(), operations);
                //TODO: Batch this into a single addition call, by looping over and calculating things?
                ItemStack secondaryOutput = toOutput.getSecondaryOutput();
                for (int i = 0; i < operations; i++) {
                    OutputHelper.handleOutput(secondarySlot, secondaryOutput, operations);
                    if (i < operations - 1) {
                        secondaryOutput = toOutput.nextSecondaryOutput();
                    }
                }
            }

            @Override
            public void calculateOperationsCanSupport(OperationTracker tracker, ChanceOutput toOutput) {
                OutputHelper.calculateOperationsCanSupport(tracker, mainSlotNotEnoughSpaceError, mainSlot, toOutput.getMainOutput());
                if (tracker.shouldContinueChecking()) {
                    OutputHelper.calculateOperationsCanSupport(tracker, secondarySlotNotEnoughSpaceError, secondarySlot, toOutput.getMaxSecondaryOutput());
                }
            }
        };
    }

    /**
     * Wraps a gas tank and an inventory slot an {@link IOutputHandler}.
     *
     * @param tank                    Tank to wrap.
     * @param slot                    Slot to wrap.
     * @param slotNotEnoughSpaceError The error to apply if the slot output causes the recipe to not be able to perform any operations.
     * @param tankNotEnoughSpaceError The error to apply if the tank output causes the recipe to not be able to perform any operations.
     */
    public static IOutputHandler<@NonNull PressurizedReactionRecipeOutput> getOutputHandler(IInventorySlot slot, RecipeError slotNotEnoughSpaceError,
          IGasTank tank, RecipeError tankNotEnoughSpaceError) {
        Objects.requireNonNull(slot, "Slot cannot be null.");
        Objects.requireNonNull(tank, "Tank cannot be null.");
        Objects.requireNonNull(slotNotEnoughSpaceError, "Slot not enough space error cannot be null.");
        Objects.requireNonNull(tankNotEnoughSpaceError, "Tank not enough space error cannot be null.");
        return new IOutputHandler<>() {

            @Override
            public void handleOutput(PressurizedReactionRecipeOutput toOutput, int operations) {
                OutputHelper.handleOutput(slot, toOutput.item(), operations);
                OutputHelper.handleOutput(tank, toOutput.gas(), operations);
            }

            @Override
            public void calculateOperationsCanSupport(OperationTracker tracker, PressurizedReactionRecipeOutput toOutput) {
                OutputHelper.calculateOperationsCanSupport(tracker, slotNotEnoughSpaceError, slot, toOutput.item());
                if (tracker.shouldContinueChecking()) {
                    OutputHelper.calculateOperationsCanSupport(tracker, tankNotEnoughSpaceError, tank, toOutput.gas());
                }
            }
        };
    }

    /**
     * Wraps two gas tank into an {@link IOutputHandler}.
     *
     * @param leftTank                 Left tank to wrap.
     * @param rightTank                Right tank to wrap.
     * @param leftNotEnoughSpaceError  The error to apply if the left output causes the recipe to not be able to perform any operations.
     * @param rightNotEnoughSpaceError The error to apply if the right output causes the recipe to not be able to perform any operations.
     */
    public static IOutputHandler<@NonNull ElectrolysisRecipeOutput> getOutputHandler(IGasTank leftTank, RecipeError leftNotEnoughSpaceError,
          IGasTank rightTank, RecipeError rightNotEnoughSpaceError) {
        Objects.requireNonNull(leftTank, "Left tank cannot be null.");
        Objects.requireNonNull(rightTank, "Right tank cannot be null.");
        Objects.requireNonNull(leftNotEnoughSpaceError, "Left not enough space error cannot be null.");
        Objects.requireNonNull(rightNotEnoughSpaceError, "Right not enough space error cannot be null.");
        return new IOutputHandler<>() {

            @Override
            public void handleOutput(ElectrolysisRecipeOutput toOutput, int operations) {
                OutputHelper.handleOutput(leftTank, toOutput.left(), operations);
                OutputHelper.handleOutput(rightTank, toOutput.right(), operations);
            }

            @Override
            public void calculateOperationsCanSupport(OperationTracker tracker, ElectrolysisRecipeOutput toOutput) {
                OutputHelper.calculateOperationsCanSupport(tracker, leftNotEnoughSpaceError, leftTank, toOutput.left());
                if (tracker.shouldContinueChecking()) {
                    OutputHelper.calculateOperationsCanSupport(tracker, rightNotEnoughSpaceError, rightTank, toOutput.right());
                }
            }
        };
    }

    /**
     * Adds {@code operations} operations worth of {@code toOutput} to the output.
     *
     * @param tank       Output.
     * @param toOutput   Output result.
     * @param operations Operations to perform.
     */
    static <STACK extends ChemicalStack<?>> void handleOutput(IChemicalTank<?, STACK> tank, STACK toOutput, int operations) {
        if (operations == 0) {
            //This should not happen
            return;
        }
        STACK output = tank.createStack(toOutput, toOutput.getAmount() * operations);
        tank.insert(output, Action.EXECUTE, AutomationType.INTERNAL);
    }

    private static void handleOutput(IExtendedFluidTank fluidTank, FluidStack toOutput, int operations) {
        if (operations == 0) {
            //This should not happen
            return;
        }
        fluidTank.insert(new FluidStack(toOutput, toOutput.getAmount() * operations), Action.EXECUTE, AutomationType.INTERNAL);
    }

    private static void handleOutput(IInventorySlot inventorySlot, ItemStack toOutput, int operations) {
        if (operations == 0 || toOutput.isEmpty()) {
            return;
        }
        ItemStack output = toOutput.copy();
        if (operations > 1) {
            //If we are doing more than one operation we need to make a copy of our stack and change the amount
            // that we are using the fill the tank with
            output.setCount(output.getCount() * operations);
        }
        inventorySlot.insertItem(output, Action.EXECUTE, AutomationType.INTERNAL);
    }

    /**
     * Calculates how many operations the output has room for and updates the given operation tracker.
     *
     * @param tracker        Tracker of current errors and max operations.
     * @param tank           Output.
     * @param toOutput       Output result.
     * @param notEnoughSpace The error to apply if the output causes the recipe to not be able to perform any operations.
     */
    static <STACK extends ChemicalStack<?>> void calculateOperationsCanSupport(OperationTracker tracker, RecipeError notEnoughSpace, IChemicalTank<?, STACK> tank,
          STACK toOutput) {
        //If our output is empty, we have nothing to add, so we treat it as being able to fit all
        if (!toOutput.isEmpty()) {
            //Copy the stack and make it be max size
            STACK maxOutput = tank.createStack(toOutput, Long.MAX_VALUE);
            //Divide the amount we can actually use by the amount one output operation is equal to, capping it at the max we were told about
            STACK remainder = tank.insert(maxOutput, Action.SIMULATE, AutomationType.INTERNAL);
            long amountUsed = maxOutput.getAmount() - remainder.getAmount();
            //Divide the amount we can actually use by the amount one output operation is equal to, capping it at the max we were told about
            int operations = MathUtils.clampToInt(amountUsed / toOutput.getAmount());
            tracker.updateOperations(operations);
            if (operations == 0) {
                if (amountUsed == 0 && tank.getNeeded() > 0) {
                    tracker.addError(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT);
                } else {
                    tracker.addError(notEnoughSpace);
                }
            }
        }
    }

    private static void calculateOperationsCanSupport(OperationTracker tracker, RecipeError notEnoughSpace, IExtendedFluidTank tank, FluidStack toOutput) {
        //If our output is empty, we have nothing to add, so we treat it as being able to fit all
        if (!toOutput.isEmpty()) {
            //Copy the stack and make it be max size
            FluidStack maxOutput = new FluidStack(toOutput, Integer.MAX_VALUE);
            //Then simulate filling the fluid tank, so we can see how much actually can fit
            FluidStack remainder = tank.insert(maxOutput, Action.SIMULATE, AutomationType.INTERNAL);
            int amountUsed = maxOutput.getAmount() - remainder.getAmount();
            //Divide the amount we can actually use by the amount one output operation is equal to, capping it at the max we were told about
            int operations = amountUsed / toOutput.getAmount();
            tracker.updateOperations(operations);
            if (operations == 0) {
                if (amountUsed == 0 && tank.getNeeded() > 0) {
                    tracker.addError(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT);
                } else {
                    tracker.addError(notEnoughSpace);
                }
            }
        }
    }

    private static void calculateOperationsCanSupport(OperationTracker tracker, RecipeError notEnoughSpace, IInventorySlot slot, ItemStack toOutput) {
        //If our output is empty, we have nothing to add, so we treat it as being able to fit all
        if (!toOutput.isEmpty()) {
            ItemStack output = toOutput.copy();
            //Make a cope of the stack we are outputting with its maximum size
            output.setCount(output.getMaxStackSize());
            ItemStack remainder = slot.insertItem(output, Action.SIMULATE, AutomationType.INTERNAL);
            int amountUsed = output.getCount() - remainder.getCount();
            //Divide the amount we can actually use by the amount one output operation is equal to, capping it at the max we were told about
            int operations = amountUsed / toOutput.getCount();
            tracker.updateOperations(operations);
            if (operations == 0) {
                if (amountUsed == 0 && slot.getLimit(slot.getStack()) - slot.getCount() > 0) {
                    tracker.addError(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT);
                } else {
                    tracker.addError(notEnoughSpace);
                }
            }
        }
    }
}