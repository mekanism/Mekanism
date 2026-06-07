package mekanism.common.lib.transaction;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.transaction.RateLimitTracker;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

class RateLimitIntegerJournal extends GameTimeBasedJournal<Integer> implements RateLimitTracker {

    private final Predicate<AutomationType> limitedAutomationTypes;
    private final IntSupplier rateLimit;
    private int remainingLimit;

    RateLimitIntegerJournal(LongSupplier gameTimeSupplier, IntSupplier rateLimit, Predicate<AutomationType> limitedAutomationTypes) {
        super(gameTimeSupplier);
        this.rateLimit = rateLimit;
        this.limitedAutomationTypes = limitedAutomationTypes;
    }

    @Override
    protected void tickChanged(long gameTime) {
        super.tickChanged(gameTime);
        remainingLimit = rateLimit.getAsInt();
    }

    @Override
    protected Integer createSnapshot() {
        return remainingLimit;
    }

    @Override
    protected void revertToSnapshot(Integer snapshot) {
        this.remainingLimit = snapshot;
    }

    @Override
    public int getRemainingLimit(AutomationType automationType) {
        Objects.requireNonNull(automationType, "Automation type must not be null.");
        if (limitedAutomationTypes.test(automationType)) {
            checkTickChanged();
            return remainingLimit;
        }
        //Return no limit if our automation type is not a limited one
        return Integer.MAX_VALUE;
    }

    @Override
    public void consumeLimit(int limit, AutomationType automationType, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(limit);
        Objects.requireNonNull(automationType, "Automation type must not be null.");
        //NO-OP if the automation type is not one being limited
        if (limitedAutomationTypes.test(automationType)) {
            if (limit > remainingLimit) {
                throw new IllegalArgumentException("Attempted to consume more of the limit than there was remaining. As getRemainingLimit validates the limit, this should not be possible");
            }
            //Note: This ends up checking if the tick changed again, but it should be fine
            updateSnapshots(transaction);
            this.remainingLimit -= limit;
        }
    }
}