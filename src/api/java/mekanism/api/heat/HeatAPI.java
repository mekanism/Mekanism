package mekanism.api.heat;

import mekanism.api.math.FloatingLong;

public class HeatAPI {

    /**
     * The value of the zero point of our temperature scale in kelvin
     */
    double AMBIENT_TEMP = 300;

    /**
     * The heat transfer coefficient for air
     */
    double AIR_INVERSE_COEFFICIENT = 10000;

    public static class HeatTransfer {

        private FloatingLong adjacentTransfer = FloatingLong.ZERO;
        private FloatingLong environmentTransfer = FloatingLong.ZERO;

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
