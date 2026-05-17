package mekanism.common.capabilities.holder;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProxiedContainerHolder<CONTAINER> extends ProxiedHolder implements IContainerHolder<CONTAINER> {

    private final Function<Direction, List<CONTAINER>> containerFunction;

    public static <CONTAINER> ProxiedContainerHolder<CONTAINER> create(Predicate<Direction> insertPredicate, Predicate<Direction> extractPredicate,
          Function<Direction, List<CONTAINER>> containerFunction) {
        return new ProxiedContainerHolder<>(insertPredicate, extractPredicate, containerFunction);
    }

    private ProxiedContainerHolder(Predicate<Direction> insertPredicate, Predicate<Direction> extractPredicate,
          Function<Direction, List<CONTAINER>> containerFunction) {
        super(insertPredicate, extractPredicate);
        this.containerFunction = containerFunction;
    }

    @NotNull
    @Override
    public List<CONTAINER> getContainers(@Nullable Direction side) {
        return containerFunction.apply(side);
    }
}