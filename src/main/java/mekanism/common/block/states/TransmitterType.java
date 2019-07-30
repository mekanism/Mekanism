package mekanism.common.block.states;

import mekanism.api.transmitters.TransmissionType;

public enum TransmitterType {
    UNIVERSAL_CABLE(Size.SMALL, TransmissionType.ENERGY),
    MECHANICAL_PIPE(Size.LARGE, TransmissionType.FLUID),
    PRESSURIZED_TUBE(Size.SMALL, TransmissionType.GAS),
    LOGISTICAL_TRANSPORTER(Size.LARGE, TransmissionType.ITEM),
    RESTRICTIVE_TRANSPORTER(Size.LARGE, TransmissionType.ITEM),
    DIVERSION_TRANSPORTER(Size.LARGE, TransmissionType.ITEM),
    THERMODYNAMIC_CONDUCTOR(Size.SMALL, TransmissionType.HEAT);

    private Size size;
    private TransmissionType transmissionType;

    TransmitterType(Size s, TransmissionType type) {
        size = s;
        transmissionType = type;
    }

    public static TransmitterType get(int meta) {
        return TransmitterType.values()[meta];
    }

    public Size getSize() {
        return size;
    }

    public TransmissionType getTransmission() {
        return transmissionType;
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