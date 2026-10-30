package mekanism.common.inventory.slot;

import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class EntangloporterInventorySlot extends BasicInventorySlot {

    public static EntangloporterInventorySlot create(@Nullable IContentsListener listener) {
        return new EntangloporterInventorySlot(listener);
    }

    private EntangloporterInventorySlot(@Nullable IContentsListener listener) {
        super(ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(), null, null, listener, 0, 0);
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot(Player player) {
        //Make sure the slot doesn't get added to the container
        return null;
    }
}