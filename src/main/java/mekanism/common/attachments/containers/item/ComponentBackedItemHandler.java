package mekanism.common.attachments.containers.item;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.attachments.containers.ComponentBackedHandler;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ComponentBackedItemHandler extends ComponentBackedHandler<ItemStack, IInventorySlot, AttachedItems> implements IMekanismInventory {

    public ComponentBackedItemHandler(ItemStack attachedTo, int totalSlots) {
        super(attachedTo, totalSlots);
    }

    @Override
    protected ContainerType<IInventorySlot, AttachedItems, ?> containerType() {
        return ContainerType.ITEM;
    }
}