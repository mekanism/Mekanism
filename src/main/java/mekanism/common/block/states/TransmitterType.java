package mekanism.common.block.states;

import mekanism.api.math.MathUtils;
import mekanism.api.transmitters.TransmissionType;

public enum TransmitterType {
    UNIVERSAL_CABLE(Size.SMALL, TransmissionType.ENERGY),
    MECHANICAL_PIPE(Size.LARGE, TransmissionType.FLUID),
    PRESSURIZED_TUBE(Size.SMALL, TransmissionType.GAS),//TODO - V10: Re-evaluate this, make pressurized tubes able to support multiple types
    LOGISTICAL_TRANSPORTER(Size.LARGE, TransmissionType.ITEM),
    RESTRICTIVE_TRANSPORTER(Size.LARGE, TransmissionType.ITEM),
    DIVERSION_TRANSPORTER(Size.LARGE, TransmissionType.ITEM),
    THERMODYNAMIC_CONDUCTOR(Size.SMALL, TransmissionType.HEAT);

    private static final TransmitterType[] TYPES = values();

    private final Size size;
    private final TransmissionType transmissionType;

    TransmitterType(Size size, TransmissionType type) {
        this.size = size;
        transmissionType = type;
    }

    public Size getSize() {
        return size;
    }

    public TransmissionType getTransmission() {
        return transmissionType;
    }

    public static TransmitterType byIndexStatic(int index) {
        return MathUtils.getByIndexMod(TYPES, index);
    }

    public enum Size {
        SMALL(6),
        LARGE(8);

        public int centerSize;

        Size(int size) {
            centerSize = size;
        }
    }
}