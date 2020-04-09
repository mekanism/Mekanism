package mekanism.api.heat;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.math.FloatingLong;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IHeatHandler {

    //TODO: Finish documenting this other Heat api interfaces

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

    //TODO: Document as non zero
    FloatingLong getInverseConduction(int capacitor);

    //TODO: Document as being at least one
    FloatingLong getHeatCapacity(int capacitor);

    void handleHeat(int capacitor, HeatPacket transfer);

    default FloatingLong getTotalTemperature() {
        FloatingLong sum = FloatingLong.ZERO;
        FloatingLong totalCapacity = getTotalHeatCapacity();
        for (int capacitor = 0; capacitor < getHeatCapacitorCount(); capacitor++) {
            sum = sum.plusEqual(getTemperature(capacitor).multiply(getHeatCapacity(capacitor).divide(totalCapacity)));
        }
        return sum;
    }

    default FloatingLong getTotalInverseConductionCoefficient() {
        FloatingLong sum = FloatingLong.ZERO;
        for (int capacitor = 0; capacitor < getHeatCapacitorCount(); capacitor++) {
            sum = sum.plusEqual(getInverseConduction(capacitor));
        }
        return sum;
    }

    default FloatingLong getTotalHeatCapacity() {
        FloatingLong sum = FloatingLong.ZERO;
        for (int capacitor = 0; capacitor < getHeatCapacitorCount(); capacitor++) {
            sum = sum.plusEqual(getHeatCapacity(capacitor));
        }
        return sum;
    }

    default void handleHeat(HeatPacket transfer) {
        FloatingLong totalHeatCapacity = getTotalHeatCapacity();
        for (int capacitor = 0; capacitor < getHeatCapacitorCount(); capacitor++) {
            handleHeat(capacitor, transfer.split(getHeatCapacity(capacitor).divideToLevel(totalHeatCapacity)));
        }
    }
}