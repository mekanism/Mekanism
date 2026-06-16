package mekanism.common.capabilities.holder.single;

import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.common.capabilities.holder.ProxiedHolder;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class ProxiedSingleContainerHolder<CONTAINER> extends ProxiedHolder implements ISingleContainerHolder<CONTAINER> {

    private final Function<@Nullable Direction, @Nullable CONTAINER> containerFunction;

    public static <CONTAINER> ProxiedSingleContainerHolder<CONTAINER> energy(Predicate<@Nullable Direction> insertPredicate, Predicate<@Nullable Direction> extractPredicate,
          Function<@Nullable Direction, @Nullable CONTAINER> containerFunction) {
        return new ProxiedSingleContainerHolder<>(insertPredicate, extractPredicate, containerFunction);
    }

    private ProxiedSingleContainerHolder(Predicate<@Nullable Direction> insertPredicate, Predicate<@Nullable Direction> extractPredicate,
          Function<@Nullable Direction, @Nullable CONTAINER> containerFunction) {
        super(insertPredicate, extractPredicate);
        this.containerFunction = containerFunction;
    }

    @Nullable
    @Override
    public CONTAINER getContainer(@Nullable Direction side) {
        return containerFunction.apply(side);
    }
}