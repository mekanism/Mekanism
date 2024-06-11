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

@NothingNullByDefault
public class ComponentBackedItemHandler extends ComponentBackedHandler<ItemStack, IInventorySlot, AttachedItems> implements IMekanismInventory {

    public ComponentBackedItemHandler(ItemStack attachedTo, int totalSlots) {
        super(attachedTo, totalSlots);
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
        return size();
    }

    @Override
    public ItemStack getStackInSlot(int slot, @Nullable Direction side) {
        return getContents(slot);
    }

    @Override
    public boolean isInventoryEmpty(@Nullable Direction side) {
        for (ItemStack item : getAttached()) {
            if (!item.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}