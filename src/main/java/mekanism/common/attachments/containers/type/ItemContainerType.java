package mekanism.common.attachments.containers.type;

import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.slot.CraftingWindowOutputInventorySlot;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;

@NothingNullByDefault
public class ItemContainerType extends ResourceContainerType<ItemResource, IInventorySlot> {

    ItemContainerType() {
        super(MekanismDataComponents.ATTACHED_ITEMS, SerializationConstants.ITEMS, Capabilities.ITEM, LargeResourceStack.ITEM_HELPER);
    }

    @Override
    public ItemResource asResourceOrEmpty(Resource resource) {
        return resource instanceof ItemResource itemResource ? itemResource : emptyResource();
    }

    @Override
    protected boolean isFakeOutput(IInventorySlot slot) {
        return slot instanceof CraftingWindowOutputInventorySlot;
    }

    @Override
    public List<IInventorySlot> getContainers(TileEntityMekanism tile) {
        return tile.getInventorySlots();
    }

    @Override
    public boolean canHandle(TileEntityMekanism tile) {
        return tile.hasInventory();
    }
}