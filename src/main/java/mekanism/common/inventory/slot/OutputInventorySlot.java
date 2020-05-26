package mekanism.common.inventory.slot;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.common.inventory.container.slot.ContainerSlotType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OutputInventorySlot extends BasicInventorySlot {

    public static OutputInventorySlot at(@Nullable IContentsListener listener, int x, int y) {
        return new OutputInventorySlot(listener, x, y);
    }

    private OutputInventorySlot(@Nullable IContentsListener listener, int x, int y) {
        super(alwaysTrueBi, internalOnly, alwaysTrue, listener, x, y);
        setSlotType(ContainerSlotType.OUTPUT);
    }
}