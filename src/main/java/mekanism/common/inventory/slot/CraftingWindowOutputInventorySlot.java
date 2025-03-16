package mekanism.common.inventory.slot;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.inventory.container.slot.VirtualCraftingOutputSlot;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class CraftingWindowOutputInventorySlot extends CraftingWindowInventorySlot {

    public static CraftingWindowOutputInventorySlot create(QIOCraftingWindow window) {
        return new CraftingWindowOutputInventorySlot(window);
    }

    private CraftingWindowOutputInventorySlot(QIOCraftingWindow window) {
        super(ConstantPredicates.manualOnly(), ConstantPredicates.internalOnly(), window, null, null);
    }

    @NotNull
    @Override
    public VirtualInventoryContainerSlot createContainerSlot() {
        return new VirtualCraftingOutputSlot(this, getSlotOverlay(), this::setStackUnchecked, craftingWindow);
    }
}