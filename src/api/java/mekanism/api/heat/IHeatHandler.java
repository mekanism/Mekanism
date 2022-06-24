package mekanism.api.heat;

import mekanism.api.annotations.NothingNullByDefault;

@NothingNullByDefault
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
     * @param capacitor Capacitor to query.
     *
     * @return Temperature of a given capacitor.
     */
    double getTemperature(int capacitor);

    /**
     * Returns the inverse conduction coefficient of a given capacitor. This value defines how much heat is allowed to be dissipated. The larger the number the less heat
     * can dissipate. The trade-off is that it also allows for lower amounts of heat to be inserted.
     *
     * @param capacitor Capacitor to query.
     *
     * @return Inverse conduction coefficient of a given capacitor.
     *
     * @apiNote Must be at least 1.
     */
    double getInverseConduction(int capacitor);

    /**
     * Returns the heat capacity of a given capacitor. This number can be thought of as the specific heat of the capacitor.
     *
     * @param capacitor Capacitor to query.
     *
     * @return Heat capacity of a given capacitor.
     *
     * @apiNote Must be at least 1.
     */
    double getHeatCapacity(int capacitor);

    /**
     * Handles transferring a heat amount to the given capacitor.
     *
     * @param capacitor Capacitor to target
     * @param transfer  The amount of heat being transferred.
     */
    void handleHeat(int capacitor, double transfer);

    /**
     * Calculates the total temperature across all capacitors in this handler.
     *
     * @return The total temperature, taking into account all capacitors in this handler.
     */
    default double getTotalTemperature() {
        int heatCapacitorCount = getHeatCapacitorCount();
        if (heatCapacitorCount == 1) {
            return getTemperature(0);
        }
        double sum = 0;
        double totalCapacity = getTotalHeatCapacity();
        for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum += getTemperature(capacitor) * (getHeatCapacity(capacitor) / totalCapacity);
        }
        return sum;
    }

    /**
     * Calculates the total inverse conduction coefficient across all capacitors in this handler.
     *
     * @return The total inverse conduction coefficient across all capacitors in this handler.
     *
     * @apiNote Must be greater than {@code 0}.
     */
    default double getTotalInverseConduction() {
        int heatCapacitorCount = getHeatCapacitorCount();
        if (heatCapacitorCount == 0) {
            return HeatAPI.DEFAULT_INVERSE_CONDUCTION;
        } else if (heatCapacitorCount == 1) {
            return getInverseConduction(0);
        }
        double sum = 0;
        double totalCapacity = getTotalHeatCapacity();
        for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum += getInverseConduction(capacitor) * (getHeatCapacity(capacitor) / totalCapacity);
        }
        return sum;
    }

    /**
     * Calculates the total heat capacity across all capacitors in this handler.
     *
     * @return The total heat capacity across all capacitors in this handler.
     */
    default double getTotalHeatCapacity() {
        int heatCapacitorCount = getHeatCapacitorCount();
        if (heatCapacitorCount == 1) {
            return getHeatCapacity(0);
        }
        double sum = 0;
        for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum += getHeatCapacity(capacitor);
        }
        return sum;
    }

    /**
     * Handles a change of heat in this block. Can be positive or negative.
     *
     * @param transfer The amount being transferred.
     *
     * @implNote Default implementation splits it up to stored capacitors by heat capacity weightings.
     */
    default void handleHeat(double transfer) {
        int heatCapacitorCount = getHeatCapacitorCount();
        if (heatCapacitorCount == 1) {
            handleHeat(0, transfer);
        } else {
            double totalHeatCapacity = getTotalHeatCapacity();
            for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
                handleHeat(capacitor, transfer * (getHeatCapacity(capacitor) / totalHeatCapacity));
            }
        }
    }
}