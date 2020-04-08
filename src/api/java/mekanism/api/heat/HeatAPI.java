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
