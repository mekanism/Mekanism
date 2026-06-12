package mekanism.common.capabilities.holder.single;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.common.capabilities.holder.BasicHolder;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class OverridingSingleHolder<CONTAINER> extends BasicHolder implements ISingleContainerHolder<CONTAINER> {

    private final Map<RelativeSide, CONTAINER> sideOverrides = new EnumMap<>(RelativeSide.class);
    private final CONTAINER container;

    public OverridingSingleHolder(CONTAINER container, Supplier<Direction> facingSupplier, BiFunction<CONTAINER, RelativeSide, CONTAINER> containerTransformer) {
        this(container, facingSupplier, null, null, containerTransformer);
    }

    public OverridingSingleHolder(CONTAINER container, Supplier<Direction> facingSupplier, @Nullable Predicate<RelativeSide> insertPredicate,
          @Nullable Predicate<RelativeSide> extractPredicate, BiFunction<CONTAINER, RelativeSide, CONTAINER> containerTransformer) {
        super(facingSupplier, insertPredicate, extractPredicate);
        this.container = container;
        for (RelativeSide side : EnumUtils.SIDES) {
            CONTAINER transformed = containerTransformer.apply(container, side);
            if (transformed != container) {
                sideOverrides.put(side, transformed);
            }
        }
        if (sideOverrides.isEmpty()) {
            throw new IllegalStateException("No sides overrides added for " + container);
        }
    }

    @Nullable
    @Override
    public CONTAINER getContainer(@Nullable Direction side) {
        if (side == null) {
            //If we want the internal OR we are contained within our side specification, give all of our containers
            return container;
        }
        CONTAINER containerOverride = sideOverrides.get(RelativeSide.fromDirections(facingSupplier.get(), side));
        return Objects.requireNonNullElse(containerOverride, container);
    }
}