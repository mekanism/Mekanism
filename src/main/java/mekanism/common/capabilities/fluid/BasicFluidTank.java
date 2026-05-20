package mekanism.common.capabilities.fluid;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismPreconditions;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.BasicResourceContainer;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class BasicFluidTank extends BasicResourceContainer<FluidResource> implements IFluidTank {

    public static BasicFluidTank create(@Range(from = 0, to = Long.MAX_VALUE) long capacity, @Nullable IContentsListener listener) {
        MekanismPreconditions.checkNonNegative(capacity);
        return new BasicFluidTank(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(), listener);
    }

    public static BasicFluidTank create(@Range(from = 0, to = Long.MAX_VALUE) long capacity, Predicate<FluidResource> validator, @Nullable IContentsListener listener) {
        MekanismPreconditions.checkNonNegative(capacity);
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new BasicFluidTank(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), validator, listener);
    }

    public static BasicFluidTank create(@Range(from = 0, to = Long.MAX_VALUE) long capacity, Predicate<FluidResource> canExtract, Predicate<FluidResource> canInsert,
          @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, ConstantPredicates.alwaysTrue(), listener);
    }

    public static BasicFluidTank input(@Range(from = 0, to = Long.MAX_VALUE) long capacity, Predicate<FluidResource> validator, @Nullable IContentsListener listener) {
        MekanismPreconditions.checkNonNegative(capacity);
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new BasicFluidTank(capacity, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), validator, listener);
    }

    public static BasicFluidTank input(@Range(from = 0, to = Long.MAX_VALUE) long capacity, Predicate<FluidResource> canInsert, Predicate<FluidResource> validator,
          @Nullable IContentsListener listener) {
        MekanismPreconditions.checkNonNegative(capacity);
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new BasicFluidTank(capacity, ConstantPredicates.notExternal(), (stack, _) -> canInsert.test(stack), validator, listener);
    }

    public static BasicFluidTank output(@Range(from = 0, to = Long.MAX_VALUE) long capacity, @Nullable IContentsListener listener) {
        MekanismPreconditions.checkNonNegative(capacity);
        return new BasicFluidTank(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.internalOnly(), ConstantPredicates.alwaysTrue(), listener);
    }

    public static BasicFluidTank create(@Range(from = 0, to = Long.MAX_VALUE) long capacity, Predicate<FluidResource> canExtract, Predicate<FluidResource> canInsert,
          Predicate<FluidResource> validator, @Nullable IContentsListener listener) {
        MekanismPreconditions.checkNonNegative(capacity);
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new BasicFluidTank(capacity, canExtract, canInsert, validator, listener);
    }

    public static BasicFluidTank create(@Range(from = 0, to = Long.MAX_VALUE) long capacity, BiPredicate<FluidResource, AutomationType> canExtract,
          BiPredicate<FluidResource, AutomationType> canInsert, Predicate<FluidResource> validator, @Nullable IContentsListener listener) {
        MekanismPreconditions.checkNonNegative(capacity);
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new BasicFluidTank(capacity, canExtract, canInsert, validator, listener);
    }

    protected BasicFluidTank(@Range(from = 0, to = Long.MAX_VALUE) long capacity, Predicate<FluidResource> canExtract, Predicate<FluidResource> canInsert,
          Predicate<FluidResource> validator, @Nullable IContentsListener listener) {
        this(capacity, (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack),
              (stack, _) -> canInsert.test(stack), validator, listener);
    }

    protected BasicFluidTank(@Range(from = 0, to = Long.MAX_VALUE) long capacity, BiPredicate<FluidResource, AutomationType> canExtract,
          BiPredicate<FluidResource, AutomationType> canInsert, Predicate<FluidResource> validator, @Nullable IContentsListener listener) {
        super(capacity, canExtract, canInsert, validator, listener);
    }
}