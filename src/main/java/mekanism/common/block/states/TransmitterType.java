package mekanism.common.block.states;

public enum TransmitterType {
    UNIVERSAL_CABLE(Size.SMALL),
    MECHANICAL_PIPE(Size.LARGE),
    PRESSURIZED_TUBE(Size.SMALL),
    LOGISTICAL_TRANSPORTER(Size.LARGE),
    RESTRICTIVE_TRANSPORTER(Size.LARGE),
    DIVERSION_TRANSPORTER(Size.LARGE),
    THERMODYNAMIC_CONDUCTOR(Size.SMALL);

    private final Size size;

    TransmitterType(Size size) {
        this.size = size;
    }

    public Size getSize() {
        return size;
    }

    public enum Size {
        SMALL(6),
        LARGE(8);

        public final int centerSize;

        Size(int size) {
            centerSize = size;
        }
    }
}