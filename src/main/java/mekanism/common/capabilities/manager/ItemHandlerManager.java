package mekanism.common.capabilities.manager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.ISidedItemHandler;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.proxy.ProxyItemHandler;
import net.minecraftforge.items.IItemHandler;

/**
 * Helper class to make reading instead of having as messy generics
 */
public class ItemHandlerManager extends CapabilityHandlerManager<IInventorySlotHolder, IInventorySlot, IItemHandler, ISidedItemHandler> {

    public ItemHandlerManager(@Nullable IInventorySlotHolder holder, @Nonnull ISidedItemHandler baseHandler) {
        super(holder, baseHandler, ProxyItemHandler::new, IInventorySlotHolder::getInventorySlots);
    }

    public ItemHandlerManager(@Nullable IInventorySlotHolder holder, boolean canHandle, @Nonnull ISidedItemHandler baseHandler) {
        super(holder, canHandle, baseHandler, ProxyItemHandler::new, IInventorySlotHolder::getInventorySlots);
    }
}