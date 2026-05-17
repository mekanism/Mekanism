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

    private final Map<RelativeSide, List<CONTAINER>> directionalContaineres = new EnumMap<>(RelativeSide.class);
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
        for (RelativeSide side : sides) {
            directionalContaineres.computeIfAbsent(side, k -> new ArrayList<>()).add(container);
        }
    }

    @NotNull
    @Override
    public List<CONTAINER> getContainers(@Nullable Direction side) {
        if (side == null || directionalContaineres.isEmpty()) {
            //If we want the internal OR we have no side specification, give all of our slots
            return containers;
        }
        List<CONTAINER> slots = directionalContaineres.get(RelativeSide.fromDirections(facingSupplier.get(), side));
        if (slots == null) {
            return Collections.emptyList();
        }
        return slots;
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