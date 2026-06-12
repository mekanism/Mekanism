package mekanism.common.capabilities.holder.single;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.common.capabilities.holder.BasicHolder;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class BasicSingleHolder<CONTAINER> extends BasicHolder implements ISingleContainerHolder<CONTAINER> {

    protected static final Set<RelativeSide> ALL_SIDES = EnumSet.allOf(RelativeSide.class);
    private final Set<RelativeSide> supportedSides;
    private final CONTAINER container;

    public BasicSingleHolder(CONTAINER container, Supplier<Direction> facingSupplier, Set<RelativeSide> supportedSides) {
        this(container, facingSupplier, null, null, supportedSides);
    }

    public BasicSingleHolder(CONTAINER container, Supplier<Direction> facingSupplier, @Nullable Predicate<RelativeSide> insertPredicate,
          @Nullable Predicate<RelativeSide> extractPredicate) {
        this(container, facingSupplier, insertPredicate, extractPredicate, ALL_SIDES);
    }

    public BasicSingleHolder(CONTAINER container, Supplier<Direction> facingSupplier, @Nullable Predicate<RelativeSide> insertPredicate,
          @Nullable Predicate<RelativeSide> extractPredicate, Set<RelativeSide> supportedSides) {
        super(facingSupplier, insertPredicate, extractPredicate);
        this.container = container;
        this.supportedSides = supportedSides;
    }

    @Nullable
    @Override
    public CONTAINER getContainer(@Nullable Direction side) {
        if (side == null || supportedSides.contains(RelativeSide.fromDirections(facingSupplier.get(), side))) {
            //If we want the internal OR we are contained within our side specification, give all of our containers
            return container;
        }
        return null;
    }
}