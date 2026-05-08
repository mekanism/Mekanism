package mekanism.common.attachments.containers.item;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.attachments.ComponentBackedResourceHandler;
import mekanism.common.attachments.containers.AttachedResources;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

@NothingNullByDefault
public class ComponentBackedItemHandler extends ComponentBackedResourceHandler<ItemResource, IInventorySlot> implements IMekanismInventory {

    public ComponentBackedItemHandler(ItemStack attachedTo, int totalSlots) {
        super(attachedTo, totalSlots);
    }

    @Override
    protected ContainerType<IInventorySlot, AttachedResources<ItemResource>, ?> containerType() {
        return ContainerType.ITEM;
    }
}