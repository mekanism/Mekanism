package mekanism.api.heat;

import mekanism.api.math.FloatingLong;

public class HeatAPI {

    /**
     * The value of the zero point of our temperature scale in kelvin
     */
    public static final FloatingLong AMBIENT_TEMP = FloatingLong.createConst(273);

    /**
     * The heat transfer coefficient for air
     */
    public static final FloatingLong AIR_INVERSE_COEFFICIENT = FloatingLong.createConst(10_000);

    public static FloatingLong getTotalTemperature(IHeatHandler handler) {
        FloatingLong sum = FloatingLong.ZERO;
        for (int capacitor = 0; capacitor < handler.getHeatCapacitorCount(); capacitor++) {
            sum = sum.plusEqual(handler.getTemperature(capacitor).multiply(handler.getHeatCapacity(capacitor).divide(handler.getHeatCapacity(capacitor))));
        }
        return sum;
    }

    public static FloatingLong getTotalInverseConductionCoefficient(IHeatHandler handler) {
        FloatingLong sum = FloatingLong.ZERO;
        for (int capacitor = 0; capacitor < handler.getHeatCapacitorCount(); capacitor++) {
            sum = sum.plusEqual(handler.getInverseConductionCoefficient(capacitor));
        }
        return sum;
    }

    public static FloatingLong getTotalHeatCapacity(IHeatHandler handler) {
        FloatingLong sum = FloatingLong.ZERO;
        for (int capacitor = 0; capacitor < handler.getHeatCapacitorCount(); capacitor++) {
            sum = sum.plusEqual(handler.getHeatCapacity(capacitor));
        }
        return sum;
    }

    public static void handleTemperatureChange(IHeatHandler handler, TemperaturePacket packet) {
        //TODO: FIXME
        /*double totalHeatCapacity = handler.getHeatCapacity();
        for (IHeatCapacitor capacitor : getCapacitors(side)) {
            capacitor.handleHeatChange(transfer.split(capacitor.getHeatCapacity() / totalHeatCapacity));
        }*/
    }

    public static class HeatTransfer {

        private final FloatingLong adjacentTransfer;
        private final FloatingLong environmentTransfer;

        public HeatTransfer(FloatingLong adjacentTransfer, FloatingLong environmentTransfer) {
            this.adjacentTransfer = adjacentTransfer;
            this.environmentTransfer = environmentTransfer;
        }

        public FloatingLong getAdjacentTransfer() {
            return adjacentTransfer;
        }

        public FloatingLong getEnvironmentTransfer() {
            return environmentTransfer;
        }
    }
}
