package mekanism.common.capabilities.fluid;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismPreconditions;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.resource.BasicResourceContainer;
import mekanism.api.transaction.RateLimitTracker;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class BasicFluidTank extends BasicResourceContainer<FluidResource> implements IFluidTank {

    public static BasicFluidTank create(@Range(from = 0, to = Long.MAX_VALUE) long capacity, @Nullable IContentsListener listener) {
        return create(capacity, ConstantPredicates.alwaysTrue(), listener);
    }

    public static BasicFluidTank create(@Range(from = 0, to = Long.MAX_VALUE) long capacity, Predicate<FluidResource> validator, @Nullable IContentsListener listener) {
        return create(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), validator, listener);
    }

    public static BasicFluidTank create(@Range(from = 0, to = Long.MAX_VALUE) long capacity, BiPredicate<FluidResource, AutomationType> canExtract,
          BiPredicate<FluidResource, AutomationType> canInsert, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, ConstantPredicates.alwaysTrue(), listener);
    }

    public static BasicFluidTank input(@Range(from = 0, to = Long.MAX_VALUE) long capacity, Predicate<FluidResource> validator, @Nullable IContentsListener listener) {
        return create(capacity, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), validator, listener);
    }

    public static BasicFluidTank input(@Range(from = 0, to = Long.MAX_VALUE) long capacity, BiPredicate<FluidResource, AutomationType> canInsert, Predicate<FluidResource> validator,
          @Nullable IContentsListener listener) {
        return create(capacity, ConstantPredicates.notExternal(), canInsert, validator, listener);
    }

    public static BasicFluidTank output(@Range(from = 0, to = Long.MAX_VALUE) long capacity, @Nullable IContentsListener listener) {
        return create(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.internalOnly(), ConstantPredicates.alwaysTrue(), listener);
    }

    public static BasicFluidTank create(@Range(from = 0, to = Long.MAX_VALUE) long capacity, BiPredicate<FluidResource, AutomationType> canExtract,
          BiPredicate<FluidResource, AutomationType> canInsert, Predicate<FluidResource> validator, @Nullable IContentsListener listener) {
        MekanismPreconditions.checkNonNegative(capacity);
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new BasicFluidTank(capacity, canExtract, canInsert, validator, null, null, listener);
    }

    protected BasicFluidTank(@Range(from = 0, to = Long.MAX_VALUE) long capacity, BiPredicate<FluidResource, AutomationType> canExtract,
          BiPredicate<FluidResource, AutomationType> canInsert, Predicate<FluidResource> validator, @Nullable RateLimitTracker insertionRateLimiter,
          @Nullable RateLimitTracker extractionRateLimiter, @Nullable IContentsListener listener) {
        super(capacity, canExtract, canInsert, validator, insertionRateLimiter, extractionRateLimiter, listener);
    }
}