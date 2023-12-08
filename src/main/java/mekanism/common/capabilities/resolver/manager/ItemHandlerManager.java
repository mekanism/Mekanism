package mekanism.common.capabilities.resolver.manager;

import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.ISidedItemHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.proxy.ProxyItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class to make reading instead of having as messy generics
 */
public class ItemHandlerManager extends CapabilityHandlerManager<IInventorySlotHolder, IInventorySlot, IItemHandler, ISidedItemHandler> {

    public ItemHandlerManager(@Nullable IInventorySlotHolder holder, @NotNull ISidedItemHandler baseHandler) {
        super(holder, baseHandler, Capabilities.ITEM.block(), ProxyItemHandler::new, IInventorySlotHolder::getInventorySlots);
    }
}