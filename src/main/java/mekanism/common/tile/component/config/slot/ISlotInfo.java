package mekanism.common.tile.component.config.slot;

public interface ISlotInfo {

    //TODO: Implement this
    default boolean canInput() {
        return true;
    }

    //TODO: Implement this
    default boolean canOutput() {
        return true;
    }

    default boolean isEnabled() {
        return canInput() || canOutput();
    }
}