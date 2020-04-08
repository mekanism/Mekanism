package mekanism.api.heat;

import mekanism.api.math.FloatingLong;

public class HeatPacket {

    private Transfer type;
    private FloatingLong amount;

    public HeatPacket(Transfer type, FloatingLong amount) {
        this.type = type;
        this.amount = amount;
    }

    public Transfer getType() {
        return type;
    }

    public FloatingLong getAmount() {
        return amount;
    }

    public HeatPacket split(double ratio) {
        return new HeatPacket(type, amount.multiply(ratio));
    }

    public enum Transfer {
        /** Receiving heat energy. */
        ABSORB,
        /** Emitting heat energy. */
        EMIT;
    }
}
