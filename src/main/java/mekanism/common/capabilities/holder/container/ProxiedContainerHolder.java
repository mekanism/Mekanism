package mekanism.common.capabilities.holder.container;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.common.capabilities.holder.ProxiedHolder;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class ProxiedContainerHolder<CONTAINER> extends ProxiedHolder implements IContainerHolder<CONTAINER> {

    private final Function<@Nullable Direction, List<CONTAINER>> containerFunction;

    public ProxiedContainerHolder(Predicate<@Nullable Direction> insertPredicate, Predicate<@Nullable Direction> extractPredicate,
          Function<@Nullable Direction, List<CONTAINER>> containerFunction) {
        super(insertPredicate, extractPredicate);
        this.containerFunction = containerFunction;
    }

    @Override
    public List<CONTAINER> getContainers(@Nullable Direction side) {
        return containerFunction.apply(side);
    }
}