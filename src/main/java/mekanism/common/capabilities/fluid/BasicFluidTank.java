package mekanism.common.capabilities.fluid;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.container.BasicResourceContainer;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BasicFluidTank extends BasicResourceContainer<FluidResource> implements IFluidTank {

    public static BasicFluidTank create(long capacity, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        return new BasicFluidTank(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(), listener);
    }

    public static BasicFluidTank create(long capacity, Predicate<FluidResource> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new BasicFluidTank(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), validator, listener);
    }

    public static BasicFluidTank create(long capacity, Predicate<FluidResource> canExtract, Predicate<FluidResource> canInsert, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, ConstantPredicates.alwaysTrue(), listener);
    }

    public static BasicFluidTank input(long capacity, Predicate<FluidResource> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new BasicFluidTank(capacity, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), validator, listener);
    }

    public static BasicFluidTank input(long capacity, Predicate<FluidResource> canInsert, Predicate<FluidResource> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new BasicFluidTank(capacity, ConstantPredicates.notExternal(), (stack, _) -> canInsert.test(stack), validator, listener);
    }

    public static BasicFluidTank output(long capacity, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        return new BasicFluidTank(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.internalOnly(), ConstantPredicates.alwaysTrue(), listener);
    }

    public static BasicFluidTank create(long capacity, Predicate<FluidResource> canExtract, Predicate<FluidResource> canInsert, Predicate<FluidResource> validator,
          @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new BasicFluidTank(capacity, canExtract, canInsert, validator, listener);
    }

    public static BasicFluidTank create(long capacity, BiPredicate<FluidResource, AutomationType> canExtract, BiPredicate<FluidResource, AutomationType> canInsert,
          Predicate<FluidResource> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new BasicFluidTank(capacity, canExtract, canInsert, validator, listener);
    }

    protected BasicFluidTank(long capacity, Predicate<FluidResource> canExtract, Predicate<FluidResource> canInsert, Predicate<FluidResource> validator,
          @Nullable IContentsListener listener) {
        this(capacity, (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack),
              (stack, _) -> canInsert.test(stack), validator, listener);
    }

    //TODO - 26.1: Evaluate callers and make sure that our capacity configs support longs where relevant
    protected BasicFluidTank(long capacity, BiPredicate<FluidResource, AutomationType> canExtract, BiPredicate<FluidResource, AutomationType> canInsert,
          Predicate<FluidResource> validator, @Nullable IContentsListener listener) {
        super(FluidResource.EMPTY, capacity, canExtract, canInsert, validator, listener);
    }
}