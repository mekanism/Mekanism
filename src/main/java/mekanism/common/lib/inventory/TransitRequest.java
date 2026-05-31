package mekanism.common.lib.inventory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.item.TransporterItemHandler;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.inventory.TransitRequest.ItemData;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public abstract class TransitRequest implements Iterable<ItemData> {

    public static SimpleTransitRequest simple(ItemResource itemType, int amount) {
        return new SimpleTransitRequest(itemType, amount);
    }

    public static SimpleTransitRequest simple(TransporterStack stack) {
        return simple(stack.getItemType(), stack.size());
    }

    public static TransitRequest anyItem(ResourceHandler<ItemResource> inventory, int amount, @Nullable TransactionContext transaction) {
        return definedItem(inventory, amount, Finder.ANY, transaction);
    }

    public static TransitRequest definedItem(ResourceHandler<ItemResource> inventory, int amount, Finder finder, @Nullable TransactionContext transaction) {
        return definedItem(inventory, 1, amount, finder, transaction);
    }

    //TODO - 26.1: Evaluate what callers of this could just be replaced with using ResourceHandlerUtil calls
    public static TransitRequest definedItem(ResourceHandler<ItemResource> inventory, int min, int max, Finder finder, @Nullable TransactionContext transaction) {
        HandlerTransitRequest ret = new HandlerTransitRequest(inventory);
        if (inventory == null) {
            return ret;
        }
        try (Transaction simulation = Transaction.open(transaction)) {
            // count backwards: we start from the bottom of the inventory and go back for consistency
            for (int i = inventory.size() - 1; i >= 0; i--) {
                ItemResource itemType = inventory.getResource(i);
                if (itemType.isEmpty() || !finder.test(itemType)) {
                    continue;
                }
                //TODO - 26.1: Do we want to try and rework the handler transit requests to be able to ignore index?
                //TODO - 26.1: Should we be limiting the passed max based on the max stack size? extract used to be limited to a stack at a time
                int extractableAmount = inventory.extract(i, itemType, max, simulation);
                if (extractableAmount > 0) {
                    int toUse = Math.min(extractableAmount, max - ret.getCount(itemType));
                    if (toUse == 0) {
                        continue; // continue if we don't need any more of this item type
                    }
                    ret.addItem(itemType, toUse, i);
                }
            }
        }
        // remove items that we don't have enough of
        for (Iterator<ItemData> iterator = ret.iterator(); iterator.hasNext(); ) {
            if (iterator.next().getTotalCount() < min) {
                iterator.remove();
            }
        }
        return ret;
    }
    @NotNull
    public TransitResponse eject(BlockEntity outputter, @Nullable ResourceHandler<ItemResource> target, int min, @Nullable EnumColor outputColor,
          @NotNull TransactionContext transaction) {
        return eject(outputter, outputter.getBlockPos(), target, min, outputColor, transaction);
    }

    @NotNull
    public TransitResponse eject(BlockEntity outputter, BlockPos outputterPos, @Nullable ResourceHandler<ItemResource> target, int min,
          @Nullable EnumColor outputColor, @NotNull TransactionContext transaction) {
        if (isEmpty()) {//Short circuit if our request is empty
            return TransitResponse.EMPTY;
        } else if (target instanceof TransporterItemHandler cursed) {
            LogisticalTransporterBase transporter = cursed.getTransporter();
            //TODO - 26.1: Re-evaluate this, but I am fairly sure that if the color is null, then it basically is "no color" so would just take the color of the transporter it is being inserted directly into
            EnumColor color = outputColor == null ? transporter.getColor() : outputColor;
            return transporter.insert(outputter, outputterPos, this, color, min, transaction);
        }
        return addToInventoryUnchecked(target, min, transaction);
    }

    @NotNull
    public TransitResponse addToInventory(Level level, BlockPos pos, @Nullable ResourceHandler<ItemResource> inventory, int min, boolean force, @NotNull TransactionContext transaction) {
        if (isEmpty()) {//Short circuit if our request is empty
            return TransitResponse.EMPTY;
        } else if (force && WorldUtils.getTileEntity(level, pos) instanceof IAdvancedTransportEjector sorter) {
            return sorter.sendHome(this, transaction);
        }
        return addToInventoryUnchecked(inventory, min, transaction);
    }

    //Note: We are unchecked because we don't validate if we are empty or not
    @NotNull
    private TransitResponse addToInventoryUnchecked(@Nullable ResourceHandler<ItemResource> inventory, int min, @NotNull TransactionContext transaction) {
        if (inventory == null || inventory.size() == 0) {
            //If the inventory has no slots just exit early with the result that we can't send any items
            return TransitResponse.EMPTY;
        }
        for (ItemData data : this) {
            int count = data.getTotalCount();
            if (count > 0) {
                try (Transaction subTransaction = Transaction.open(transaction)) {
                    ItemResource itemType = data.getItemType();
                    int inserted = inventory.insert(itemType, count, subTransaction);
                    if (inserted > 0 && inserted >= min) {
                        //If we are able to send any items, and the amount we are sending is at least what our minimum required amount is
                        // commit the transaction and return a response of how much of the item was actually inserted
                        subTransaction.commit();
                        return createResponse(itemType, inserted, data);
                    }
                }
            }
        }
        return TransitResponse.EMPTY;
    }

    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @NotNull
    public TransitResponse createResponse(ItemResource itemType, int inserted, ItemData data) {
        if (itemType.isEmpty() || inserted <= 0) {
            return TransitResponse.EMPTY;
        }
        return new TransitResponse(itemType, inserted, data);
    }

    @NotNull
    public TransitResponse getEmptyResponse() {
        //TODO - 26.1: Evaluate if we want to inline this
        return TransitResponse.EMPTY;
    }

    public record TransitResponse(ItemResource itemType, int sendingAmount, ItemData slotData) {

        public static TransitResponse EMPTY = new TransitResponse(ItemResource.EMPTY, 0, null);

        public boolean isEmpty() {
            return itemType.isEmpty() || sendingAmount <= 0 || slotData.getTotalCount() == 0;
        }

        public int getRejected() {
            if (isEmpty()) {
                return 0;
            }
            return slotData.getTotalCount() - sendingAmount;
        }

        public int use(int amount, @Nullable TransactionContext transaction) {
            return isEmpty() ? 0 : slotData.use(amount, transaction);
        }

        public boolean useAll(@Nullable TransactionContext transaction) {
            return !isEmpty() && use(sendingAmount, transaction) == sendingAmount;
        }
    }

    public static class ItemData extends SnapshotJournal<Integer> {

        private final ItemResource itemType;
        protected int totalCount;

        public ItemData(ItemResource itemType) {
            this.itemType = itemType;
        }

        public ItemResource getItemType() {
            return itemType;
        }

        public int getTotalCount() {
            return totalCount;
        }

        /// @return Amount actually used
        public int use(int amount, @Nullable TransactionContext transaction) {
            Mekanism.logger.error("Can't 'use' with this type of TransitResponse: {}", getClass().getName());
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ItemData itemData = (ItemData) o;
            return getTotalCount() == itemData.getTotalCount() && getItemType().equals(itemData.getItemType());
        }

        @Override
        public int hashCode() {
            return 31 * getItemType().hashCode() + getTotalCount();
        }

        @Override
        protected Integer createSnapshot() {
            return totalCount;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            totalCount = snapshot;
        }
    }

    public static class SimpleTransitRequest extends TransitRequest {

        private final List<ItemData> slotData;

        protected SimpleTransitRequest(ItemResource itemType, int amount) {
            //TODO - 26.1: Re-evaluate this, but I think this makes sense and prevents transit requests from having any empty data in them
            if (itemType.isEmpty() || amount <= 0) {
                slotData = Collections.emptyList();
            } else {
                slotData = Collections.singletonList(new SimpleItemData(itemType, amount));
            }
        }

        @NonNull
        @Override
        public Iterator<ItemData> iterator() {
            return slotData.iterator();
        }

        @Override
        public boolean isEmpty() {
            return slotData.isEmpty();
        }

        public static class SimpleItemData extends ItemData {

            public SimpleItemData(ItemResource itemType, int amount) {
                super(itemType);
                totalCount = amount;
            }
        }
    }
}
