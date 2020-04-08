package mekanism.api.heat;

import mekanism.api.math.FloatingLong;

public class TemperaturePacket {

    private TransferType type;
    private FloatingLong amount;

    public TemperaturePacket(TransferType type, FloatingLong amount) {
        this.type = type;
        this.amount = amount;
    }

    public TransferType getType() {
        return type;
    }

    public FloatingLong getAmount() {
        return amount;
    }

    public TemperaturePacket split(double ratio) {
        return new TemperaturePacket(type, amount.multiply(ratio));
    }

    public enum TransferType {
        /** Receiving heat energy. */
        ABSORB,
        /** Emitting heat energy. */
        EMIT;

        public boolean absorb() {
            return this == ABSORB;
        }

        public boolean emit() {
            return this == EMIT;
        }
    }
}
