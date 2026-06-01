package mekanism.common.item.interfaces;

import java.util.List;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.attachments.containers.type.ContainerType;
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
    List<LargeResourceStack<ItemResource>> getDroppedSlots(ItemAccess itemAccess);

    @FunctionalInterface
    interface IDroppableAttachmentContents extends IDroppableContents {

        @Override
        boolean canContentsDrop(ItemResource itemType);

        @Override
        default List<LargeResourceStack<ItemResource>> getDroppedSlots(ItemAccess itemAccess) {
            //Note: Just directly interact with the attached contents as #getScalar will handle scaling the amounts
            return ContainerType.ITEM.getAttachedContents(itemAccess.getResource());
        }
    }
}