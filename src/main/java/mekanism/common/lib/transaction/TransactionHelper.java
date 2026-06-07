package mekanism.common.lib.transaction;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.transaction.ITransactionHelper;
import mekanism.api.transaction.RateLimitTracker;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class TransactionHelper implements ITransactionHelper {

    private final Predicate<AutomationType> manualBypass = automationType -> !automationType.isManual();
    private final RateLimitTracker infiniteLimit = new RateLimitTracker() {

        @Override
        public int getRemainingLimit(AutomationType automationType) {
            Objects.requireNonNull(automationType, "Automation type must not be null.");
            return Integer.MAX_VALUE;
        }

        @Override
        public void consumeLimit(int limit, AutomationType automationType, TransactionContext transaction) {
            //NO-OP, as we don't actually want to decrease the remaining limit
            TransferPreconditions.checkNonNegative(limit);
            Objects.requireNonNull(automationType, "Automation type must not be null.");
        }
    };

    @Override
    public RateLimitTracker createManualBypassRateLimit(LongSupplier gameTimeSupplier, IntSupplier rateLimit) {
        return createRateLimitTracker(gameTimeSupplier, rateLimit, manualBypass);
    }

    @Override
    public RateLimitTracker createRateLimitTracker(LongSupplier gameTimeSupplier, IntSupplier rateLimit, Predicate<AutomationType> limitedAutomationTypes) {
        return new RateLimitIntegerJournal(gameTimeSupplier, rateLimit, limitedAutomationTypes);
    }

    @Override
    public RateLimitTracker infiniteRateLimit() {
        return infiniteLimit;
    }

    /// Similar to [Transaction#openRoot] except will open it as a nested transaction if already in a transactional context. This is for cases where there isn't a
    /// transactional context available, but there might be one necessary. For example in item use implementations as it is possible an auto-clicker opened a transaction
    /// for managing power usage before calling the use method.
    ///
    /// Try to avoid using this unless the method signature is not changeable.
    @SuppressWarnings("deprecation")
    public static Transaction openTransactionSafe() {
        return Transaction.open(Transaction.getCurrentOpenedTransaction());
    }
}