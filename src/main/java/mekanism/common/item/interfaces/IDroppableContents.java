package mekanism.common.item.interfaces;

import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface IDroppableContents {

    default boolean canContentsDrop(ItemStack stack) {
        return true;
    }

    default int getScalar(ItemStack stack) {
        return stack.getCount();
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
        boolean canContentsDrop(ItemStack stack);

        @Override
        default List<IInventorySlot> getDroppedSlots(ItemStack stack) {
            return ContainerType.ITEM.getAttachmentContainersIfPresent(stack);
        }
    }
}