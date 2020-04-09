package mekanism.api.heat;

import mekanism.api.math.FloatingLong;

public class HeatPacket {

    private TransferType type;
    private FloatingLong amount;

    public HeatPacket(TransferType type, FloatingLong amount) {
        this.type = type;
        this.amount = amount;
    }

    public TransferType getType() {
        return type;
    }

    public FloatingLong getAmount() {
        return amount;
    }

    public HeatPacket split(double ratio) {
        return new HeatPacket(type, amount.multiply(ratio));
    }

    public void merge(HeatPacket packet) {
        if ((type == TransferType.ABSORB && packet.type == TransferType.ABSORB) || (type == TransferType.EMIT && packet.type == TransferType.EMIT)) {
            amount = amount.plusEqual(packet.amount);
        } else if (amount.greaterThan(packet.amount)) {
            amount = amount.minusEqual(packet.amount);
        } else {
            amount = packet.amount.minusEqual(amount);
            type = packet.type;
        }
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
