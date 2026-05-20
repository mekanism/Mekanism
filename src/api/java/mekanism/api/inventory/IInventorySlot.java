package mekanism.api.inventory;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Nullable;

/// Represents a [`resource container`][IResourceContainer] that contains [`items`][ItemResource].
@NothingNullByDefault
public interface IInventorySlot extends IResourceContainer<ItemResource> {

    /// {@return a slot that should be automatically added to container, or null if this slot should not be added}
    @Nullable
    default Slot createContainerSlot() {
        return null;
    }

    @Override
    @NonExtendable
    default LargeResourceStack.StackHelper<ItemResource> stackHelper() {
        return LargeResourceStack.ITEM_HELPER;
    }
}