package mekanism.common.lib.distribution.handler;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Lies about how much it can actually accept when simulating. This is to simulate if multiple handlers end up affecting the same backing tank. As then during simulation
 * it will have reported it could accept more than we actually can.
 */
public class LyingAmountIntegerHandler extends SpecificAmountIntegerHandler {

    private final int amountToLieBy;
    private boolean hasLied;

    public LyingAmountIntegerHandler(int toAccept, int amountToLieBy) {
        super(toAccept);
        this.amountToLieBy = amountToLieBy;
    }

    @Override
    public int perform(int amountOffered, TransactionContext transaction) {//TODO - 26.1: Evaluate if these lying handlers even make any sense anymore
        int canAccept = super.perform(amountOffered, transaction);
        if (!hasLied) {
            hasLied = true;
            //If we are simulating (the first time we are called), "lie" and say we can accept more than we actually have room for
            return Math.min(amountOffered, canAccept + amountToLieBy);
        }
        return canAccept;
    }
}