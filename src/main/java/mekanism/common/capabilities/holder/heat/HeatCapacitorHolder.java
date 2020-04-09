package mekanism.common.capabilities.holder.heat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.heat.IHeatCapacitor;
import net.minecraft.util.Direction;

public class HeatCapacitorHolder implements IHeatCapacitorHolder {

    private final Map<RelativeSide, List<IHeatCapacitor>> directionalCapacitors = new EnumMap<>(RelativeSide.class);
    private final List<IHeatCapacitor> capacitors = new ArrayList<>();
    private final Supplier<Direction> facingSupplier;

    HeatCapacitorHolder(Supplier<Direction> facingSupplier) {
        this.facingSupplier = facingSupplier;
    }

    void addCapacitor(@Nonnull IHeatCapacitor tank, RelativeSide... sides) {
        capacitors.add(tank);
        for (RelativeSide side : sides) {
            directionalCapacitors.computeIfAbsent(side, k -> new ArrayList<>()).add(tank);
        }
    }

    @Nonnull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction direction) {
        if (direction == null || directionalCapacitors.isEmpty()) {
            //If we want the internal OR we have no side specification, give all of our tanks
            return capacitors;
        }
        RelativeSide side = RelativeSide.fromDirections(facingSupplier.get(), direction);
        List<IHeatCapacitor> tanks = directionalCapacitors.get(side);
        if (tanks == null) {
            return Collections.emptyList();
        }
        return tanks;
    }
}
