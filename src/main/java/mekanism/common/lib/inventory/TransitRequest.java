package mekanism.common.lib.inventory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.item.CursedTransporterItemHandler;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
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

    private final TransitResponse EMPTY = new TransitResponse(ItemResource.EMPTY, 0, null);

    public static SimpleTransitRequest simple(ItemResource itemType, int amount) {
        return new SimpleTransitRequest(itemType, amount);
    }

    public static SimpleTransitRequest simple(TransporterStack stack) {
        return simple(stack.getItemType(), stack.size());
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
        if (inventory == null || inventory.size() == 0) {
            //If the inventory has no slots just exit early with the result that we can't send any items
            return getEmptyResponse();
        }
        for (ItemData data : this) {
            try (Transaction transaction = Transaction.openRoot()) {//TODO - 26.1: Check callers and see if any are already in a transaction context
                ItemResource itemType = data.getItemType();
                int inserted = inventory.insert(itemType, data.getTotalCount(), transaction);
                if (inserted > 0 && inserted >= min) {
                    //If we are able to send any items, and the amount we are sending is at least what our minimum required amount is
                    // commit the transaction and return a response of how much of the item was actually inserted
                    transaction.commit();
                    return createResponse(itemType, inserted, data);
                }
            }
        }
        return getEmptyResponse();
    }

    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @NotNull
    public TransitResponse createResponse(ItemResource itemType, int inserted, ItemData data) {
        if (itemType.isEmpty() || inserted <= 0) {
            return getEmptyResponse();
        }
        return new TransitResponse(itemType, inserted, data);
    }

    @NotNull
    public TransitResponse getEmptyResponse() {
        return EMPTY;
    }

    public record TransitResponse(ItemResource itemType, int sendingAmount, ItemData slotData) {

        public boolean isEmpty() {
            return itemType.isEmpty() || sendingAmount <= 0 || slotData.getTotalCount() == 0;
        }

        public int getRejected() {
            if (isEmpty()) {
                return 0;
            }
            return slotData.getTotalCount() - sendingAmount();
        }

        public ItemStack use(int amount) {
            return slotData.use(amount);
        }

        public ItemStack useAll() {
            return use(sendingAmount());
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

        protected SimpleTransitRequest(ItemResource itemType, int amount) {
            //TODO - 26.1: Re-evaluate this, but I think this makes sense and prevents transit requests from having any empty data in them
            if (itemType.isEmpty() || amount <= 0) {
                slotData = Collections.emptyList();
            } else {
                slotData = Collections.singletonList(new SimpleItemData(itemType, amount));
            }
        }

        @Override
        public List<ItemData> getItemData() {
            return slotData;
        }

        public static class SimpleItemData extends ItemData {

            public SimpleItemData(ItemResource itemType, int amount) {
                super(itemType);
                totalCount = amount;
            }
        }
    }
}
