package mekanism.common.tile.component.config.slot;

//TODO: Verify and make sure we actually use canInput and canOutput
// Easiest way would probably to modify our Proxies to disallow if they are for config types and don't have access to a specific interaction type
public interface ISlotInfo {

    boolean canInput();

    boolean canOutput();

    default boolean isEnabled() {
        return canInput() || canOutput();
    }
}