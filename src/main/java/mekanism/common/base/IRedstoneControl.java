package mekanism.common.base;

import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
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

    enum RedstoneControl implements IIncrementalEnum<RedstoneControl>, IHasTranslationKey {
        DISABLED("tooltip.mekanism.control.disabled"),
        HIGH("tooltip.mekanism.control.high"),
        LOW("tooltip.mekanism.control.low"),
        PULSE("tooltip.mekanism.control.pulse");

        private static final RedstoneControl[] MODES = values();
        private String display;

        RedstoneControl(String s) {
            display = s;
        }

        @Override
        public String getTranslationKey() {
            return display;
        }

        @Nonnull
        @Override
        public RedstoneControl byIndex(int index) {
            return byIndexStatic(index);
        }

        public static RedstoneControl byIndexStatic(int index) {
            //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
            return MODES[Math.floorMod(index, MODES.length)];
        }
    }
}