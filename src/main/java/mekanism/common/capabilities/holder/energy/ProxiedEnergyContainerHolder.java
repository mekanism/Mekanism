package mekanism.common.capabilities.holder.energy;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.holder.ProxiedHolder;
import net.minecraft.util.Direction;

public class ProxiedEnergyContainerHolder extends ProxiedHolder implements IEnergyContainerHolder {

    private final Function<Direction, List<IEnergyContainer>> containerFunction;

    public static ProxiedEnergyContainerHolder create(Predicate<Direction> insertPredicate, Predicate<Direction> extractPredicate,
          Function<Direction, List<IEnergyContainer>> containerFunction) {
        return new ProxiedEnergyContainerHolder(insertPredicate, extractPredicate, containerFunction);
    }

    private ProxiedEnergyContainerHolder(Predicate<Direction> insertPredicate, Predicate<Direction> extractPredicate,
          Function<Direction, List<IEnergyContainer>> containerFunction) {
        super(insertPredicate, extractPredicate);
        this.containerFunction = containerFunction;
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return containerFunction.apply(side);
    }
}