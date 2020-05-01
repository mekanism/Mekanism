package mekanism.common.inventory.container;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.transporter.HashedItem;
import mekanism.common.inventory.ISlotClickHandler;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.network.PacketGuiItemDataRequest;
import mekanism.common.network.PacketQIOItemViewerSlotInteract;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public abstract class QIOItemViewerContainer extends MekanismContainer implements ISlotClickHandler {

    public static final int SLOTS_X_MIN = 8, SLOTS_X_MAX = 16, SLOTS_Y_MIN = 2, SLOTS_Y_MAX = 16;

    public static final int SLOTS_START_Y = 40;
    private static final int DOUBLE_CLICK_TRANSFER_DURATION = 20;

    private ListSortType sortType = ListSortType.NAME_ASCENDING;

    private Map<HashedItem, Long> cachedInventory = new Object2ObjectOpenHashMap<>();
    private long cachedCountCapacity;
    private int cachedTypeCapacity;
    private long totalItems;

    private List<IScrollableSlot> itemList;
    private List<IScrollableSlot> searchList;

    private Map<String, List<IScrollableSlot>> searchCache = new Object2ObjectOpenHashMap<>();

    private String searchQuery = "";

    private int doubleClickTransferTicks = 0;
    private int lastSlot = -1;
    private ItemStack lastStack = ItemStack.EMPTY;

    protected QIOItemViewerContainer(ContainerTypeRegistryObject<?> type, int id, PlayerInventory inv) {
        super(type, id, inv);
    }

    public abstract QIOFrequency getFrequency();

    public abstract QIOItemViewerContainer recreate();

    protected void sync(QIOItemViewerContainer container) {
        container.sortType = sortType;
        container.cachedInventory = cachedInventory;
        container.cachedCountCapacity = cachedCountCapacity;
        container.cachedTypeCapacity = cachedTypeCapacity;
        container.totalItems = totalItems;
        container.itemList = itemList;
        container.searchList = searchList;
        container.searchCache = searchCache;
        container.searchQuery = searchQuery;
    }

    @Override
    protected int getInventoryYOffset() {
        return SLOTS_START_Y + MekanismConfig.client.qioItemViewerSlotsY.get() * 18 + 12;
    }

    @Override
    protected int getInventoryXOffset() {
        return super.getInventoryXOffset() + (MekanismConfig.client.qioItemViewerSlotsX.get() - 8) * 18 / 2;
    }

    @Override
    protected void openInventory(@Nonnull PlayerInventory inv) {
        super.openInventory(inv);
        if (inv.player.world.isRemote()) {
            Mekanism.packetHandler.sendToServer(PacketGuiItemDataRequest.qioItemViewer());
        }
    }

    @Override
    protected void closeInventory(PlayerEntity player) {
        super.closeInventory(player);
        QIOFrequency freq = getFrequency();
        if (!inv.player.world.isRemote() && freq != null) {
            freq.closeItemViewer((ServerPlayerEntity) inv.player);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (doubleClickTransferTicks > 0) {
            doubleClickTransferTicks--;
        } else {
            resetTransferTracker();
        }
    }

    private void resetTransferTracker() {
        doubleClickTransferTicks = 0;
        lastSlot = -1;
        lastStack = ItemStack.EMPTY;
    }

    private void setTransferTracker(ItemStack stack, int slot) {
        doubleClickTransferTicks = DOUBLE_CLICK_TRANSFER_DURATION;
        lastSlot = slot;
        lastStack = stack;
    }

    private void doDoubleClickTransfer(PlayerEntity player) {
        QIOFrequency freq = getFrequency();
        mainInventorySlots.forEach(slot -> {
            if (slot.canTakeStack(player) && InventoryUtils.areItemsStackable(lastStack, slot.getStack())) {
                updateSlot(player, slot, freq.addItem(slot.getStack()));
            }
        });
        hotBarSlots.forEach(slot -> {
            if (slot.canTakeStack(player) && InventoryUtils.areItemsStackable(lastStack, slot.getStack())) {
                updateSlot(player, slot, freq.addItem(slot.getStack()));
            }
        });
        ((ServerPlayerEntity) player).sendContainerToPlayer(this);
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot == null || !currentSlot.getHasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack slotStack = currentSlot.getStack().copy();
        // special handling for shift-clicking into GUI
        if (!(currentSlot instanceof InventoryContainerSlot)) {
            if (!player.world.isRemote() && getFrequency() != null) {
                if (!slotStack.isEmpty()) {
                    ItemStack ret = getFrequency().addItem(slotStack);
                    if (slotStack.getCount() != ret.getCount()) {
                        setTransferTracker(slotStack, slotID);
                    } else {
                        return ItemStack.EMPTY;
                    }

                    ItemStack newStack = updateSlot(player, currentSlot, ret);
                    // update the client
                    ((ServerPlayerEntity) player).sendContainerToPlayer(this);
                    return newStack;
                } else {
                    if (slotID == lastSlot && !lastStack.isEmpty()) {
                        doDoubleClickTransfer(player);
                    }
                    resetTransferTracker();
                    return ItemStack.EMPTY;
                }
            }
            return ItemStack.EMPTY;
        }
        return super.transferStackInSlot(player, slotID);
    }

    private ItemStack updateSlot(PlayerEntity player, Slot currentSlot, ItemStack ret) {
        int difference = currentSlot.getStack().getCount() - ret.getCount();
        currentSlot.decrStackSize(difference);
        ItemStack newStack = StackUtils.size(currentSlot.getStack(), difference);
        currentSlot.onTake(player, newStack);
        return newStack;
    }

    public void handleBatchUpdate(Map<HashedItem, Long> itemMap, long countCapacity, int typeCapacity) {
        cachedInventory = itemMap;
        cachedCountCapacity = countCapacity;
        cachedTypeCapacity = typeCapacity;
        syncItemList();
    }

    public void handleUpdate(Map<HashedItem, Long> itemMap, long countCapacity, int typeCapacity) {
        itemMap.entrySet().forEach(e -> {
            if (e.getValue() == 0) {
                cachedInventory.remove(e.getKey());
            } else {
                cachedInventory.put(e.getKey(), e.getValue());
            }
        });
        cachedCountCapacity = countCapacity;
        cachedTypeCapacity = typeCapacity;
        syncItemList();
    }

    public void handleKill() {
        itemList = null;
        searchList = null;
        cachedInventory.clear();
    }

    private void syncItemList() {
        if (itemList == null)
            itemList = new ArrayList<>();
        itemList.clear();
        searchCache.clear();
        totalItems = 0;
        cachedInventory.entrySet().forEach(e -> {
            itemList.add(new ItemSlotData(e.getKey(), e.getValue()));
            totalItems += e.getValue();
        });
        sortItemList();
        if (!searchQuery.isEmpty()) {
            updateSearch(searchQuery);
        }
    }

    private void sortItemList() {
        if (itemList == null)
            return;
        sortType.sort(itemList);
    }

    public void setSortType(ListSortType sortType) {
        this.sortType = sortType;
        sortItemList();
    }

    public void toggleSortType() {
        setSortType(sortType.toggle());
    }

    public List<IScrollableSlot> getQIOItemList() {
        return !searchQuery.isEmpty() ? searchList : itemList;
    }

    public long getCountCapacity() {
        return cachedCountCapacity;
    }

    public int getTypeCapacity() {
        return cachedTypeCapacity;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public int getTotalTypes() {
        return itemList.size();
    }

    public ItemStack insertIntoPlayerInventory(ItemStack stack) {
        stack = insertItem(hotBarSlots, stack, true);
        stack = insertItem(mainInventorySlots, stack, true);
        stack = insertItem(hotBarSlots, stack, false);
        stack = insertItem(mainInventorySlots, stack, false);
        return stack;
    }

    public void updateSearch(String query) {
        List<IScrollableSlot> list = searchCache.get(query);
        if (list != null) {
            searchList = list;
            return;
        }
        list = new ArrayList<>();
        for (IScrollableSlot slot : itemList) {
            if (slot.getDisplayName().toLowerCase().contains(query.toLowerCase())) {
                list.add(slot);
            }
        }
        searchCache.put(query, searchList);
    }

    @Override
    public void onClick(IScrollableSlot slot, int button, boolean hasShiftDown, ItemStack heldItem) {
        if (hasShiftDown) {
            if (slot != null)
                Mekanism.packetHandler.sendToServer(PacketQIOItemViewerSlotInteract.shiftTake(slot.getItem()));
            return;
        }
        if (button == 0) {
            if (heldItem.isEmpty() && slot != null) {
                Mekanism.packetHandler.sendToServer(PacketQIOItemViewerSlotInteract.take(slot.getItem(), 64));
            } else if (!heldItem.isEmpty()) {
                Mekanism.packetHandler.sendToServer(PacketQIOItemViewerSlotInteract.put(heldItem.getCount()));
            }
        } else if (button == 1) {
            if (heldItem.isEmpty() && slot != null) {
                Mekanism.packetHandler.sendToServer(PacketQIOItemViewerSlotInteract.take(slot.getItem(), (int) Math.min(32, slot.getCount() / 2)));
            } else if (!heldItem.isEmpty()) {
                Mekanism.packetHandler.sendToServer(PacketQIOItemViewerSlotInteract.put(1));
            }
        }
    }

    public static class ItemSlotData implements IScrollableSlot {

        private HashedItem itemType;
        private long count;

        private ItemSlotData(HashedItem itemType, long count) {
            this.itemType = itemType;
            this.count = count;
        }

        @Override
        public HashedItem getItem() {
            return itemType;
        }

        @Override
        public long getCount() {
            return count;
        }

        @Override
        public String getDisplayName() {
            return getItem().getStack().getDisplayName().getString();
        }
    }

    public enum ListSortType {
        NAME_ASCENDING((a, b) -> a.getDisplayName().compareTo(b.getDisplayName())),
        NAME_DESCENDING((a, b) -> b.getDisplayName().compareTo(a.getDisplayName())),
        SIZE_ASCENDING((a, b) -> Long.compare(a.getCount(), b.getCount())),
        SIZE_DESCENDING((a, b) -> Long.compare(b.getCount(), a.getCount()));

        private Comparator<IScrollableSlot> comparator;

        private ListSortType(Comparator<IScrollableSlot> comparator) {
            this.comparator = comparator;
        }

        public void sort(List<IScrollableSlot> list) {
            list.sort(comparator);
        }

        public ListSortType toggle() {
            switch (this) {
                case NAME_ASCENDING: return NAME_DESCENDING;
                case NAME_DESCENDING: return NAME_ASCENDING;
                case SIZE_ASCENDING: return SIZE_DESCENDING;
                case SIZE_DESCENDING: return SIZE_ASCENDING;
                default:
                    return NAME_ASCENDING; // java is annoying
            }
        }
    }
}
