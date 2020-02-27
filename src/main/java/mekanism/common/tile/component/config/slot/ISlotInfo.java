package mekanism.common.tile.component.config.slot;

public interface ISlotInfo {

    boolean canInput();

    boolean canOutput();

    default boolean isEnabled() {
        return canInput() || canOutput();
    }
}