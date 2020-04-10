package mekanism.common.capabilities.holder.fluid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.fluid.IExtendedFluidTank;
import net.minecraft.util.Direction;

public class FluidTankHolder implements IFluidTankHolder {

    private final Map<RelativeSide, List<IExtendedFluidTank>> directionalTanks = new EnumMap<>(RelativeSide.class);
    private final List<IExtendedFluidTank> tanks = new ArrayList<>();
    private final Supplier<Direction> facingSupplier;
    //TODO: Allow declaring that some sides will be the same, so can just be the same list in memory??

    FluidTankHolder(Supplier<Direction> facingSupplier) {
        this.facingSupplier = facingSupplier;
    }

    void addTank(@Nonnull IExtendedFluidTank tank, RelativeSide... sides) {
        tanks.add(tank);
        for (RelativeSide side : sides) {
            directionalTanks.computeIfAbsent(side, k -> new ArrayList<>()).add(tank);
        }
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getTanks(@Nullable Direction direction) {
        if (direction == null || directionalTanks.isEmpty()) {
            //If we want the internal OR we have no side specification, give all of our tanks
            return tanks;
        }
        RelativeSide side = RelativeSide.fromDirections(facingSupplier.get(), direction);
        List<IExtendedFluidTank> tanks = directionalTanks.get(side);
        if (tanks == null) {
            return Collections.emptyList();
        }
        return tanks;
    }
}