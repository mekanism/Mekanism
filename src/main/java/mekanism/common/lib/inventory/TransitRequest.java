package mekanism.common.lib.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public abstract class TransitRequest {

    private final TransitResponse EMPTY = new TransitResponse(ItemStack.EMPTY, null);

    public static SimpleTransitRequest simple(ItemStack stack) {
        return new SimpleTransitRequest(stack);
    }

    public static TransitRequest anyItem(TileEntity tile, Direction side, int amount) {
        return definedItem(tile, side, amount, Finder.ANY);
    }

    public static TransitRequest definedItem(TileEntity tile, Direction side, int amount, Finder finder) {
        return definedItem(tile, side, 1, amount, finder);
    }

    public static TransitRequest definedItem(TileEntity tile, Direction side, int min, int max, Finder finder) {
        TileTransitRequest ret = new TileTransitRequest(tile, side);
        IItemHandler inventory = InventoryUtils.assertItemHandler("TransitRequest", tile, side);
        if (inventory == null) {
            return ret;
        }
        // count backwards- we start from the bottom of the inventory and go back for consistency
        for (int i = inventory.getSlots() - 1; i >= 0; i--) {
            ItemStack stack = inventory.extractItem(i, max, true);

            if (!stack.isEmpty() && finder.modifies(stack)) {
                HashedItem hashed = new HashedItem(stack);
                int toUse = Math.min(stack.getCount(), max - ret.getCount(hashed));
                if (toUse == 0) {
                    continue; // continue if we don't need anymore of this item type
                }
                ret.addItem(StackUtils.size(stack, toUse), i);
            }
        }
        // remove items that we don't have enough of
        ret.getItemMap().entrySet().removeIf(entry -> entry.getValue().getTotalCount() < min);
        return ret;
    }

    public abstract Collection<? extends ItemData> getItemData();

    @Nonnull
    public TransitResponse addToInventory(TileEntity tile, Direction side, boolean force) {
        if (force && tile instanceof TileEntityLogisticalSorter) {
            return ((TileEntityLogisticalSorter) tile).sendHome(this);
        }
        if (isEmpty()) {
            return getEmptyResponse();
        }
        Optional<IItemHandler> capability = CapabilityUtils.getCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite()).resolve();
        if (capability.isPresent()) {
            IItemHandler inventory = capability.get();
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

    @Nonnull
    public TransitResponse createResponse(ItemStack inserted, ItemData data) {
        return new TransitResponse(inserted, data);
    }

    public TransitResponse createSimpleResponse() {
        ItemData data = getItemData().stream().findFirst().orElse(null);
        return data == null ? getEmptyResponse() : createResponse(data.itemType.createStack(data.totalCount), data);
    }

    @Nonnull
    public TransitResponse getEmptyResponse() {
        return EMPTY;
    }

    public static class TransitResponse {

        private final ItemStack inserted;
        private final ItemData slotData;

        public TransitResponse(@Nonnull ItemStack inserted, ItemData slotData) {
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
                super(new HashedItem(stack));
                totalCount = stack.getCount();
            }
        }
    }
}
