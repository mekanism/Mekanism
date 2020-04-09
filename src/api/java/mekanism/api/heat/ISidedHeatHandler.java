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

    /**
     * A sided variant of {@link IHeatHandler#getTemperature(int)}, docs copied for convenience.
     *
     * Returns the temperature of a given capacitor.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link FloatingLong} <em>MUST NOT</em> be modified. This method is not for altering the internal temperature. Any implementers
     * who are able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the value returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED FLOATING LONG</em></strong>
     * </p>
     *
     * @param capacitor Capacitor to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return Temperature of a given capacitor. {@link FloatingLong#ZERO} if the capacitor has a temperature of absolute zero.
     */
    FloatingLong getTemperature(int capacitor, @Nullable Direction side);

    @Override
    default FloatingLong getTemperature(int capacitor) {
        return getTemperature(capacitor, getHeatSideFor());
    }

    /**
     * A sided variant of {@link IHeatHandler#getInverseConduction(int)}, docs copied for convenience.
     *
     * Returns the inverse conduction coefficient of a given capacitor. This value defines how much heat is allowed to be dissipated. The larger the number the less heat
     * can dissipate. The trade off is that it also allows for lower amounts of heat to be inserted.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link FloatingLong} <em>MUST NOT</em> be modified. This method is not for altering the conduction coefficient. Any implementers
     * who are able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the value returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED FLOATING LONG</em></strong>
     * </p>
     *
     * @param capacitor Capacitor to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return Inverse conduction coefficient of a given capacitor.
     *
     * @apiNote Must be greater than {@link FloatingLong#ZERO}
     */
    FloatingLong getInverseConduction(int capacitor, @Nullable Direction side);

    @Override
    default FloatingLong getInverseConduction(int capacitor) {
        return getInverseConduction(capacitor, getHeatSideFor());
    }

    /**
     * A sided variant of {@link IHeatHandler#getHeatCapacity(int)}, docs copied for convenience.
     *
     * Returns the heat capacity of a given capacitor. This number can be thought of as the specific heat of the capacitor.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link FloatingLong} <em>MUST NOT</em> be modified. This method is not for altering the heat capacity. Any implementers who are
     * able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the value returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED FLOATING LONG</em></strong>
     * </p>
     *
     * @param capacitor Capacitor to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return Heat capacity of a given capacitor.
     *
     * @apiNote Must be at least {@link FloatingLong#ONE}
     */
    FloatingLong getHeatCapacity(int capacitor, @Nullable Direction side);

    @Override
    default FloatingLong getHeatCapacity(int capacitor) {
        return getHeatCapacity(capacitor, getHeatSideFor());
    }

    /**
     * A sided variant of {@link IHeatHandler#handleHeat(int, HeatPacket)}, docs copied for convenience.
     *
     * Handles transferring a {@link HeatPacket} to the given capacitor.
     *
     * @param capacitor Capacitor to target
     * @param transfer  The {@link HeatPacket} being transferred.
     * @param side      The side we are interacting with the handler from (null for internal).
     */
    void handleHeat(int capacitor, HeatPacket transfer, @Nullable Direction side);

    @Override
    default void handleHeat(int capacitor, HeatPacket transfer) {
        handleHeat(capacitor, transfer, getHeatSideFor());
    }

    /**
     * A sided variant of {@link IHeatHandler#getTotalTemperature()}, docs copied for convenience.
     *
     * Calculates the total temperature across all capacitors in this handler.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The total temperature across all capacitors in this handler.
     *
     * @apiNote The returned {@link FloatingLong} can be safely modified afterwards.
     */
    default FloatingLong getTotalTemperature(@Nullable Direction side) {
        int heatCapacitorCount = getHeatCapacitorCount(side);
        if (heatCapacitorCount == 1) {
            return getTemperature(0, side).copy();
        }
        FloatingLong sum = FloatingLong.ZERO;
        FloatingLong totalCapacity = getTotalHeatCapacity(side);
        for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum = sum.plusEqual(getTemperature(capacitor, side).multiply(getHeatCapacity(capacitor, side).divide(totalCapacity)));
        }
        return sum;
    }

    /**
     * A sided variant of {@link IHeatHandler#getTotalInverseConductionCoefficient()}, docs copied for convenience.
     *
     * Calculates the total inverse conduction coefficient across all capacitors in this handler.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The total inverse conduction coefficient across all capacitors in this handler.
     *
     * @apiNote Must be greater than {@link FloatingLong#ZERO}, and the returned {@link FloatingLong} can be safely modified afterwards.
     */
    default FloatingLong getTotalInverseConductionCoefficient(@Nullable Direction side) {
        int heatCapacitorCount = getHeatCapacitorCount(side);
        if (heatCapacitorCount == 0) {
            return HeatAPI.DEFAULT_INVERSE_CONDUCTION;
        } else if (heatCapacitorCount == 1) {
            return getInverseConduction(0, side).copy();
        }
        FloatingLong sum = FloatingLong.ZERO;
        for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum = sum.plusEqual(getInverseConduction(capacitor, side));
        }
        return sum;
    }

    /**
     * A sided variant of {@link IHeatHandler#getTotalHeatCapacity()}, docs copied for convenience.
     *
     * Calculates the total heat capacity across all capacitors in this handler.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The total heat capacity across all capacitors in this handler.
     *
     * @apiNote The returned {@link FloatingLong} can be safely modified afterwards.
     */
    default FloatingLong getTotalHeatCapacity(@Nullable Direction side) {
        int heatCapacitorCount = getHeatCapacitorCount(side);
        if (heatCapacitorCount == 1) {
            return getHeatCapacity(0, side).copy();
        }
        FloatingLong sum = FloatingLong.ZERO;
        for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum = sum.plusEqual(getHeatCapacity(capacitor, side));
        }
        return sum;
    }

    /**
     * A sided variant of {@link IHeatHandler#handleHeat(HeatPacket)}, docs copied for convenience.
     *
     * Handles transferring a {@link HeatPacket} to this handler.
     *
     * @param transfer The {@link HeatPacket} being transferred.
     * @param side     The side we are interacting with the handler from (null for internal).
     *
     * @implNote Default implementation evenly distributes it between stored capacitors
     */
    default void handleHeat(HeatPacket transfer, @Nullable Direction side) {
        int heatCapacitorCount = getHeatCapacitorCount(side);
        if (heatCapacitorCount == 1) {
            handleHeat(0, transfer, side);
        } else {
            FloatingLong totalHeatCapacity = getTotalHeatCapacity(side);
            for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
                handleHeat(capacitor, transfer.split(getHeatCapacity(capacitor, side).divideToLevel(totalHeatCapacity)), side);
            }
        }
    }
}
