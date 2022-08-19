package mekanism.common.lib.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mekanism.common.Mekanism;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public abstract class TransitRequest {

    private final TransitResponse EMPTY = new TransitResponse(ItemStack.EMPTY, null);

    public static SimpleTransitRequest simple(ItemStack stack) {
        return new SimpleTransitRequest(stack);
    }

    public static TransitRequest anyItem(BlockEntity tile, Direction side, int amount) {
        return definedItem(tile, side, amount, Finder.ANY);
    }

    public static TransitRequest definedItem(BlockEntity tile, Direction side, int amount, Finder finder) {
        return definedItem(tile, side, 1, amount, finder);
    }

    public static TransitRequest definedItem(BlockEntity tile, Direction side, int min, int max, Finder finder) {
        TileTransitRequest ret = new TileTransitRequest(tile, side);
        IItemHandler inventory = InventoryUtils.assertItemHandler("TransitRequest", tile, side);
        if (inventory == null) {
            return ret;
        }
        // count backwards- we start from the bottom of the inventory and go back for consistency
        for (int i = inventory.getSlots() - 1; i >= 0; i--) {
            ItemStack stack = inventory.extractItem(i, max, true);

            if (!stack.isEmpty() && finder.modifies(stack)) {
                HashedItem hashed = HashedItem.raw(stack);
                int toUse = Math.min(stack.getCount(), max - ret.getCount(hashed));
                if (toUse == 0) {
                    continue; // continue if we don't need any more of this item type
                }
                ret.addItem(StackUtils.size(stack, toUse), i);
            }
        }
        // remove items that we don't have enough of
        ret.getItemMap().entrySet().removeIf(entry -> entry.getValue().getTotalCount() < min);
        return ret;
    }

    public abstract Collection<? extends ItemData> getItemData();

    @NotNull
    public TransitResponse addToInventory(BlockEntity tile, Direction side, int min, boolean force) {
        if (force && tile instanceof TileEntityLogisticalSorter sorter) {
            return sorter.sendHome(this);
        }
        if (isEmpty()) {
            return getEmptyResponse();
        }
        Optional<IItemHandler> capability = CapabilityUtils.getCapability(tile, ForgeCapabilities.ITEM_HANDLER, side.getOpposite()).resolve();
        if (capability.isPresent()) {
            IItemHandler inventory = capability.get();
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
            for (ItemData data : getItemData()) {
                ItemStack origInsert = StackUtils.size(data.getStack(), data.getTotalCount());
                ItemStack toInsert = origInsert.copy();
                for (int i = 0; i < inventory.getSlots(); i++) {
                    // Check validation
                    if (inventory.isItemValid(i, toInsert)) {
                        // Do insert
                        toInsert = inventory.insertItem(i, toInsert, false);
                        // If empty, end
                        if (toInsert.isEmpty()) {
                            return createResponse(origInsert, data);
                        }
                    }
                }
                if (TransporterManager.didEmit(origInsert, toInsert)) {
                    return createResponse(TransporterManager.getToUse(origInsert, toInsert), data);
                }
            }
        }
        return getEmptyResponse();
    }

    public boolean isEmpty() {
        return getItemData().isEmpty();
    }

    @NotNull
    public TransitResponse createResponse(ItemStack inserted, ItemData data) {
        return new TransitResponse(inserted, data);
    }

    public TransitResponse createSimpleResponse() {
        ItemData data = getItemData().stream().findFirst().orElse(null);
        return data == null ? getEmptyResponse() : createResponse(data.itemType.createStack(data.totalCount), data);
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
            return inserted.getCount();
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
            return StackUtils.size(slotData.getStack(), slotData.getStack().getCount() - getSendingAmount());
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
            int code = 1;
            code = 31 * code + inserted.getItem().hashCode();
            code = 31 * code + inserted.getCount();
            if (inserted.hasTag()) {
                code = 31 * code + inserted.getTag().hashCode();
            }
            code = 31 * code + slotData.hashCode();
            return code;
        }
    }

    public static class ItemData {

        private final HashedItem itemType;
        protected int totalCount;

        public ItemData(HashedItem itemType) {
            this.itemType = itemType;
        }

        public HashedItem getItemType() {
            return itemType;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public ItemStack getStack() {
            return getItemType().createStack(getTotalCount());
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
            return totalCount == itemData.totalCount && itemType.equals(itemData.itemType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(itemType, totalCount);
        }
    }

    public static class SimpleTransitRequest extends TransitRequest {

        private final List<ItemData> slotData = new ArrayList<>();

        protected SimpleTransitRequest(ItemStack stack) {
            slotData.add(new SimpleItemData(stack));
        }

        @Override
        public Collection<ItemData> getItemData() {
            return slotData;
        }

        public static class SimpleItemData extends ItemData {

            public SimpleItemData(ItemStack stack) {
                super(HashedItem.create(stack));
                totalCount = stack.getCount();
            }
        }
    }
}
