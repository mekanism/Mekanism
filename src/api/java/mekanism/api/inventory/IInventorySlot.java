package mekanism.api.inventory;

import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.IResourceContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

//TODO - 26.1: make this implement/supply ItemAccess/ResourceHandler? It currently has a pseudo ItemHandler impl, so might be better to move everything away from single-slot context?
// Maybe extract a good portion of this to a super interface IResourceContainer?
@NothingNullByDefault
public interface IInventorySlot extends IResourceContainer<ItemResource> {

    /**
     * Returns the {@link ItemStack} in this {@link IInventorySlot}.
     * <p>
     * The result's stack size may be greater than the itemstack's max size.
     * <p>
     * If the result is empty, then the slot is empty.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link ItemStack} <em>MUST NOT</em> be modified. This method is not for altering an inventory's contents. Any implementers who
     * are able to detect modification through this method should throw an exception.
     * </p>
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED ITEMSTACK</em></strong>
     * </p>
     *
     * @return {@link ItemStack} in this {@link IInventorySlot}. Empty {@link ItemStack} if this {@link IInventorySlot} is empty.
     *
     * @apiNote <strong>IMPORTANT:</strong> Do not modify this {@link ItemStack}.
     */
    default ItemStack getStack() {//TODO - 26.1: Replace this with getResource and amount
        return getResource().toStack(amount());
    }

    /**
     * Overrides the stack in this {@link IInventorySlot}.
     *
     * @param stack {@link ItemStack} to set this slot to (may be empty).
     *
     * @throws RuntimeException if this slot is called in a way that it was not expecting.
     * @implNote If the internal stack does get updated make sure to call {@link #onContentsChanged()}
     */
    @Deprecated(forRemoval = true)//TODO - 26.1: Move calls to setContents(ItemResource, int)
    default void setStack(ItemStack stack) {
        setContents(ItemResource.of(stack), stack.count());
    }

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
    default void setEmpty() {
        setContents(ItemResource.EMPTY, 0);
    }

    @Override
    default void serialize(ValueOutput output) {
        if (!isEmpty()) {
            //TODO - 26.1: Reimplement this to save the resource and amount rather than having it as an oversized stack
            output.store(SerializationConstants.ITEM, SerializerHelper.OVERSIZED_ITEM_CODEC, getStack());
            /*ValueOutput itemOutput = output.child(SerializationConstants.ITEM);
            itemOutput.store(SerializationConstants.TYPE, ItemResource.CODEC, getResource());
            itemOutput.putInt(ItemInstance.FIELD_COUNT, amount());*/
        }
    }
}