package mekanism.common.lib.recipetransfer;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public interface RecipePlacementHelper<RESULT extends @Nullable Object> {

    ResourceHandler<ItemResource> itemHandler();

    default boolean sendItemsToPlayerInventory(ItemResource resource, int amount, TransactionContext transaction) {
        return ResourceHandlerUtil.insertStacking(itemHandler(), resource, amount, transaction) == amount;
    }

    default RESULT successNoAction() {
        return success();
    }

    RESULT success();

    RESULT error();

    default RESULT missingItems(int slot) {
        return error();
    }

    default RESULT notEnoughRoom() {
        return error();
    }

    default RESULT unableToClearInput(int slot) {
        return error();
    }

    default void markInputUsed(int slotUsedFor, ItemResource resource, int amount) {
    }

    boolean useMaxItems();
}