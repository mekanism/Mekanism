package mekanism.common.capabilities.resolver.manager;

import java.util.Collections;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.proxy.ProxyItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class to make reading instead of having as messy generics
 */
public class ItemHandlerManager extends CapabilityHandlerManager<IInventorySlotHolder, IInventorySlot, ResourceHandler<ItemResource>> {

    public ItemHandlerManager(@Nullable IInventorySlotHolder holder, @NotNull IContentsListener changeListener) {
        super(holder, Capabilities.ITEM.block(), IInventorySlotHolder::getInventorySlots, (side, h) -> new ProxyItemHandler(new IMekanismInventory() {
            @Override
            public void onContentsChanged() {
                changeListener.onContentsChanged();
            }

            @NotNull
            @Override
            public List<IInventorySlot> getInventorySlots() {
                //Note: This instance of check should always pass, but we have it in case we are passed a null holder
                return h instanceof IInventorySlotHolder slotHolder ? slotHolder.getInventorySlots(side) : Collections.emptyList();
            }
        }, side, h));
    }
}