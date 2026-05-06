package mekanism.common.lib.inventory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.item.CursedTransporterItemHandler;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.lib.inventory.TransitRequest.ItemData;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TransitRequest implements Iterable<ItemData> {

    private final TransitResponse EMPTY = new TransitResponse(ItemStack.EMPTY, null);

    public static SimpleTransitRequest simple(ItemStack stack) {
        return new SimpleTransitRequest(stack);
    }

    public static TransitRequest anyItem(ResourceHandler<ItemResource> inventory, int amount) {
        return definedItem(inventory, amount, Finder.ANY);
    }

    public static TransitRequest definedItem(ResourceHandler<ItemResource> inventory, int amount, Finder finder) {
        return definedItem(inventory, 1, amount, finder);
    }

    //TODO - 26.1: Evaluate what callers of this could just be replaced with using ResourceHandlerUtil calls
    public static TransitRequest definedItem(ResourceHandler<ItemResource> inventory, int min, int max, Finder finder) {
        HandlerTransitRequest ret = new HandlerTransitRequest(inventory);
        if (inventory == null) {
            return ret;
        }
        //TODO - 26.1: Re-evaluate callers and see if we have a transaction context from any of them that we need to use rather than opening root
        try (Transaction simulation = Transaction.openRoot()) {
            // count backwards: we start from the bottom of the inventory and go back for consistency
            for (int i = inventory.size() - 1; i >= 0; i--) {
                ItemResource itemType = inventory.getResource(i);
                if (itemType.isEmpty() || !finder.test(itemType)) {
                    continue;
                }
                //TODO - 26.1: Do we want to try and rework the handler transit requests to be able to ignore index?
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
    public TransitResponse eject(BlockEntity outputter, @Nullable ResourceHandler<ItemResource> target, int min, Function<LogisticalTransporterBase, EnumColor> outputColor) {
        return eject(outputter, outputter.getBlockPos(), target, min, outputColor);
    }

    @NotNull
    public TransitResponse eject(BlockEntity outputter, BlockPos outputterPos, @Nullable ResourceHandler<ItemResource> target, int min,
          Function<LogisticalTransporterBase, EnumColor> outputColor) {
        if (isEmpty()) {//Short circuit if our request is empty
            return getEmptyResponse();
        } else if (target instanceof CursedTransporterItemHandler cursed) {
            LogisticalTransporterBase transporter = cursed.getTransporter();
            return transporter.insert(outputter, outputterPos, this, outputColor.apply(transporter), true, min);
        }
        return addToInventoryUnchecked(target, min);
    }

    @NotNull
    public TransitResponse addToInventory(Level level, BlockPos pos, @Nullable ResourceHandler<ItemResource> inventory, int min, boolean force) {
        if (isEmpty()) {//Short circuit if our request is empty
            return getEmptyResponse();
        } else if (force && WorldUtils.getTileEntity(level, pos) instanceof IAdvancedTransportEjector sorter) {
            return sorter.sendHome(this);
        }
        return addToInventoryUnchecked(inventory, min);
    }

    //Note: We are unchecked because we don't validate if we are empty or not
    @NotNull
    public TransitResponse addToInventoryUnchecked(@Nullable ResourceHandler<ItemResource> inventory, int min) {
        if (inventory == null) {
            return getEmptyResponse();
        }
        int slots = inventory.size();
        if (slots == 0) {
            //If the inventory has no slots just exit early with the result that we can't send any items
            return getEmptyResponse();
        }
        if (min > 1) {
            //If we have a minimum amount of items we are trying to send, we need to start by simulating
            // to see if we actually have enough room to send the minimum amount of our item. We can
            // skip this step if we don't have a minimum amount being sent, as then whatever we are
            // able to insert will be "good enough"
            TransitResponse response = TransporterManager.getPredictedInsert(inventory, this);
            if (response.isEmpty() || response.getSendingAmount() < min) {
                //If we aren't able to send any items or are only able to send less than we have room for
                // return that we aren't able to insert the requested amount
                return getEmptyResponse();
            }
            // otherwise, continue on to actually sending items to the inventory
        }
        for (ItemData data : this) {
            ItemResource itemType = data.getItemType();
            int totalCount = data.getTotalCount();
            int toInsert = totalCount;
            try (Transaction transaction = Transaction.openRoot()) {//TODO - 26.1: Check callers and see if any are already in a transaction context
                for (int i = 0; i < slots; i++) {
                    // Do insert, this will handle validating the item is valid for the inventory
                    toInsert -= inventory.insert(i, itemType, toInsert, transaction);
                    if (toInsert == 0) {//If we inserted everything we wanted to break and create the response
                        break;
                    }
                }
                transaction.commit();
            }
            //TODO - 26.1: Re-evaluate if we even need to be checking if toInsert is zero here?
            // Main case that would matter where it isn't caught by the second check is if total count is zero
            // is that a possible/valid state?
            if (toInsert == 0 || toInsert < totalCount) {
                //Return a response of how much of the item was inserted
                return createResponse(itemType.toStack(totalCount - toInsert), data);
            }
        }
        return getEmptyResponse();
    }

    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @NotNull
    public TransitResponse createResponse(ItemStack inserted, ItemData data) {
        return new TransitResponse(inserted, data);
    }

    @NotNull
    public TransitResponse createSimpleResponse() {
        for (ItemData data : this) {
            return createResponse(data.getStack(), data);
        }
        return getEmptyResponse();
    }

    @NotNull
    public TransitResponse getEmptyResponse() {
        return EMPTY;
    }

    public static class TransitResponse {

        private final ItemStack inserted;
        private final ItemData slotData;

        public TransitResponse(@NotNull ItemStack inserted, ItemData slotData) {
            this.inserted = inserted;
            this.slotData = slotData;
        }

        public int getSendingAmount() {
            return inserted.count();
        }

        public ItemData getSlotData() {
            return slotData;
        }

        public ItemStack getStack() {
            return inserted;
        }

        public boolean isEmpty() {
            return inserted.isEmpty() || slotData.getTotalCount() == 0;
        }

        public ItemStack getRejected() {
            if (isEmpty()) {
                return ItemStack.EMPTY;
            }
            return slotData.getItemType().toStack(slotData.getTotalCount() - getSendingAmount());
        }

        public ItemStack use(int amount) {
            return slotData.use(amount);
        }

        public ItemStack useAll() {
            return use(getSendingAmount());
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TransitResponse other = (TransitResponse) o;
            return (inserted == other.inserted || ItemStack.matches(inserted, other.inserted)) && slotData.equals(other.slotData);
        }

        @Override
        public int hashCode() {
            int code = ItemStack.hashItemAndComponents(inserted);
            code = 31 * code + inserted.count();
            code = 31 * code + slotData.hashCode();
            return code;
        }
    }

    public static class ItemData {

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

        public ItemStack getStack() {
            return getItemType().toStack(getTotalCount());
        }

        public ItemStack use(int amount) {
            Mekanism.logger.error("Can't 'use' with this type of TransitResponse: {}", getClass().getName());
            return ItemStack.EMPTY;
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
    }

    public static class SimpleTransitRequest extends CollectionTransitRequest {

        private final List<ItemData> slotData;

        protected SimpleTransitRequest(ItemStack stack) {
            slotData = Collections.singletonList(new SimpleItemData(stack));
        }

        @Override
        public List<ItemData> getItemData() {
            return slotData;
        }

        public static class SimpleItemData extends ItemData {

            public SimpleItemData(ItemStack stack) {
                super(ItemResource.of(stack));
                totalCount = stack.count();
            }
        }
    }
}
