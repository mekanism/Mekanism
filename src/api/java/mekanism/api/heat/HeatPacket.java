package mekanism.api.heat;

import mekanism.api.math.FloatingLong;

/**
 * Represents a transfer of heat into or out of a capacitor.
 */
public class HeatPacket {

    private TransferType type;
    private FloatingLong amount;

    public HeatPacket(TransferType type, FloatingLong amount) {
        this.type = type;
        this.amount = amount;
    }

    /**
     * @return the type of transfer this {@link HeatPacket} represents.
     */
    public TransferType getType() {
        return type;
    }

    /**
     * @return the amount of heat being transferred by this {@link HeatPacket}.
     */
    public FloatingLong getAmount() {
        return amount;
    }

    /**
     * Gets a partial split of this {@link HeatPacket} based on the given ratio.
     *
     * @param ratio The ratio to split this packet with.
     *
     * @return A partial split of this {@link HeatPacket} based on the given ratio.
     */
    public HeatPacket split(double ratio) {
        return new HeatPacket(type, amount.multiply(ratio));
    }

    /**
     * Merges this {@link HeatPacket} with another {@link HeatPacket}
     *
     * @param packet The {@link HeatPacket} to merge into this {@link HeatPacket}
     */
    public void merge(HeatPacket packet) {
        if ((type.absorb() && packet.type.absorb()) || (type.emit() && packet.type.emit())) {
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
