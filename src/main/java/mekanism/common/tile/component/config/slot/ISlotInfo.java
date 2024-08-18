package mekanism.common.tile.component.config.slot;

public interface ISlotInfo {

    boolean canInput();

    boolean canOutput();

    default boolean isEnabled() {
        return canInput() || canOutput();
    }

    /**
     * Relevant to Output modes
     *
     * @return true if none of the slots contain anything to output, and ejecting can be skipped
     */
    boolean isEmpty();
}