package mekanism.api.inventory;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.IMekanismResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

@NothingNullByDefault//TODO - 26.1: Docs and generify to support other resource types
public interface IMekanismInventory extends IMekanismResourceHandler<ItemResource, IInventorySlot> {

    //@Override
    default void setStackInSlot(int slot, ItemResource itemType, int amount) {//TODO - 26.1: Re-evaluate, previously was in IItemHandlerModifiable
        IInventorySlot inventorySlot = getContainer(slot);
        if (inventorySlot != null) {
            inventorySlot.setContents(itemType, amount);
        }
    }

    @Override
    default ItemResource getEmptyResource() {
        return ItemResource.EMPTY;
    }
}