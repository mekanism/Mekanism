package mekanism.api.heat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.math.FloatingLong;
import net.minecraft.util.Direction;

public interface ISidedHeatHandler extends IHeatHandler {

    @Nullable
    default Direction getHeatSideFor() {
        return null;
    }

    int getHeatCapacitorCount(@Nullable Direction side);

    @Override
    default int getHeatCapacitorCount() {
        return getHeatCapacitorCount(getHeatSideFor());
    }

    FloatingLong getTemperature(@Nullable Direction side);

    @Override
    default FloatingLong getTemperature() {
        return getTemperature(getHeatSideFor());
    }

    double getInverseConductionCoefficient(@Nullable Direction side);

    @Override
    default double getInverseConductionCoefficient() {
        return getInverseConductionCoefficient(getHeatSideFor());
    }

    void handleHeatChange(@Nonnull HeatPacket transfer, @Nullable Direction side);

    @Override
    default void handleHeatChange(@Nonnull HeatPacket transfer) {
        handleHeatChange(transfer, getHeatSideFor());
    }
}
