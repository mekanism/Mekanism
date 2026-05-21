package mekanism.common.item.interfaces;

import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;

@FunctionalInterface
public interface IDroppableContents {

    default boolean canContentsDrop(ItemResource itemType) {
        return true;
    }

    default int getScalar(ItemAccess itemAccess) {
        return itemAccess.getAmount();
    }

    /**
     * Helper to get the inventory slots that should have their contents dropped into the world
     *
     * @apiNote Server side only.
     */
    List<IInventorySlot> getDroppedSlots(ItemStack stack);

    @FunctionalInterface
    interface IDroppableAttachmentContents extends IDroppableContents {

        @Override
        boolean canContentsDrop(ItemResource itemType);

        @Override
        default List<IInventorySlot> getDroppedSlots(ItemStack stack) {
            return ContainerType.ITEM.getAttachmentContainersIfPresent(stack);
        }
    }
}