package mekanism.api.transaction;

import mekanism.api.AutomationType;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

/// Represents a tracker that manages the usage of a per tick rate limit.
///
/// @since 10.8.0
public interface RateLimitTracker {

    /// {@return the remaining rate limit this tick for the given automation type}
    ///
    /// @param automationType The automation type to get the remaining rate limit for.
    @Range(from = 0, to = Integer.MAX_VALUE)
    int getRemainingLimit(AutomationType automationType);

    /// Marks part of the rate limit as having been used.
    ///
    /// @param limit          Amount of the rate limit that has been used.
    /// @param automationType The automation type to reduce the limit this tick for.
    /// @param transaction    The transaction that this operation is part of.
    void consumeLimit(@Range(from = 0, to = Integer.MAX_VALUE) int limit, AutomationType automationType, TransactionContext transaction);
}