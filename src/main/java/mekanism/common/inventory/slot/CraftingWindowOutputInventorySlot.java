package mekanism.common.inventory.slot;

import mekanism.api.functions.ConstantPredicates;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.inventory.container.slot.VirtualCraftingOutputSlot;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import net.minecraft.world.entity.player.Player;

public class CraftingWindowOutputInventorySlot extends CraftingWindowInventorySlot {

    public static CraftingWindowOutputInventorySlot create(QIOCraftingWindow window) {
        return new CraftingWindowOutputInventorySlot(window);
    }

    private CraftingWindowOutputInventorySlot(QIOCraftingWindow window) {
        super(ConstantPredicates.manualOnly(), ConstantPredicates.internalOnly(), window, null, null);
    }

    @Override
    public VirtualInventoryContainerSlot createContainerSlot(Player player) {
        return new VirtualCraftingOutputSlot(this, getSlotOverlay(), player, craftingWindow);
    }
}