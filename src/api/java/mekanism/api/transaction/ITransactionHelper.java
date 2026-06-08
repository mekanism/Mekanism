package mekanism.api.transaction;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.MekanismAPI;
import mekanism.api.functions.ConstantPredicates;
import org.jspecify.annotations.Nullable;

/// Provides helper methods for performing transactional operations and creation objects that manage data transactionally.
///
/// @since 10.8.0
public interface ITransactionHelper {

    /// Provides access to Mekanism's implementation of [ITransactionHelper].
    ITransactionHelper INSTANCE = MekanismAPI.getService(ITransactionHelper.class);

    /// @param gameTimeSupplier Supplier for the current game time. Every time this value returns a different result than the previous time, the rate limit will be reset.
    /// @param rateLimit        Transfer rate limit per tick.
    ///
    /// @return A rate limit tracker that allows the given rate limit to be used, resetting every tick.
    default RateLimitTracker createRateLimitTracker(LongSupplier gameTimeSupplier, IntSupplier rateLimit) {
        return createRateLimitTracker(gameTimeSupplier, rateLimit, ConstantPredicates.alwaysTrue());
    }

    /// @param gameTimeSupplier Supplier for the current game time. Every time this value returns a different result than the previous time, the rate limit will be reset.
    /// @param rateLimit        Transfer rate limit per tick when using [internal automation][AutomationType#INTERNAL].
    ///
    /// @return A rate limit tracker that allows the given rate limit to be used for internal automation, resetting every tick. Other automation types will behave as if
    /// they have an [infinite rate limit][#infiniteRateLimit]
    default RateLimitTracker createInternalOnlyRateLimit(LongSupplier gameTimeSupplier, IntSupplier rateLimit) {
        return createRateLimitTracker(gameTimeSupplier, rateLimit, AutomationType::isInternal);
    }

    /// @param gameTimeSupplier Supplier for the current game time. Every time this value returns a different result than the previous time, the rate limit will be reset.
    /// @param rateLimit        Transfer rate limit per tick when not using [manual automation][AutomationType#MANUAL].
    ///
    /// @return A rate limit tracker that allows the given rate limit to be used for non-manual automation types, resetting every tick. The manual automation type will
    /// behave as if it has an [infinite rate limit][#infiniteRateLimit]
    RateLimitTracker createManualBypassRateLimit(LongSupplier gameTimeSupplier, IntSupplier rateLimit);

    /// @param gameTimeSupplier       Supplier for the current game time. Every time this value returns a different result than the previous time, the rate limit will be
    /// reset.
    /// @param rateLimit              Transfer rate limit per tick.
    /// @param limitedAutomationTypes Predicate to check if an automation type should have a limited rate. If this returns `false` the rate limit will act like
    /// [#infiniteRateLimit] for that automation type.
    ///
    /// @return A rate limit tracker that allows the given rate limit to be used by the limited automation types, resetting every tick.
    RateLimitTracker createRateLimitTracker(LongSupplier gameTimeSupplier, IntSupplier rateLimit, Predicate<AutomationType> limitedAutomationTypes);


    /// {@return a rate limit tracker that has no limit}
    RateLimitTracker infiniteRateLimit();

    /// {@return the given rate limit tracker, or the infinite rate limit tracker if no tracker was given}
    ///
    /// @param tracker Rate limit tracker to check if present.
    default RateLimitTracker orInfinite(@Nullable RateLimitTracker tracker) {//TODO - 26.1: Do we want to define rate limits for more things?
        return tracker == null ? infiniteRateLimit() : tracker;
    }
}