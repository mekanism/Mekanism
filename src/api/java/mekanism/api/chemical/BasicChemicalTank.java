package mekanism.api.chemical;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismPreconditions;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.resource.BasicResourceContainer;
import mekanism.api.transaction.RateLimitTracker;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

public class BasicChemicalTank extends BasicResourceContainer<ChemicalResource> implements IChemicalTank {

    /// Creates a tank with a given capacity, and content listener, using the default attribute validator [ChemicalAttributeValidator#DEFAULT].
    ///
    /// @param capacity Tank capacity.
    /// @param listener Contents change listener.
    ///
    /// @since 10.7.0 Previously was in ChemicalTankBuilder
    public static IChemicalTank create(@Range(from = 0, to = Long.MAX_VALUE) long capacity, @Nullable IContentsListener listener) {
        return createWithValidator(capacity, null, listener);
    }

    /// Creates a tank with a given capacity, attribute validator, and content listener.
    ///
    /// @param capacity           Tank capacity.
    /// @param attributeValidator Chemical Attribute Validator, or `null` to fall back to [ChemicalAttributeValidator#DEFAULT].
    /// @param listener           Contents change listener.
    ///
    /// @since 10.7.0 Previously was in ChemicalTankBuilder
    public static IChemicalTank createWithValidator(@Range(from = 0, to = Long.MAX_VALUE) long capacity, @Nullable ChemicalAttributeValidator attributeValidator,
          @Nullable IContentsListener listener) {
        MekanismPreconditions.checkNonNegative(capacity);
        return create(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(), attributeValidator, listener);
    }

    /// Creates a tank with a given capacity, and content listener, that allows chemicals with any attributes.
    ///
    /// @param capacity Tank capacity.
    /// @param listener Contents change listener.
    ///
    /// @since 10.7.0 Previously was in ChemicalTankBuilder
    public static IChemicalTank createAllValid(@Range(from = 0, to = Long.MAX_VALUE) long capacity, @Nullable IContentsListener listener) {
        return createWithValidator(capacity, ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    /// Creates a tank with a given capacity, validation predicate, and content listener, using the default attribute validator [ChemicalAttributeValidator#DEFAULT].
    ///
    /// @param capacity  Tank capacity.
    /// @param validator Validation predicate.
    /// @param listener  Contents change listener.
    ///
    /// @since 10.7.11
    public static IChemicalTank create(@Range(from = 0, to = Long.MAX_VALUE) long capacity, Predicate<ChemicalResource> validator, @Nullable IContentsListener listener) {
        MekanismPreconditions.checkNonNegative(capacity);
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return create(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), validator, null, listener);
    }

    /// Creates an input tank with a given capacity, validation predicate, and content listener, using the default attribute validator
    /// [ChemicalAttributeValidator#DEFAULT]. Input tanks don't allow for external ([AutomationType#EXTERNAL]) extraction.
    ///
    /// @param capacity  Tank capacity.
    /// @param validator Validation predicate.
    /// @param listener  Contents change listener.
    ///
    /// @since 10.7.11
    public static IChemicalTank input(@Range(from = 0, to = Long.MAX_VALUE) long capacity, Predicate<ChemicalResource> validator, @Nullable IContentsListener listener) {
        MekanismPreconditions.checkNonNegative(capacity);
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return create(capacity, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), validator, null, listener);
    }

    /// Creates an input tank with a given capacity, insertion predicate, validation predicate, and content listener, using the default attribute validator
    /// [ChemicalAttributeValidator#DEFAULT]. Input tanks don't allow for external ([AutomationType#EXTERNAL]) extraction.
    ///
    /// @param capacity  Tank capacity.
    /// @param canInsert Insert predicate.
    /// @param validator Validation predicate.
    /// @param listener  Contents change listener.
    ///
    /// @since 10.8.0
    public static IChemicalTank input(@Range(from = 0, to = Long.MAX_VALUE) long capacity, BiPredicate<ChemicalResource, AutomationType> canInsert,
          Predicate<ChemicalResource> validator, @Nullable IContentsListener listener) {
        MekanismPreconditions.checkNonNegative(capacity);
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return create(capacity, ConstantPredicates.notExternal(), canInsert, validator, null, listener);
    }

    /// Creates an output tank with a given capacity, and content listener, that allows chemicals with any attributes. Output tanks only allow for internal
    /// ([AutomationType#INTERNAL]) insertion.
    ///
    /// @param capacity Tank capacity.
    /// @param listener Contents change listener.
    ///
    /// @since 10.7.0 Previously was in ChemicalTankBuilder
    public static IChemicalTank output(@Range(from = 0, to = Long.MAX_VALUE) long capacity, @Nullable IContentsListener listener) {
        MekanismPreconditions.checkNonNegative(capacity);
        return create(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.internalOnly(), ConstantPredicates.alwaysTrue(),
              ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    /// Creates a tank with a given capacity, extract predicate, insert predicate, validation predicate, and content listener, using the default attribute validator
    /// [ChemicalAttributeValidator#DEFAULT].
    ///
    /// @param capacity   Tank capacity.
    /// @param canExtract Extract predicate.
    /// @param canInsert  Insert predicate.
    /// @param validator  Validation predicate.
    /// @param listener   Contents change listener.
    ///
    /// @since 10.7.11
    public static IChemicalTank create(@Range(from = 0, to = Long.MAX_VALUE) long capacity, BiPredicate<ChemicalResource, AutomationType> canExtract,
          BiPredicate<ChemicalResource, AutomationType> canInsert, Predicate<ChemicalResource> validator, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, validator, null, listener);
    }

    /// Creates a tank with a given capacity, extract predicate, insert predicate, validation predicate, attribute validator, and content listener, that allows chemicals
    /// with any attributes.
    ///
    /// @param capacity   Tank capacity.
    /// @param canExtract Extract predicate.
    /// @param canInsert  Insert predicate.
    /// @param validator  Validation predicate.
    /// @param listener   Contents change listener.
    ///
    /// @since 10.8.0
    public static IChemicalTank createAllValid(@Range(from = 0, to = Long.MAX_VALUE) long capacity, BiPredicate<ChemicalResource, AutomationType> canExtract,
          BiPredicate<ChemicalResource, AutomationType> canInsert, Predicate<ChemicalResource> validator, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, validator, ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    /// Creates a tank with a given capacity, extract predicate, insert predicate, validation predicate, attribute validator, and content listener.
    ///
    /// @param capacity           Tank capacity.
    /// @param canExtract         Extract predicate.
    /// @param canInsert          Insert predicate.
    /// @param validator          Validation predicate.
    /// @param attributeValidator Chemical Attribute Validator, or `null` to fall back to [ChemicalAttributeValidator#DEFAULT].
    /// @param listener           Contents change listener.
    ///
    /// @since 10.7.11
    public static IChemicalTank create(@Range(from = 0, to = Long.MAX_VALUE) long capacity, BiPredicate<ChemicalResource, AutomationType> canExtract,
          BiPredicate<ChemicalResource, AutomationType> canInsert, Predicate<ChemicalResource> validator, @Nullable ChemicalAttributeValidator attributeValidator,
          @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, validator, null, null, attributeValidator, listener);
    }

    /// Creates a tank with a given capacity, extract predicate, insert predicate, validation predicate, attribute validator, and content listener.
    ///
    /// @param capacity              Tank capacity.
    /// @param canExtract            Extract predicate.
    /// @param canInsert             Insert predicate.
    /// @param validator             Validation predicate.
    /// @param insertionRateLimiter  Insertion rate limit handler, or `null` to not limit the insertion rate.
    /// @param extractionRateLimiter Extraction rate limit handler, or `null` to not limit the insertion rate.
    /// @param attributeValidator    Chemical Attribute Validator, or `null` to fall back to [ChemicalAttributeValidator#DEFAULT].
    /// @param listener              Contents change listener.
    ///
    /// @since 10.8.0
    public static IChemicalTank create(@Range(from = 0, to = Long.MAX_VALUE) long capacity, BiPredicate<ChemicalResource, AutomationType> canExtract,
          BiPredicate<ChemicalResource, AutomationType> canInsert, Predicate<ChemicalResource> validator, @Nullable RateLimitTracker insertionRateLimiter,
          @Nullable RateLimitTracker extractionRateLimiter, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        MekanismPreconditions.checkNonNegative(capacity);
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return new BasicChemicalTank(capacity, canExtract, canInsert, validator, insertionRateLimiter, extractionRateLimiter, attributeValidator, listener);
    }

    @Nullable
    private final ChemicalAttributeValidator attributeValidator;

    /// @param capacity              Tank capacity.
    /// @param canExtract            Extract predicate.
    /// @param canInsert             Insert predicate.
    /// @param validator             Validation predicate.
    /// @param insertionRateLimiter  Insertion rate limit handler, or `null` to not limit the insertion rate.
    /// @param extractionRateLimiter Extraction rate limit handler, or `null` to not limit the insertion rate.
    /// @param attributeValidator    Chemical Attribute Validator, or `null` to fall back to [ChemicalAttributeValidator#DEFAULT].
    /// @param listener              Contents change listener.
    protected BasicChemicalTank(@Range(from = 0, to = Long.MAX_VALUE) long capacity, BiPredicate<ChemicalResource, AutomationType> canExtract,
          BiPredicate<ChemicalResource, AutomationType> canInsert, Predicate<ChemicalResource> validator, @Nullable RateLimitTracker insertionRateLimiter,
          @Nullable RateLimitTracker extractionRateLimiter, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        super(capacity, canExtract, canInsert, validator, insertionRateLimiter, extractionRateLimiter, listener);
        this.attributeValidator = attributeValidator;
    }

    @Override
    public boolean isValid(ChemicalResource chemicalType) {
        TransferPreconditions.checkNonEmpty(chemicalType);
        return getAttributeValidator().process(chemicalType) && super.isValid(chemicalType);
    }

    @Override
    public ChemicalAttributeValidator getAttributeValidator() {
        return attributeValidator == null ? IChemicalTank.super.getAttributeValidator() : attributeValidator;
    }
}