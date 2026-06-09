package mekanism.common.capabilities.holder.container;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class OverridingHolder<CONTAINER> extends BasicContainerHolder<CONTAINER> {

    private final Map<RelativeSide, List<CONTAINER>> sideOverrides = new EnumMap<>(RelativeSide.class);

    protected OverridingHolder(Supplier<Direction> facingSupplier, @Nullable Predicate<RelativeSide> insertPredicate, @Nullable Predicate<RelativeSide> extractPredicate) {
        super(facingSupplier, insertPredicate, extractPredicate);
    }

    void addContainer(CONTAINER container, BiFunction<CONTAINER, RelativeSide, CONTAINER> containerTransformer) {
        addContainer(container);
        for (RelativeSide side : EnumUtils.SIDES) {
            CONTAINER transformed = containerTransformer.apply(container, side);
            if (transformed != container) {
                sideOverrides.computeIfAbsent(side, _ -> new ArrayList<>()).add(transformed);
            }
        }
        if (sideOverrides.isEmpty()) {
            throw new IllegalStateException("No sides overrides added for " + container);
        }
    }

    @Override
    public List<CONTAINER> getContainers(@Nullable Direction side) {
        if (side == null) {
            return super.getContainers(null);
        }
        List<CONTAINER> containers = sideOverrides.get(RelativeSide.fromDirections(facingSupplier.get(), side));
        if (containers == null) {
            return super.getContainers(side);
        }
        return containers;
    }
}