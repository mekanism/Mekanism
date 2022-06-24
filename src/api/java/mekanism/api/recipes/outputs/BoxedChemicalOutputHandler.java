package mekanism.api.recipes.outputs;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;

/**
 * Specialized version of {@link IOutputHandler} for handling boxed chemicals.
 */
@ParametersAreNonnullByDefault
public class BoxedChemicalOutputHandler {

    private final RecipeError notEnoughSpaceError;
    private final MergedChemicalTank chemicalTank;

    public BoxedChemicalOutputHandler(MergedChemicalTank chemicalTank, RecipeError notEnoughSpaceError) {
        this.chemicalTank = Objects.requireNonNull(chemicalTank, "Chemical tank cannot be null.");
        this.notEnoughSpaceError = Objects.requireNonNull(notEnoughSpaceError, "Not enough space error cannot be null.");
    }

    /**
     * Adds {@code operations} operations worth of {@code toOutput} to the output.
     *
     * @param toOutput   Output result.
     * @param operations Operations to perform.
     */
    public void handleOutput(BoxedChemicalStack toOutput, int operations) {
        handleOutput(chemicalTank.getTankForType(toOutput.getChemicalType()), toOutput.getChemicalStack(), operations);
    }

    @SuppressWarnings("unchecked")
    private <STACK extends ChemicalStack<?>> void handleOutput(IChemicalTank<?, ?> tank, STACK stack, int operations) {
        OutputHelper.handleOutput((IChemicalTank<?, STACK>) tank, stack, operations);
    }

    /**
     * Calculates how many operations the output has room for and updates the given operation tracker. It can be assumed that when this method is called {@link
     * OperationTracker#shouldContinueChecking()} is {@code true}.
     *
     * @param tracker  Tracker of current errors and max operations.
     * @param toOutput Output result.
     */
    public void calculateOperationsRoomFor(OperationTracker tracker, BoxedChemicalStack toOutput) {
        calculateOperationsRoomFor(tracker, chemicalTank.getTankForType(toOutput.getChemicalType()), toOutput.getChemicalStack());
    }

    @SuppressWarnings("unchecked")
    private <STACK extends ChemicalStack<?>> void calculateOperationsRoomFor(OperationTracker tracker, IChemicalTank<?, ?> tank, STACK stack) {
        OutputHelper.calculateOperationsCanSupport(tracker, notEnoughSpaceError, (IChemicalTank<?, STACK>) tank, stack);
    }
}