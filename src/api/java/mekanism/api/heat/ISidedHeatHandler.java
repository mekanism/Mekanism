package mekanism.api.heat;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * A sided variant of {@link IHeatHandler}
 */
@NothingNullByDefault
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
     * <p>
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

    /**
     * A sided variant of {@link IHeatHandler#getTemperature(int)}, docs copied for convenience.
     * <p>
     * Returns the temperature of a given capacitor.
     *
     * @param capacitor Capacitor to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return Temperature of a given capacitor.
     */
    double getTemperature(int capacitor, @Nullable Direction side);

    @Override
    default double getTemperature(int capacitor) {
        return getTemperature(capacitor, getHeatSideFor());
    }

    /**
     * A sided variant of {@link IHeatHandler#getInverseConduction(int)}, docs copied for convenience.
     * <p>
     * Returns the inverse conduction coefficient of a given capacitor. This value defines how much heat is allowed to be dissipated. The larger the number the less heat
     * can dissipate. The trade-off is that it also allows for lower amounts of heat to be inserted.
     *
     * @param capacitor Capacitor to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return Inverse conduction coefficient of a given capacitor.
     *
     * @apiNote Must be greater than 0
     */
    double getInverseConduction(int capacitor, @Nullable Direction side);

    @Override
    default double getInverseConduction(int capacitor) {
        return getInverseConduction(capacitor, getHeatSideFor());
    }

    /**
     * A sided variant of {@link IHeatHandler#getHeatCapacity(int)}, docs copied for convenience.
     * <p>
     * Returns the heat capacity of a given capacitor.
     *
     * @param capacitor Capacitor to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return Heat capacity of a given capacitor.
     *
     * @apiNote Must be at least 1
     */
    double getHeatCapacity(int capacitor, @Nullable Direction side);

    @Override
    default double getHeatCapacity(int capacitor) {
        return getHeatCapacity(capacitor, getHeatSideFor());
    }

    /**
     * A sided variant of {@link IHeatHandler#handleHeat(int, double)}, docs copied for convenience.
     * <p>
     * Handles transferring heat to the given capacitor.
     *
     * @param capacitor Capacitor to target
     * @param transfer  The amount being transferred.
     * @param side      The side we are interacting with the handler from (null for internal).
     */
    void handleHeat(int capacitor, double transfer, @Nullable Direction side);

    @Override
    default void handleHeat(int capacitor, double transfer) {
        handleHeat(capacitor, transfer, getHeatSideFor());
    }

    /**
     * A sided variant of {@link IHeatHandler#getTotalTemperature()}, docs copied for convenience.
     * <p>
     * Calculates the total temperature across all capacitors in this handler.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The total temperature across all capacitors in this handler.
     */
    default double getTotalTemperature(@Nullable Direction side) {
        int heatCapacitorCount = getHeatCapacitorCount(side);
        if (heatCapacitorCount == 1) {
            return getTemperature(0, side);
        }
        double sum = 0;
        double totalCapacity = getTotalHeatCapacity(side);
        for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum += getTemperature(capacitor, side) * (getHeatCapacity(capacitor, side) / totalCapacity);
        }
        return sum;
    }

    @Override
    default double getTotalTemperature() {
        return getTotalTemperature(getHeatSideFor());
    }

    /**
     * A sided variant of {@link IHeatHandler#getTotalInverseConduction()}, docs copied for convenience.
     * <p>
     * Calculates the total inverse conduction coefficient across all capacitors in this handler.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The total inverse conduction coefficient across all capacitors in this handler.
     *
     * @apiNote Must be greater than 0
     */
    default double getTotalInverseConductionCoefficient(@Nullable Direction side) {
        int heatCapacitorCount = getHeatCapacitorCount(side);
        if (heatCapacitorCount == 0) {
            return HeatAPI.DEFAULT_INVERSE_CONDUCTION;
        } else if (heatCapacitorCount == 1) {
            return getInverseConduction(0, side);
        }
        double sum = 0;
        double totalCapacity = getTotalHeatCapacity(side);
        for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum += getInverseConduction(capacitor, side) * (getHeatCapacity(capacitor, side) / totalCapacity);
        }
        return sum;
    }

    @Override
    default double getTotalInverseConduction() {
        return getTotalInverseConductionCoefficient(getHeatSideFor());
    }

    /**
     * A sided variant of {@link IHeatHandler#getTotalHeatCapacity()}, docs copied for convenience.
     * <p>
     * Calculates the total heat capacity across all capacitors in this handler.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The total heat capacity across all capacitors in this handler.
     */
    default double getTotalHeatCapacity(@Nullable Direction side) {
        int heatCapacitorCount = getHeatCapacitorCount(side);
        if (heatCapacitorCount == 1) {
            return getHeatCapacity(0, side);
        }
        double sum = 0;
        for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum += getHeatCapacity(capacitor, side);
        }
        return sum;
    }

    @Override
    default double getTotalHeatCapacity() {
        return getTotalHeatCapacity(getHeatSideFor());
    }

    /**
     * A sided variant of {@link IHeatHandler#handleHeat(double)}, docs copied for convenience.
     * <p>
     * Handles a change of heat in this block. Can be positive or negative.
     *
     * @param transfer The amount being transferred.
     * @param side     The side we are interacting with the handler from (null for internal).
     *
     * @implNote Default implementation evenly distributes it between stored capacitors
     */
    default void handleHeat(double transfer, @Nullable Direction side) {
        int heatCapacitorCount = getHeatCapacitorCount(side);
        if (heatCapacitorCount == 1) {
            handleHeat(0, transfer, side);
        } else {
            double totalHeatCapacity = getTotalHeatCapacity(side);
            for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
                handleHeat(capacitor, transfer * (getHeatCapacity(capacitor, side) / totalHeatCapacity), side);
            }
        }
    }

    @Override
    default void handleHeat(double transfer) {
        handleHeat(transfer, getHeatSideFor());
    }
}
