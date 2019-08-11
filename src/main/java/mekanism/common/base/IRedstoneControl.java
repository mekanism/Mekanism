package mekanism.common.base;

import mekanism.api.text.IHasTranslationKey;

public interface IRedstoneControl {

    /**
     * Gets the RedstoneControl type from this block.
     *
     * @return this block's RedstoneControl type
     */
    RedstoneControl getControlType();

    /**
     * Sets this block's RedstoneControl type to a new value.
     *
     * @param type - RedstoneControl type to set
     */
    void setControlType(RedstoneControl type);

    /**
     * If the block is getting powered or not by redstone (indirectly).
     *
     * @return if the block is getting powered indirectly
     */
    boolean isPowered();

    /**
     * If the block was getting powered or not by redstone, last tick. Used for PULSE mode.
     */
    boolean wasPowered();

    /**
     * If the machine can be pulsed.
     */
    boolean canPulse();

    enum RedstoneControl implements IHasTranslationKey {
        DISABLED("mekanism.control.disabled"),
        HIGH("mekanism.control.high"),
        LOW("mekanism.control.low"),
        PULSE("mekanism.control.pulse");

        private String display;

        RedstoneControl(String s) {
            display = s;
        }

        @Override
        public String getTranslationKey() {
            return display;
        }
    }
}