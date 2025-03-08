package mekanism.common.inventory.slot;

import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class OutputInventorySlot extends BasicInventorySlot {

    public static OutputInventorySlot at(@Nullable IContentsListener listener, int x, int y) {
        return new OutputInventorySlot(listener, x, y);
    }

    private OutputInventorySlot(@Nullable IContentsListener listener, int x, int y) {
        super(ConstantPredicates.alwaysTrueBi(), ConstantPredicates.internalOnly(), ConstantPredicates.alwaysTrue(), listener, x, y);
        setSlotType(ContainerSlotType.OUTPUT);
    }
}