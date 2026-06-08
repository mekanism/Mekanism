package mekanism.api.recipes.outputs;

import java.util.Objects;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.ElectrolysisRecipe.ElectrolysisRecipeOutput;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe.FluidOptionalItemOutput;
import mekanism.api.recipes.PressurizedReactionRecipe.PressurizedReactionRecipeOutput;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.resource.IResourceContainer;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public class OutputHelper {

    private OutputHelper() {
    }

    /**
     * Wrap a chemical tank into an {@link IOutputHandler}.
     *
     * @param tank                Tank to wrap.
     * @param notEnoughSpaceError The error to apply if the output causes the recipe to not be able to perform any operations.
     */
    public static IOutputHandler<ChemicalStack> getOutputHandler(IChemicalTank tank, RecipeError notEnoughSpaceError) {
        Objects.requireNonNull(tank, "Tank cannot be null.");
        Objects.requireNonNull(notEnoughSpaceError, "Not enough space error cannot be null.");
        return new IOutputHandler<>() {
            @Override
            public boolean handleOutput(@Nullable ChemicalStack toOutput, int operations, TransactionContext transaction) {
                return OutputHelper.handleOutput(tank, toOutput, operations, transaction);
            }

            @Override
            public void calculateOperationsCanSupport(OperationTracker tracker, ChemicalStack toOutput) {
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
    public static IOutputHandler<FluidStackTemplate> getOutputHandler(IFluidTank tank, RecipeError notEnoughSpaceError) {
        Objects.requireNonNull(tank, "Tank cannot be null.");
        Objects.requireNonNull(notEnoughSpaceError, "Not enough space error cannot be null.");
        return new IOutputHandler<>() {
            @Override
            public boolean handleOutput(@Nullable FluidStackTemplate toOutput, int operations, TransactionContext transaction) {
                return OutputHelper.handleOutput(tank, toOutput, operations, transaction);
            }

            @Override
            public void calculateOperationsCanSupport(OperationTracker tracker, FluidStackTemplate toOutput) {
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
    public static IOutputHandler<ItemStackTemplate> getOutputHandler(IInventorySlot slot, RecipeError notEnoughSpaceError) {
        Objects.requireNonNull(slot, "Slot cannot be null.");
        Objects.requireNonNull(notEnoughSpaceError, "Not enough space error cannot be null.");
        return new IOutputHandler<>() {
            @Override
            public boolean handleOutput(@Nullable ItemStackTemplate toOutput, int operations, TransactionContext transaction) {
                return OutputHelper.handleOutput(slot, toOutput, operations, transaction);
            }

            @Override
            public void calculateOperationsCanSupport(OperationTracker tracker, ItemStackTemplate toOutput) {
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
    public static IOutputHandler<ChanceOutput> getOutputHandler(IInventorySlot mainSlot, RecipeError mainSlotNotEnoughSpaceError,
          IInventorySlot secondarySlot, RecipeError secondarySlotNotEnoughSpaceError) {
        Objects.requireNonNull(mainSlot, "Main slot cannot be null.");
        Objects.requireNonNull(secondarySlot, "Secondary/Extra slot cannot be null.");
        Objects.requireNonNull(mainSlotNotEnoughSpaceError, "Main slot not enough space error cannot be null.");
        Objects.requireNonNull(secondarySlotNotEnoughSpaceError, "Secondary/Extra slot not enough space error cannot be null.");
        return new IOutputHandler<>() {
            @Override
            public boolean handleOutput(@Nullable ChanceOutput toOutput, int operations, TransactionContext transaction) {
                if (toOutput == null) {
                    return false;
                } else if (operations == 0) {
                    return true;
                }
                ItemStackTemplate mainOutput = toOutput.getMainOutput();
                //If we don't have a primary output, or we could add it all
                if (mainOutput == null || OutputHelper.handleOutput(mainSlot, mainOutput, operations, transaction)) {
                    ItemStackTemplate secondaryOutput = toOutput.getMaxSecondaryOutput();
                    if (secondaryOutput != null) {
                        ItemResource secondaryType = ItemResource.of(secondaryOutput);
                        int scaledAmount = 0;
                        for (int i = 0; i < operations; i++) {
                            //Note: We know this should fit as we calculated how many operations can be performed based on the maximum amount of the secondary output
                            ItemStackTemplate nextOutput = toOutput.nextSecondaryOutput();
                            if (nextOutput != null) {
                                scaledAmount += nextOutput.count();
                            }
                        }
                        //If we produced a secondary output for our operations, validate that we could add it
                        if (scaledAmount > 0) {
                            //Note: We know this shouldn't overflow, as we clamped the operations based on usage in calculateOperationsCanSupport
                            return OutputHelper.handleOutput(secondarySlot, secondaryType, scaledAmount, transaction);
                        }
                    }
                    return true;
                }
                return false;
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
     * Wraps a chemical tank and an inventory slot into an {@link IOutputHandler}.
     *
     * @param tank                    Tank to wrap.
     * @param slot                    Slot to wrap.
     * @param slotNotEnoughSpaceError The error to apply if the slot output causes the recipe to not be able to perform any operations.
     * @param tankNotEnoughSpaceError The error to apply if the tank output causes the recipe to not be able to perform any operations.
     */
    public static IOutputHandler<PressurizedReactionRecipeOutput> getOutputHandler(IInventorySlot slot, RecipeError slotNotEnoughSpaceError,
          IChemicalTank tank, RecipeError tankNotEnoughSpaceError) {
        Objects.requireNonNull(slot, "Slot cannot be null.");
        Objects.requireNonNull(tank, "Tank cannot be null.");
        Objects.requireNonNull(slotNotEnoughSpaceError, "Slot not enough space error cannot be null.");
        Objects.requireNonNull(tankNotEnoughSpaceError, "Tank not enough space error cannot be null.");
        return new IOutputHandler<>() {
            @Override
            public boolean handleOutput(@Nullable PressurizedReactionRecipeOutput toOutput, int operations, TransactionContext transaction) {
                if (toOutput == null) {
                    return false;
                }
                ItemStackTemplate item = toOutput.item();
                return (item == null || OutputHelper.handleOutput(slot, item, operations, transaction)) &&
                       (toOutput.chemical().isEmpty() || OutputHelper.handleOutput(tank, toOutput.chemical(), operations, transaction));
            }

            @Override
            public void calculateOperationsCanSupport(OperationTracker tracker, PressurizedReactionRecipeOutput toOutput) {
                OutputHelper.calculateOperationsCanSupport(tracker, slotNotEnoughSpaceError, slot, toOutput.item());
                if (tracker.shouldContinueChecking()) {
                    OutputHelper.calculateOperationsCanSupport(tracker, tankNotEnoughSpaceError, tank, toOutput.chemical());
                }
            }
        };
    }

    /**
     * Wraps a fluid tank and an inventory slot into an {@link IOutputHandler}.
     *
     * @param tank                    Tank to wrap.
     * @param slot                    Slot to wrap.
     * @param tankNotEnoughSpaceError The error to apply if the tank output causes the recipe to not be able to perform any operations.
     * @param slotNotEnoughSpaceError The error to apply if the slot output causes the recipe to not be able to perform any operations.
     *
     * @since 10.6.3
     */
    public static IOutputHandler<FluidOptionalItemOutput> getOutputHandler(IFluidTank tank, RecipeError tankNotEnoughSpaceError,
          IInventorySlot slot, RecipeError slotNotEnoughSpaceError) {
        Objects.requireNonNull(tank, "Tank cannot be null.");
        Objects.requireNonNull(slot, "Slot cannot be null.");
        Objects.requireNonNull(tankNotEnoughSpaceError, "Tank not enough space error cannot be null.");
        Objects.requireNonNull(slotNotEnoughSpaceError, "Slot not enough space error cannot be null.");
        return new IOutputHandler<>() {
            @Override
            public boolean handleOutput(@Nullable FluidOptionalItemOutput toOutput, int operations, TransactionContext transaction) {
                if (toOutput == null) {
                    return false;
                }
                return OutputHelper.handleOutput(tank, toOutput.fluid(), operations, transaction) &&
                       (toOutput.optionalItem() == null || OutputHelper.handleOutput(slot, toOutput.optionalItem(), operations, transaction));
            }

            @Override
            public void calculateOperationsCanSupport(OperationTracker tracker, FluidOptionalItemOutput toOutput) {
                OutputHelper.calculateOperationsCanSupport(tracker, tankNotEnoughSpaceError, tank, toOutput.fluid());
                if (tracker.shouldContinueChecking()) {
                    OutputHelper.calculateOperationsCanSupport(tracker, slotNotEnoughSpaceError, slot, toOutput.optionalItem());
                }
            }
        };
    }

    /**
     * Wraps two chemical tanks into an {@link IOutputHandler}.
     *
     * @param leftTank                 Left tank to wrap.
     * @param rightTank                Right tank to wrap.
     * @param leftNotEnoughSpaceError  The error to apply if the left output causes the recipe to not be able to perform any operations.
     * @param rightNotEnoughSpaceError The error to apply if the right output causes the recipe to not be able to perform any operations.
     */
    public static IOutputHandler<ElectrolysisRecipeOutput> getOutputHandler(IChemicalTank leftTank, RecipeError leftNotEnoughSpaceError,
          IChemicalTank rightTank, RecipeError rightNotEnoughSpaceError) {
        Objects.requireNonNull(leftTank, "Left tank cannot be null.");
        Objects.requireNonNull(rightTank, "Right tank cannot be null.");
        Objects.requireNonNull(leftNotEnoughSpaceError, "Left not enough space error cannot be null.");
        Objects.requireNonNull(rightNotEnoughSpaceError, "Right not enough space error cannot be null.");
        return new IOutputHandler<>() {
            @Override
            public boolean handleOutput(@Nullable ElectrolysisRecipeOutput toOutput, int operations, TransactionContext transaction) {
                if (toOutput == null) {
                    return false;
                }
                return OutputHelper.handleOutput(leftTank, toOutput.left(), operations, transaction) &&
                       OutputHelper.handleOutput(rightTank, toOutput.right(), operations, transaction);
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

    private static boolean handleOutput(IChemicalTank tank, @Nullable ChemicalStack toOutput, int operations, TransactionContext transaction) {
        return toOutput != null && !toOutput.isEmpty() && handleOutput(tank, ChemicalResource.of(toOutput), toOutput.amount(), operations, transaction);
    }

    private static boolean handleOutput(IFluidTank fluidTank, @Nullable FluidStackTemplate toOutput, int operations, TransactionContext transaction) {
        return toOutput != null && handleOutput(fluidTank, FluidResource.of(toOutput), toOutput.amount(), operations, transaction);
    }

    private static boolean handleOutput(IInventorySlot inventorySlot, @Nullable ItemStackTemplate toOutput, int operations, TransactionContext transaction) {
        return toOutput != null && handleOutput(inventorySlot, ItemResource.of(toOutput), toOutput.count(), operations, transaction);
    }

    private static <RESOURCE extends Resource> boolean handleOutput(IResourceContainer<RESOURCE> container, RESOURCE toOutput, int amount, int operations,
          TransactionContext transaction) {
        if (operations == 0) {
            return true;
        }
        //Note: We know this shouldn't overflow, as we clamped the operations based on usage in calculateOperationsCanSupport
        return handleOutput(container, toOutput, amount * operations, transaction);
    }

    private static <RESOURCE extends Resource> boolean handleOutput(IResourceContainer<RESOURCE> container, RESOURCE toOutput, int scaledAmount, TransactionContext transaction) {
        return container.insert(toOutput, scaledAmount, transaction, AutomationType.INTERNAL) == scaledAmount;
    }

    /**
     * Calculates how many operations the output has room for and updates the given operation tracker.
     *
     * @param tracker        Tracker of current errors and max operations.
     * @param tank           Output.
     * @param toOutput       Output result.
     * @param notEnoughSpace The error to apply if the output causes the recipe to not be able to perform any operations.
     */
    private static void calculateOperationsCanSupport(OperationTracker tracker, RecipeError notEnoughSpace, IChemicalTank tank, ChemicalStack toOutput) {
        calculateOperationsCanSupport(tracker, notEnoughSpace, tank, ChemicalResource.of(toOutput), toOutput.amount(), Integer.MAX_VALUE);
    }

    private static void calculateOperationsCanSupport(OperationTracker tracker, RecipeError notEnoughSpace, IFluidTank tank, @Nullable FluidStackTemplate toOutput) {
        //If our output is empty, we have nothing to add, so we treat it as being able to fit all
        if (toOutput != null) {
            calculateOperationsCanSupport(tracker, notEnoughSpace, tank, FluidResource.of(toOutput), toOutput.amount(), Integer.MAX_VALUE);
        }
    }

    private static void calculateOperationsCanSupport(OperationTracker tracker, RecipeError notEnoughSpace, IInventorySlot slot, @Nullable ItemStackTemplate toOutput) {
        //If our output is empty, we have nothing to add, so we treat it as being able to fit all
        if (toOutput != null) {
            calculateOperationsCanSupport(tracker, notEnoughSpace, slot, ItemResource.of(toOutput), toOutput.count(), toOutput.getMaxStackSize());
        }
    }

    private static <RESOURCE extends Resource> void calculateOperationsCanSupport(OperationTracker tracker, RecipeError notEnoughSpace, IResourceContainer<RESOURCE> container,
          RESOURCE toOutput, int outputSize, int maxStackSize) {
        //If our output is empty, we have nothing to add, so we treat it as being able to fit all
        if (!toOutput.isEmpty()) {
            try (Transaction simulation = tracker.openSimulation()) {
                //Try inserting an amount corresponding to the maximum size of the output
                int amountUsed = container.insert(toOutput, maxStackSize, simulation, AutomationType.INTERNAL);
                //Divide the amount we can actually use by the amount one output operation is equal to, capping it at the max we were told about
                int operations = amountUsed / outputSize;
                tracker.updateOperations(operations);
                if (operations == 0) {
                    if (amountUsed == 0 && !container.isFull()) {
                        tracker.addError(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT);
                    } else {
                        tracker.addError(notEnoughSpace);
                    }
                }
            }
        }
    }
}