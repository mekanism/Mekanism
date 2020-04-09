package mekanism.api.heat;

import mekanism.api.math.FloatingLong;

public class HeatAPI {

    /**
     * Default heat capacity
     */
    public static final FloatingLong DEFAULT_HEAT_CAPACITY = FloatingLong.ONE;
    /**
     * Default inverse conduction coefficient
     */
    public static final FloatingLong DEFAULT_INVERSE_CONDUCTION = FloatingLong.ONE;
    /**
     * Default inverse insulation coefficient
     */
    public static final FloatingLong DEFAULT_INVERSE_INSULATION = FloatingLong.ZERO;
    /**
     * The value of the zero point of our temperature scale in kelvin (room temperature)
     */
    public static final FloatingLong AMBIENT_TEMP = FloatingLong.createConst(300);
    /**
     * The heat transfer coefficient for air
     */
    public static final FloatingLong AIR_INVERSE_COEFFICIENT = FloatingLong.createConst(10_000);

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
