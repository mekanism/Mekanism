package mekanism.api.inventory;

import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jspecify.annotations.Nullable;

/// Represents a [`resource container`][IResourceContainer] that contains [`items`][ItemResource].
public interface IInventorySlot extends IResourceContainer<ItemResource> {

    /// {@return a slot that should be automatically added to container, or null if this slot should not be added}
    ///
    /// @param player Player the container is being made for.
    @Nullable
    default Slot createContainerSlot(Player player) {
        return null;
    }

    /// {@return the resource in this container converted to a new item stack}
    ///
    /// @since 10.8.0
    default ItemStack asItemStack() {
        LargeResourceStack<ItemResource> stack = asStack();
        return stack.resource().toStack(stack.amountAsInt());
    }

    @Override
    @NonExtendable
    default LargeResourceStack.StackHelper<ItemResource> stackHelper() {
        return LargeResourceStack.ITEM_HELPER;
    }
}