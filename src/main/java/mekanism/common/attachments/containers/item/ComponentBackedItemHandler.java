package mekanism.common.attachments.containers.item;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.attachments.containers.ComponentBackedHandler;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

//TODO - 1.20.5: Can we make this in of itself a non serializable non syncable component or something?
// Unfortunately I don't think so because the attachedTo stack might change instances
@NothingNullByDefault
public class ComponentBackedItemHandler extends ComponentBackedHandler<ItemStack, IInventorySlot, AttachedItems> implements IMekanismInventory {

    public ComponentBackedItemHandler(ItemStack attachedTo) {
        super(attachedTo);
    }

    @Override
    protected ContainerType<IInventorySlot, AttachedItems, ?> containerType() {
        return ContainerType.ITEM;
    }

    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return getContainers();
    }

    @Nullable
    @Override
    public IInventorySlot getInventorySlot(int slot, @Nullable Direction side) {
        return getContainer(slot);
    }

    @Override
    public int getSlots(@Nullable Direction side) {
        return containerCount();
    }

    @Override
    public ItemStack getStackInSlot(int slot, @Nullable Direction side) {
        AttachedItems attachedItems = getAttached();
        return attachedItems == null ? ItemStack.EMPTY : attachedItems.get(slot);
    }

    @Override
    public boolean isInventoryEmpty(@Nullable Direction side) {
        AttachedItems attachedItems = getAttached();
        if (attachedItems == null) {
            return true;
        }
        for (ItemStack item : attachedItems) {
            if (!item.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}