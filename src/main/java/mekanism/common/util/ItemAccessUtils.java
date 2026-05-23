package mekanism.common.util;

import mekanism.common.inventory.access.SideEffectFreeItemAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class ItemAccessUtils {

    private ItemAccessUtils() {
    }

    /// Similar to [ItemAccess#forPlayerInteraction(Player, InteractionHand)], except does not act as infinite for cases when the player is in creative.
    public static ItemAccess playerHandAccess(Player player, InteractionHand hand) {
        //TODO - 26.1: See if any usages of this should actually be forPlayerInteraction for creative player interaction of not mutating the initial stack
        return ItemAccess.forPlayerSlot(player, switch (hand) {
            case MAIN_HAND -> player.getInventory().getSelectedSlot();
            case OFF_HAND -> Inventory.SLOT_OFFHAND;
        });
    }

    //TODO - 26.1: Re-evaluate usages and add docs stating assumptions around using this
    public static ItemAccess queryOnlyAccess(ItemResource itemType) {
        return new SideEffectFreeItemAccess(itemType);
    }

    /// Helper method to exchange all the current resource in the given item access with the same amount of another.
    ///
    /// @param itemAccess  The item access to act on.
    /// @param newResource The resource of the items after the exchange. **Must be non-empty.**
    /// @param transaction The transaction that this operation is part of. Passing in `null` will open a root transaction, and commit it at the end of the method if
    /// everything was exchanged.
    ///
    /// @return Whether the exchange was successful.
    ///
    /// @throws IllegalArgumentException If the given resource is empty or the amount is negative. See also [TransferPreconditions#checkNonEmptyNonNegative] to help
    /// perform this check.
    /// @throws IllegalStateException    If the current resource is empty.
    /// @see ItemAccess#exchange(ItemResource, int, TransactionContext)
    public static boolean exchange(ItemAccess itemAccess, ItemResource newResource, @Nullable TransactionContext transaction) {
        return itemAccess.exchange(newResource, itemAccess.getAmount(), transaction) != 0;
    }
}