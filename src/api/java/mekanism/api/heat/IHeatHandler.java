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

    FloatingLong getInverseConductionCoefficient(int capacitor);

    FloatingLong getInsulationCoefficient(int capacitor);

    //TODO: Do not allow this to be zero
    FloatingLong getHeatCapacity(int capacitor);

    void handleTemperatureChange(int capacitor, TemperaturePacket transfer);

    default void handleTemperatureChange(TemperaturePacket transfer) {
        //TODO: Implement me and split it evenly across all the capacitors, HeatAPI#handleTemperatureChange
    }
}