package mekanism.common.lib.inventory;

import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import mekanism.common.Mekanism;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class HandlerTransitRequest extends CollectionTransitRequest {

    private final ResourceHandler<ItemResource> handler;
    private Map<ItemResource, HandlerItemData> itemMap = Collections.emptyMap();

    public HandlerTransitRequest(ResourceHandler<ItemResource> handler) {
        this.handler = handler;
    }

    public void addItem(ItemStack stack, int slot) {
        ItemResource type = ItemResource.of(stack);
        if (itemMap.isEmpty()) {
            itemMap = new LinkedHashMap<>();
        }
        itemMap.computeIfAbsent(type, HandlerItemData::new).addSlot(slot, stack);
    }

    public int getCount(ItemResource itemType) {
        ItemData data = itemMap.get(itemType);
        return data == null ? 0 : data.getTotalCount();
    }

    protected ResourceHandler<ItemResource> getHandler() {
        return handler;
    }

    @Override
    public Collection<HandlerItemData> getItemData() {
        return itemMap.values();
    }

    @Override
    public boolean isEmpty() {
        //Skip the values call
        return itemMap.isEmpty();
    }

    public class HandlerItemData extends ItemData {

        private final Int2IntMap slotMap = new Int2IntLinkedOpenHashMap();

        public HandlerItemData(ItemResource itemType) {
            super(itemType);
        }

        public void addSlot(int id, ItemStack stack) {
            slotMap.put(id, stack.count());
            totalCount += stack.count();
        }

        @Override
        public ItemStack use(int amount) {
            ResourceHandler<ItemResource> handler = getHandler();
            ItemResource itemType = getItemType();
            if (handler != null && !slotMap.isEmpty() && !itemType.isEmpty()) {//TODO - 26.1: Can item type even be empty?
                //TODO - 26.1: Evaluate callers, and see if we should be passing a transaction context from there that then we need to open as a sub transaction
                try (Transaction tx = Transaction.openRoot()) {
                    for (ObjectIterator<Int2IntMap.Entry> iterator = Int2IntMaps.fastIterator(slotMap); iterator.hasNext(); ) {
                        Int2IntMap.Entry entry = iterator.next();
                        int slot = entry.getIntKey();
                        int currentCount = entry.getIntValue();
                        int toUse = Math.min(amount, currentCount);
                        int extracted = handler.extract(slot, itemType, toUse, tx);
                        if (extracted != toUse) { // be loud if an InvStack's prediction doesn't line up
                            //Double check if the type that is stored even matches
                            Mekanism.logger.warn("An inventory's returned content count does not line up with HandlerTransitRequest's prediction.");
                            Mekanism.logger.warn("HandlerTransitRequest slot: {}, item: {}, toUse: {}, stored type: {}, extracted: {}", slot, itemType, toUse,
                                  handler.getResource(slot), extracted);
                            Mekanism.logger.warn("ResourceHandler<ItemResource>: {}", handler.getClass().getName());
                        }
                        //TODO - 26.1: Should this be adjusting by extracted instead of toUse? As if extracted != toUse it should always be less than toUse
                        // so realistically we should be adjusting by that reduced amount instead
                        amount -= toUse;
                        totalCount -= toUse;
                        if (totalCount == 0) {
                            itemMap.remove(itemType);
                        }
                        currentCount = currentCount - toUse;
                        if (currentCount == 0) {
                            //If we removed all items from this slot, remove the slot
                            iterator.remove();
                        } else {
                            // otherwise, update the amount in it
                            entry.setValue(currentCount);
                        }
                        if (amount == 0) {
                            break;
                        }
                    }
                    tx.commit();
                }
            }
            return getStack();
        }
    }
}