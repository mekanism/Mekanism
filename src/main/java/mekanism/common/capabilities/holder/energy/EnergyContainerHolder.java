package mekanism.common.capabilities.holder.energy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IEnergyContainer;
import net.minecraft.util.Direction;

public class EnergyContainerHolder implements IEnergyContainerHolder {

    private final Map<RelativeSide, List<IEnergyContainer>> directionalContainers = new EnumMap<>(RelativeSide.class);
    private final List<IEnergyContainer> containers = new ArrayList<>();
    private final Supplier<Direction> facingSupplier;
    //TODO: Allow declaring that some sides will be the same, so can just be the same list in memory??

    EnergyContainerHolder(Supplier<Direction> facingSupplier) {
        this.facingSupplier = facingSupplier;
    }

    void addContainer(@Nonnull IEnergyContainer container, RelativeSide... sides) {
        containers.add(container);
        for (RelativeSide side : sides) {
            directionalContainers.computeIfAbsent(side, k -> new ArrayList<>()).add(container);
        }
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction direction) {
        if (direction == null || directionalContainers.isEmpty()) {
            //If we want the internal OR we have no side specification, give all of our containers
            return containers;
        }
        RelativeSide side = RelativeSide.fromDirections(facingSupplier.get(), direction);
        List<IEnergyContainer> containers = directionalContainers.get(side);
        if (containers == null) {
            return Collections.emptyList();
        }
        return containers;
    }
}