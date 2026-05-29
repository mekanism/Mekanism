package mekanism.common.capabilities.holder.energy;

import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.holder.ProxiedHolder;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class ProxiedEnergyContainerHolder extends ProxiedHolder implements IEnergyContainerHolder {

    private final Function<@Nullable Direction, @Nullable IEnergyContainer> containerFunction;

    public static ProxiedEnergyContainerHolder create(Predicate<Direction> insertPredicate, Predicate<Direction> extractPredicate,
          Function<@Nullable Direction, @Nullable IEnergyContainer> containerFunction) {
        return new ProxiedEnergyContainerHolder(insertPredicate, extractPredicate, containerFunction);
    }

    private ProxiedEnergyContainerHolder(Predicate<Direction> insertPredicate, Predicate<Direction> extractPredicate,
          Function<@Nullable Direction, @Nullable IEnergyContainer> containerFunction) {
        super(insertPredicate, extractPredicate);
        this.containerFunction = containerFunction;
    }

    @Nullable
    @Override
    public IEnergyContainer getContainer(@Nullable Direction side) {
        return containerFunction.apply(side);
    }
}