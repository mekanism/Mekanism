package mekanism.api.inventory;

import com.mojang.serialization.Codec;
import mekanism.api.SerializerHelper;
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
    default Codec<LargeResourceStack<ItemResource>> resourceStackCodec() {
        return SerializerHelper.ITEM_RESOURCE_STACK_CODEC;
    }

    @Override
    @NonExtendable
    default LargeResourceStack<ItemResource> emptyStack() {
        return LargeResourceStack.EMPTY_ITEM_STACK;
    }
}