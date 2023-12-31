package mekanism.common.network;

public enum MekClickType {
    LEFT,
    RIGHT,
    SHIFT_LEFT;

    public static MekClickType left(boolean holdingShift) {
        return holdingShift ? SHIFT_LEFT : LEFT;
    }
}