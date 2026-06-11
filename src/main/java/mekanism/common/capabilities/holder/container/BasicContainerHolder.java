package mekanism.common.capabilities.holder.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.common.capabilities.holder.BasicHolder;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class BasicContainerHolder<CONTAINER> extends BasicHolder implements IContainerHolder<CONTAINER> {

    private Map<RelativeSide, List<CONTAINER>> directionalContainers = Collections.emptyMap();
    private final List<CONTAINER> containers = new ArrayList<>();
    //TODO: Allow declaring that some sides will be the same, so can just be the same list in memory??

    protected BasicContainerHolder(Supplier<Direction> facingSupplier, @Nullable Predicate<RelativeSide> insertPredicate, @Nullable Predicate<RelativeSide> extractPredicate) {
        super(facingSupplier, insertPredicate, extractPredicate);
    }

    void addContainer(CONTAINER container, RelativeSide... sides) {
        containers.add(container);
        if (sides.length > 0) {
            if (directionalContainers.isEmpty()) {//Lazily initialize the map in case our holder has no directional containers
                directionalContainers = new EnumMap<>(RelativeSide.class);
            }
            for (RelativeSide side : sides) {
                directionalContainers.computeIfAbsent(side, _ -> new ArrayList<>()).add(container);
            }
        }
    }

    @Override
    public List<CONTAINER> getContainers(@Nullable Direction side) {
        if (side == null || directionalContainers.isEmpty()) {
            //If we want the internal OR we have no side specification, give all of our containers
            return containers;
        }
        List<CONTAINER> containers = directionalContainers.get(RelativeSide.fromDirections(facingSupplier.get(), side));
        if (containers == null) {
            return Collections.emptyList();
        }
        return containers;
    }
}