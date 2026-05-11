package mekanism.api.inventory;

import com.mojang.serialization.Codec;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.IResourceContainer;
import mekanism.api.container.LargeResourceStack;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IInventorySlot extends IResourceContainer<ItemResource> {
    /**
     * Returns a slot for use in auto adding slots to a container.
     *
     * @return A slot for use in a container that represents this {@link IInventorySlot}, or null if this slot should not be added.
     */
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