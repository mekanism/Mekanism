package mekanism.common.capabilities.holder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BasicHolder<CONTAINER> implements IContainerHolder<CONTAINER> {

    private Map<RelativeSide, List<CONTAINER>> directionalContainers = Collections.emptyMap();
    private final List<CONTAINER> containers = new ArrayList<>();
    protected final Supplier<Direction> facingSupplier;
    @Nullable
    private final Predicate<RelativeSide> insertPredicate;
    @Nullable
    private final Predicate<RelativeSide> extractPredicate;
    //TODO: Allow declaring that some sides will be the same, so can just be the same list in memory??

    protected BasicHolder(Supplier<Direction> facingSupplier, @Nullable Predicate<RelativeSide> insertPredicate, @Nullable Predicate<RelativeSide> extractPredicate) {
        this.facingSupplier = facingSupplier;
        this.insertPredicate = insertPredicate;
        this.extractPredicate = extractPredicate;
    }

    void addContainer(@NotNull CONTAINER container, RelativeSide... sides) {
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

    @NotNull
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

    @Override
    public boolean canInsert(@Nullable Direction direction) {
        //If the insert predicate is null then we can insert from any side, don't bother looking up our facing
        return direction != null && (insertPredicate == null || insertPredicate.test(RelativeSide.fromDirections(facingSupplier.get(), direction)));
    }

    @Override
    public boolean canExtract(@Nullable Direction direction) {
        //If the extract predicate is null then we can extract from any side, don't bother looking up our facing
        return direction != null && (extractPredicate == null || extractPredicate.test(RelativeSide.fromDirections(facingSupplier.get(), direction)));
    }
}