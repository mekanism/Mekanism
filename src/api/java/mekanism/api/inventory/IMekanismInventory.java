package mekanism.api.inventory;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.IMekanismResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

@NothingNullByDefault//TODO - 26.1: Docs and generify to support other resource types
public interface IMekanismInventory extends IMekanismResourceHandler<ItemResource, IInventorySlot> {

    @Override
    default ItemResource getEmptyResource() {
        return ItemResource.EMPTY;
    }
}