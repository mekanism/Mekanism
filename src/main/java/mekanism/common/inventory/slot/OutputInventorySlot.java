package mekanism.common.inventory.slot;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.inventory.container.slot.ContainerSlotType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OutputInventorySlot extends BasicInventorySlot {

    public static OutputInventorySlot at(@Nullable IMekanismInventory inventory, int x, int y) {
        return new OutputInventorySlot(inventory, x, y);
    }

    private OutputInventorySlot(@Nullable IMekanismInventory inventory, int x, int y) {
        super(alwaysTrueBi, (stack, automationType) -> automationType == AutomationType.INTERNAL, alwaysTrue, inventory, x, y);
    }

    @Override
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.OUTPUT;
    }
}