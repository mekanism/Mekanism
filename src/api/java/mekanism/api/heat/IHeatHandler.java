package mekanism.api.heat;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.math.FloatingLong;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IHeatHandler {

    /**
     * Returns the number of heat storage units ("capacitors") available
     *
     * @return The number of capacitors available
     */
    int getHeatCapacitorCount();

    /**
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
     *
     * @return Temperature of a given capacitor. {@link FloatingLong#ZERO} if the capacitor has a temperature of absolute zero.
     */
    FloatingLong getTemperature(int capacitor);

    /**
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
     *
     * @return Inverse conduction coefficient of a given capacitor.
     *
     * @apiNote Must be greater than {@link FloatingLong#ZERO}
     */
    FloatingLong getInverseConduction(int capacitor);

    /**
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
     *
     * @return Heat capacity of a given capacitor.
     *
     * @apiNote Must be at least {@link FloatingLong#ONE}
     */
    FloatingLong getHeatCapacity(int capacitor);

    /**
     * Handles transferring a {@link HeatPacket} to the given capacitor.
     *
     * @param capacitor Capacitor to target
     * @param transfer  The {@link HeatPacket} being transferred.
     */
    void handleHeat(int capacitor, HeatPacket transfer);

    /**
     * Calculates the total temperature across all capacitors in this handler.
     *
     * @return The total temperature across all capacitors in this handler.
     *
     * @apiNote The returned {@link FloatingLong} can be safely modified afterwards.
     */
    default FloatingLong getTotalTemperature() {
        int heatCapacitorCount = getHeatCapacitorCount();
        if (heatCapacitorCount == 1) {
            return getTemperature(0).copy();
        }
        FloatingLong sum = FloatingLong.ZERO;
        FloatingLong totalCapacity = getTotalHeatCapacity();
        for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum = sum.plusEqual(getTemperature(capacitor).multiply(getHeatCapacity(capacitor).divide(totalCapacity)));
        }
        return sum;
    }

    /**
     * Calculates the total inverse conduction coefficient across all capacitors in this handler.
     *
     * @return The total inverse conduction coefficient across all capacitors in this handler.
     *
     * @apiNote Must be greater than {@link FloatingLong#ZERO}, and the returned {@link FloatingLong} can be safely modified afterwards.
     */
    default FloatingLong getTotalInverseConductionCoefficient() {
        int heatCapacitorCount = getHeatCapacitorCount();
        if (heatCapacitorCount == 0) {
            return HeatAPI.DEFAULT_INVERSE_CONDUCTION;
        } else if (heatCapacitorCount == 1) {
            return getInverseConduction(0).copy();
        }
        FloatingLong sum = FloatingLong.ZERO;
        for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum = sum.plusEqual(getInverseConduction(capacitor));
        }
        return sum;
    }

    /**
     * Calculates the total heat capacity across all capacitors in this handler.
     *
     * @return The total heat capacity across all capacitors in this handler.
     *
     * @apiNote The returned {@link FloatingLong} can be safely modified afterwards.
     */
    default FloatingLong getTotalHeatCapacity() {
        int heatCapacitorCount = getHeatCapacitorCount();
        if (heatCapacitorCount == 1) {
            return getHeatCapacity(0).copy();
        }
        FloatingLong sum = FloatingLong.ZERO;
        for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum = sum.plusEqual(getHeatCapacity(capacitor));
        }
        return sum;
    }

    /**
     * Handles transferring a {@link HeatPacket} to this handler.
     *
     * @param transfer The {@link HeatPacket} being transferred.
     *
     * @implNote Default implementation evenly distributes it between stored capacitors
     */
    default void handleHeat(HeatPacket transfer) {
        int heatCapacitorCount = getHeatCapacitorCount();
        if (heatCapacitorCount == 1) {
            handleHeat(0, transfer);
        } else {
            FloatingLong totalHeatCapacity = getTotalHeatCapacity();
            for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
                handleHeat(capacitor, transfer.split(getHeatCapacity(capacitor).divideToLevel(totalHeatCapacity)));
            }
        }
    }
}