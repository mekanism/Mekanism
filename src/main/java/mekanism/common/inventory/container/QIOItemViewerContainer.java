package mekanism.common.inventory.container;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.SearchQueryParser;
import mekanism.common.content.qio.SearchQueryParser.ISearchQuery;
import mekanism.common.content.transporter.HashedItem;
import mekanism.common.inventory.GuiComponents.IDropdownEnum;
import mekanism.common.inventory.GuiComponents.IToggleEnum;
import mekanism.common.inventory.ISlotClickHandler;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.network.PacketGuiItemDataRequest;
import mekanism.common.network.PacketQIOItemViewerSlotInteract;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class QIOItemViewerContainer extends MekanismContainer implements ISlotClickHandler {

    public static final int SLOTS_X_MIN = 8, SLOTS_X_MAX = 16, SLOTS_Y_MIN = 2, SLOTS_Y_MAX = 16;

    public static final int SLOTS_START_Y = 43;
    private static final int DOUBLE_CLICK_TRANSFER_DURATION = 20;

    private ListSortType sortType = MekanismConfig.client.qioItemViewerSortType.get();
    private SortDirection sortDirection = MekanismConfig.client.qioItemViewerSortDirection.get();

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
        return SLOTS_START_Y + MekanismConfig.client.qioItemViewerSlotsY.get() * 18 + 15;
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
            if (slot.getHasStack() && slot.canTakeStack(player) && InventoryUtils.areItemsStackable(lastStack, slot.getStack())) {
                updateSlot(player, slot, freq.addItem(slot.getStack()));
            }
        });
        hotBarSlots.forEach(slot -> {
            if (slot.getHasStack() && slot.canTakeStack(player) && InventoryUtils.areItemsStackable(lastStack, slot.getStack())) {
                updateSlot(player, slot, freq.addItem(slot.getStack()));
            }
        });
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot == null) {
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

                    return updateSlot(player, currentSlot, ret);
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
        sortType.sort(itemList, sortDirection);
    }

    public void setSortDirection(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
        MekanismConfig.client.qioItemViewerSortDirection.set(sortDirection);
        MekanismConfig.client.getConfigSpec().save();
        sortItemList();
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public void setSortType(ListSortType sortType) {
        this.sortType = sortType;
        MekanismConfig.client.qioItemViewerSortType.set(sortType);
        MekanismConfig.client.getConfigSpec().save();
        sortItemList();
    }

    public ListSortType getSortType() {
        return sortType;
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
        return itemList != null ? itemList.size() : 0;
    }

    public ItemStack insertIntoPlayerInventory(ItemStack stack) {
        stack = insertItem(hotBarSlots, stack, true);
        stack = insertItem(mainInventorySlots, stack, true);
        stack = insertItem(hotBarSlots, stack, false);
        stack = insertItem(mainInventorySlots, stack, false);
        return stack;
    }

    public void updateSearch(String queryText) {
        List<IScrollableSlot> list = searchCache.get(queryText);
        if (list != null) {
            searchList = list;
            searchQuery = queryText;
            return;
        }
        list = new ArrayList<>();
        ISearchQuery query = SearchQueryParser.parse(queryText);
        for (IScrollableSlot slot : itemList) {
            if (query.matches(slot.getItem().getStack())) {
                list.add(slot);
            }
        }
        searchList = list;
        searchQuery = queryText;
        searchCache.put(queryText, searchList);
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
        public String getModID() {
            return getItem().getStack().getItem().getRegistryName().getNamespace();
        }

        @Override
        public String getDisplayName() {
            return getItem().getStack().getDisplayName().getString();
        }
    }

    public enum SortDirection implements IToggleEnum {
        ASCENDING(MekanismUtils.getResource(ResourceType.GUI, "arrow_up.png"), MekanismLang.LIST_SORT_ASCENDING_DESC),
        DESCENDING(MekanismUtils.getResource(ResourceType.GUI, "arrow_down.png"), MekanismLang.LIST_SORT_DESCENDING_DESC);

        private ResourceLocation icon;
        private ILangEntry tooltip;

        private SortDirection(ResourceLocation icon, ILangEntry tooltip) {
            this.icon = icon;
            this.tooltip = tooltip;
        }

        @Override
        public ResourceLocation getIcon() {
            return icon;
        }

        @Override
        public ITextComponent getTooltip() {
            return tooltip.translate();
        }

        public boolean isAscending() {
            return this == ASCENDING;
        }
    }

    public enum ListSortType implements IDropdownEnum {
        NAME(MekanismLang.LIST_SORT_NAME, MekanismLang.LIST_SORT_NAME_DESC, (a, b) -> a.getDisplayName().compareTo(b.getDisplayName())),
        SIZE(MekanismLang.LIST_SORT_COUNT, MekanismLang.LIST_SORT_COUNT_DESC, (a, b) -> Long.compare(a.getCount(), b.getCount())),
        MOD(MekanismLang.LIST_SORT_MOD, MekanismLang.LIST_SORT_MOD_DESC, (a, b) -> a.getModID().compareTo(b.getModID()));

        private ILangEntry name;
        private ILangEntry tooltip;
        private Comparator<IScrollableSlot> comparator;

        private ListSortType(ILangEntry name, ILangEntry tooltip, Comparator<IScrollableSlot> comparator) {
            this.name = name;
            this.tooltip = tooltip;
            this.comparator = comparator;
        }

        public void sort(List<IScrollableSlot> list, SortDirection direction) {
            list.sort(direction.isAscending() ? comparator : comparator.reversed());
        }

        @Override
        public ITextComponent getTooltip() {
            return tooltip.translate();
        }

        @Override
        public ITextComponent getShortName() {
            return name.translate();
        }
    }
}
