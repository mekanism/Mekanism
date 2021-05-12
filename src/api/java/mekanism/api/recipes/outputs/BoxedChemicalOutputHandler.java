package mekanism.api.recipes.outputs;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.merged.MergedChemicalTank;

/**
 * Specialized version of {@link IOutputHandler} for handling boxed chemicals.
 */
@ParametersAreNonnullByDefault
public class BoxedChemicalOutputHandler {

    private final MergedChemicalTank chemicalTank;

    public BoxedChemicalOutputHandler(MergedChemicalTank chemicalTank) {
        this.chemicalTank = Objects.requireNonNull(chemicalTank, "Chemical tank cannot be null.");
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

    private <STACK extends ChemicalStack<?>> void handleOutput(IChemicalTank<?, ?> tank, STACK stack, int operations) {
        OutputHelper.handleOutput((IChemicalTank<?, STACK>) tank, stack, operations);
    }

    /**
     * Calculates how many operations the output has room for.
     *
     * @param toOutput   Output result.
     * @param currentMax The current maximum number of operations that can happen.
     *
     * @return The number of operations the output has room for.
     */
    public int operationsRoomFor(BoxedChemicalStack toOutput, int currentMax) {
        return operationsRoomFor(chemicalTank.getTankForType(toOutput.getChemicalType()), toOutput.getChemicalStack(), currentMax);
    }

    private <STACK extends ChemicalStack<?>> int operationsRoomFor(IChemicalTank<?, ?> tank, STACK stack, int currentMax) {
        return OutputHelper.operationsRoomFor((IChemicalTank<?, STACK>) tank, stack, currentMax);
    }
}