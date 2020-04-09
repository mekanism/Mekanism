package mekanism.api.heat;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.math.FloatingLong;
import net.minecraft.util.Direction;

/**
 * A sided variant of {@link IHeatHandler}
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ISidedHeatHandler extends IHeatHandler {

    /**
     * The side this {@link ISidedHeatHandler} is for. This defaults to null, which is for internal use.
     *
     * @return The default side to use for the normal {@link IHeatHandler} methods when wrapping them into {@link ISidedHeatHandler} methods.
     */
    @Nullable
    default Direction getHeatSideFor() {
        return null;
    }

    /**
     * A sided variant of {@link IHeatHandler#getHeatCapacitorCount()}, docs copied for convenience.
     *
     * Returns the number of heat storage units ("capacitors") available
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The number of capacitors available
     */
    int getHeatCapacitorCount(@Nullable Direction side);

    @Override
    default int getHeatCapacitorCount() {
        return getHeatCapacitorCount(getHeatSideFor());
    }

    FloatingLong getTemperature(int capacitor, @Nullable Direction side);

    @Override
    default FloatingLong getTemperature(int capacitor) {
        return getTemperature(capacitor, getHeatSideFor());
    }

    FloatingLong getInverseConduction(int capacitor, @Nullable Direction side);

    @Override
    default FloatingLong getInverseConduction(int capacitor) {
        return getInverseConduction(capacitor, getHeatSideFor());
    }

    FloatingLong getHeatCapacity(int capacitor, @Nullable Direction side);

    @Override
    default FloatingLong getHeatCapacity(int capacitor) {
        return getHeatCapacity(capacitor, getHeatSideFor());
    }

    void handleHeat(int capacitor, HeatPacket transfer, @Nullable Direction side);

    @Override
    default void handleHeat(int capacitor, HeatPacket transfer) {
        handleHeat(capacitor, transfer, getHeatSideFor());
    }

    default FloatingLong getTotalTemperature(@Nullable Direction side) {
        FloatingLong sum = FloatingLong.ZERO;
        for (int capacitor = 0; capacitor < getHeatCapacitorCount(side); capacitor++) {
            sum = sum.plusEqual(getTemperature(capacitor, side).multiply(getHeatCapacity(capacitor, side).divideEquals(getHeatCapacity(capacitor, side))));
        }
        return sum;
    }

    default FloatingLong getTotalInverseConductionCoefficient(@Nullable Direction side) {
        FloatingLong sum = FloatingLong.ZERO;
        for (int capacitor = 0; capacitor < getHeatCapacitorCount(side); capacitor++) {
            sum = sum.plusEqual(getInverseConduction(capacitor, side));
        }
        return sum;
    }

    default FloatingLong getTotalHeatCapacity(@Nullable Direction side) {
        FloatingLong sum = FloatingLong.ZERO;
        for (int capacitor = 0; capacitor < getHeatCapacitorCount(side); capacitor++) {
            sum = sum.plusEqual(getHeatCapacity(capacitor, side));
        }
        return sum;
    }

    default void handleHeat(HeatPacket transfer, @Nullable Direction side) {
        FloatingLong totalHeatCapacity = getTotalHeatCapacity(side);
        for (int capacitor = 0; capacitor < getHeatCapacitorCount(side); capacitor++) {
            handleHeat(capacitor, transfer.split(getHeatCapacity(capacitor, side).divideToLevel(totalHeatCapacity)), side);
        }
    }
}
