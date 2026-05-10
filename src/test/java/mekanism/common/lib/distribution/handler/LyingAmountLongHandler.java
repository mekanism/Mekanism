package mekanism.common.lib.distribution.handler;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Lies about how much it can actually accept when simulating. This is to simulate if multiple handlers end up affecting the same backing tank. As then during simulation
 * it will have reported it could accept more than we actually can.
 */
public class LyingAmountLongHandler extends SpecificAmountLongHandler {

    private final long amountToLieBy;
    private boolean hasLied;

    public LyingAmountLongHandler(long toAccept, long amountToLieBy) {
        super(toAccept);
        this.amountToLieBy = amountToLieBy;
    }

    @Override
    public long perform(long amountOffered, TransactionContext transaction) {
        long canAccept = super.perform(amountOffered, transaction);
        if (!hasLied) {
            hasLied = true;
            //If we are simulating (the first time we are called), "lie" and say we can accept more than we actually have room for
            return Math.min(amountOffered, canAccept + amountToLieBy);
        }
        return canAccept;
    }
}