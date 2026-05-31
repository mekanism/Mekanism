package mekanism.common.util;

import mekanism.common.inventory.access.SideEffectFreeItemAccess;
import net.minecraft.core.TypedInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.LivingEntityEquipmentWrapper;
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

    public static ItemAccess forEntitySlot(LivingEntity entity, EquipmentSlot slot) {
        ResourceHandler<ItemResource> handler = LivingEntityEquipmentWrapper.of(entity, slot);
        //TODO - 26.1: Does this need to be a strict handler?
        return ItemAccess.forHandlerIndexStrict(handler, 0);
    }

    //TODO - 26.1: Re-evaluate usages and add docs stating assumptions around using this
    // Also document that it is sometimes preferred to use this over ItemAccess#forStack when no side effects should happen just to make sure that the stack doesn't get mutated
    public static ItemAccess queryOnlyAccess(TypedInstance<Item> itemType) {
        if (itemType instanceof ItemResource resource) {
            return new SideEffectFreeItemAccess(resource);
        }
        return new SideEffectFreeItemAccess(ItemResource.of(itemType.typeHolder()));
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