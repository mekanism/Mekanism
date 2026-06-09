package mekanism.common.inventory.container.slot;

import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.container.SelectedWindowData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public interface ITransactionalSlot {

    //TODO: Improve these java docs at some point

    /// Basically a container slot's equivalent of [IInventorySlot#insert(Resource, int, TransactionContext, AutomationType)] with [AutomationType#MANUAL]
    int insert(ItemResource resource, int amount, TransactionContext transaction);

    /// Basically a container slot's equivalent of [IInventorySlot#extract(Resource, int, TransactionContext, AutomationType)] with [AutomationType#MANUAL]
    int extract(Player player, ItemResource resource, int amount, TransactionContext transaction);

    /// Used for determining if this slot can merge with the given stack when the stack is double-clicked.
    default boolean canMergeWith(ItemStack stack) {
        return true;
    }

    /// Used for determining if this slot "exists" when a given window is selected.
    ///
    /// @param windowData Data for currently selected "popup" window or null if there is no window visible.
    default boolean exists(@Nullable SelectedWindowData windowData) {
        return true;
    }
}