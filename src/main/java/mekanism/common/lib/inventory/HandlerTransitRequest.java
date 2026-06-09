package mekanism.common.lib.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import mekanism.common.Mekanism;
import mekanism.common.lib.transaction.SimpleIntegerJournal;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class HandlerTransitRequest extends CollectionTransitRequest {

    @Nullable
    protected ResourceHandler<ItemResource> handler;
    private Map<ItemResource, HandlerItemData> itemMap = Collections.emptyMap();

    public HandlerTransitRequest(@Nullable ResourceHandler<ItemResource> handler) {
        this.handler = handler;
    }

    public void addItem(ItemResource type, int amount, int slot) {
        if (!type.isEmpty() && amount > 0) {//Validate to make sure we aren't somehow adding an empty resource to the map
            if (itemMap.isEmpty()) {
                itemMap = new LinkedHashMap<>();
            }
            itemMap.computeIfAbsent(type, HandlerItemData::new).addSlot(slot, amount);
        }
    }

    public int getCount(ItemResource itemType) {
        ItemData data = itemMap.get(itemType);
        return data == null ? 0 : data.getTotalCount();
    }

    @Override
    protected Collection<? extends ItemData> getItemData() {
        return itemMap.values();
    }

    @Override
    public boolean isEmpty() {
        //Skip the values call
        if (itemMap.isEmpty()) {
            return true;
        }
        for (HandlerItemData data : itemMap.values()) {
            if (data.getTotalCount() > 0) {
                return false;
            }
        }
        return true;
    }

    public class HandlerItemData extends ItemData {

        private final Int2ObjectMap<AmountJournal> slotMap = new Int2ObjectLinkedOpenHashMap<>();

        public HandlerItemData(ItemResource itemType) {
            super(itemType);
        }

        public void addSlot(int slot, int amount) {
            //TODO - 26.1: Evaluate not bothering to keep track of the slot index, and just letting the handler figure out how it wants things extracted?
            slotMap.put(slot, new AmountJournal(slot, amount));
            totalCount += amount;
        }

        @Override
        public int use(int amount, @Nullable TransactionContext transaction) {
            ItemResource itemType = getItemType();
            if (handler == null || slotMap.isEmpty()) {
                return 0;
            }
            try (Transaction subTransaction = Transaction.open(transaction)) {
                int usedSoFar = 0;
                for (ObjectIterator<Int2ObjectMap.Entry<AmountJournal>> iterator = Int2ObjectMaps.fastIterator(slotMap); iterator.hasNext(); ) {
                    Int2ObjectMap.Entry<AmountJournal> entry = iterator.next();
                    AmountJournal currentAmount = entry.getValue();
                    int toUse = Math.min(amount - usedSoFar, currentAmount.value);
                    if (toUse == 0) {//If we are being called before committing all the changes, our value might be zero
                        continue;
                    }
                    int slot = entry.getIntKey();
                    int extracted = handler.extract(slot, itemType, toUse, subTransaction);
                    if (extracted < toUse) { // be loud if an InvStack's prediction doesn't line up
                        //TODO - 26.1: Re-evaluate this, and if we actually care about being loud here now that nothing is voided or duped
                        Mekanism.logger.warn("An inventory's returned content count does not line up with HandlerTransitRequest's prediction.");
                        Mekanism.logger.warn("HandlerTransitRequest slot: {}, item: {}, toUse: {}, stored type: {}, extracted: {}", slot, itemType, toUse,
                              handler.getResource(slot), extracted);
                        Mekanism.logger.warn("ResourceHandler<ItemResource>: {}", handler.getClass().getName());
                        //Return that we failed to extract the expected amount, which will lead to us rolling back, and then our caller also rolling back the transfer
                        return 0;
                    }
                    usedSoFar += extracted;
                    //Note: If there is already a snapshot taken, updateSnapshots won't bother taking a new one that overwrites this
                    // so it is safe for us to do here, directly before adjusting the total count, rather than forcing us to do it before the loop
                    updateSnapshots(subTransaction);
                    totalCount -= extracted;
                    currentAmount.updateSnapshots(subTransaction);
                    currentAmount.value -= extracted;
                    if (usedSoFar == amount) {
                        break;
                    }
                }
                subTransaction.commit();
                return usedSoFar;
            }
        }

        @Override
        protected void onRootCommit(Integer originalState) {
            super.onRootCommit(originalState);
            if (totalCount == 0) {//This item is no longer stored, remove it
                itemMap.remove(getItemType());
            }
        }

        private class AmountJournal extends SimpleIntegerJournal {

            private final int slot;

            public AmountJournal(int slot, int amount) {
                super(amount);
                this.slot = slot;
            }

            @Override
            protected void onRootCommit(Integer originalState) {
                super.onRootCommit(originalState);
                if (value == 0) {//There is no more stored in this slot, remove it
                    slotMap.remove(slot);
                }
            }
        }
    }
}