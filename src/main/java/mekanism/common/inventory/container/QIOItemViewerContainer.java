package mekanism.common.inventory.container;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.math.MathUtils;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOCraftingTransferHelper;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.SearchQueryParser;
import mekanism.common.content.qio.SearchQueryParser.ISearchQuery;
import mekanism.common.inventory.GuiComponents.IDropdownEnum;
import mekanism.common.inventory.GuiComponents.IToggleEnum;
import mekanism.common.inventory.ISlotClickHandler;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.VirtualCraftingOutputSlot;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import mekanism.common.inventory.slot.CraftingWindowInventorySlot;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.inventory.HashedItem.UUIDAwareHashedItem;
import mekanism.common.network.to_server.PacketGuiItemDataRequest;
import mekanism.common.network.to_server.PacketQIOItemViewerSlotInteract;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class QIOItemViewerContainer extends MekanismContainer implements ISlotClickHandler {

    public static final int SLOTS_X_MIN = 8, SLOTS_X_MAX = 16, SLOTS_Y_MIN = 2, SLOTS_Y_MAX = 48;
    public static final int SLOTS_START_Y = 43;
    private static final int DOUBLE_CLICK_TRANSFER_DURATION = 20;

    public static int getSlotsYMax() {
        int maxY = (int) Math.ceil(Minecraft.getInstance().getWindow().getGuiScaledHeight() * 0.05 - 8) + 1;
        return Math.max(Math.min(maxY, SLOTS_Y_MAX), SLOTS_Y_MIN);
    }

    private ListSortType sortType = MekanismConfig.client.qioItemViewerSortType.get();
    private SortDirection sortDirection = MekanismConfig.client.qioItemViewerSortDirection.get();

    private Object2LongMap<UUIDAwareHashedItem> cachedInventory = new Object2LongOpenHashMap<>();
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
    private List<InventoryContainerSlot>[] craftingGridInputSlots;
    protected final IQIOCraftingWindowHolder craftingWindowHolder;
    private final VirtualInventoryContainerSlot[][] craftingSlots = new VirtualInventoryContainerSlot[IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS][10];

    protected QIOItemViewerContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, boolean remote, IQIOCraftingWindowHolder craftingWindowHolder) {
        super(type, id, inv);
        this.craftingWindowHolder = craftingWindowHolder;
        if (craftingWindowHolder == null) {
            //Should never happen, but in case there was an error getting the tile it may have
            Mekanism.logger.error("Error getting crafting window holder, closing.");
            closeInventory(inv.player);
            return;
        }
        if (remote) {
            //Validate the max size when we are on the client, and fix it if it is incorrect
            int maxY = getSlotsYMax();
            if (MekanismConfig.client.qioItemViewerSlotsY.get() > maxY) {
                MekanismConfig.client.qioItemViewerSlotsY.set(maxY);
                // save the updated config info
                MekanismConfig.client.save();
            }
        }
        if (!remote) {
            craftingGridInputSlots = new List[IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS];
        }
    }

    @Nullable
    public QIOFrequency getFrequency() {
        return craftingWindowHolder.getFrequency();
    }

    /**
     * @apiNote Only used on the client
     */
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
        container.selectedWindow = getSelectedWindow();
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
    protected void addSlots() {
        super.addSlots();
        for (QIOCraftingWindow craftingWindow : craftingWindowHolder.getCraftingWindows()) {
            byte tableIndex = craftingWindow.getWindowIndex();
            for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
                addCraftingSlot(craftingWindow.getInputSlot(slotIndex), tableIndex, slotIndex);
            }
            addCraftingSlot(craftingWindow.getOutputSlot(), tableIndex, 9);
        }
    }

    private void addCraftingSlot(CraftingWindowInventorySlot slot, byte tableIndex, int slotIndex) {
        VirtualInventoryContainerSlot containerSlot = slot.createContainerSlot();
        craftingSlots[tableIndex][slotIndex] = containerSlot;
        addSlot(containerSlot);
    }

    public VirtualInventoryContainerSlot getCraftingWindowSlot(byte tableIndex, int slotIndex) {
        return craftingSlots[tableIndex][slotIndex];
    }

    @Override
    protected void openInventory(@Nonnull Inventory inv) {
        super.openInventory(inv);
        if (isRemote()) {
            Mekanism.packetHandler().sendToServer(PacketGuiItemDataRequest.qioItemViewer());
        }
    }

    @Override
    protected void closeInventory(@Nonnull Player player) {
        super.closeInventory(player);
        if (!player.level.isClientSide()) {
            QIOFrequency freq = getFrequency();
            if (freq != null) {
                freq.closeItemViewer((ServerPlayer) player);
            }
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
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

    private void doDoubleClickTransfer(Player player) {
        QIOFrequency freq = getFrequency();
        mainInventorySlots.forEach(slot -> {
            if (freq != null && slot.hasItem() && slot.mayPickup(player) && InventoryUtils.areItemsStackable(lastStack, slot.getItem())) {
                updateSlot(player, slot, freq.addItem(slot.getItem()));
            }
        });
        hotBarSlots.forEach(slot -> {
            if (freq != null && slot.hasItem() && slot.mayPickup(player) && InventoryUtils.areItemsStackable(lastStack, slot.getItem())) {
                updateSlot(player, slot, freq.addItem(slot.getItem()));
            }
        });
    }

    /**
     * Used to lazy initialize the various lists of slots for specific crafting grids
     *
     * @apiNote Only call on server
     */
    private List<InventoryContainerSlot> getCraftingGridSlots(byte selectedCraftingGrid) {
        List<InventoryContainerSlot> craftingGridSlots = craftingGridInputSlots[selectedCraftingGrid];
        if (craftingGridSlots == null) {
            //If we haven't precalculated which slots go with this crafting grid yet, do so
            craftingGridSlots = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                craftingGridSlots.add(getCraftingWindowSlot(selectedCraftingGrid, i));
            }
            craftingGridInputSlots[selectedCraftingGrid] = craftingGridSlots;
        }
        return craftingGridSlots;
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int slotID) {
        Slot currentSlot = slots.get(slotID);
        if (currentSlot == null) {
            return ItemStack.EMPTY;
        }
        if (currentSlot instanceof VirtualCraftingOutputSlot virtualSlot) {
            //If we are clicking an output crafting slot, allow the slot itself to handle the transferring
            return virtualSlot.shiftClickSlot(player, hotBarSlots, mainInventorySlots);
        } else if (currentSlot instanceof InventoryContainerSlot) {
            //Otherwise, if we are an inventory container slot (crafting input slots in this case)
            // use our normal handling to attempt and transfer the contents to the player's inventory
            return super.quickMoveStack(player, slotID);
        }
        // special handling for shift-clicking into GUI
        if (!player.level.isClientSide()) {
            ItemStack slotStack = currentSlot.getItem();
            byte selectedCraftingGrid = getSelectedCraftingGrid(player.getUUID());
            if (selectedCraftingGrid != -1) {
                //If the player has a crafting window open
                QIOCraftingWindow craftingWindow = getCraftingWindow(selectedCraftingGrid);
                if (!craftingWindow.isOutput(slotStack)) {
                    // and the stack we are trying to transfer was not the output from the crafting window
                    // as then shift clicking should be sending it into the QIO, then try transferring it
                    // into the crafting window before transferring into the frequency
                    ItemStack stackToInsert = slotStack;
                    List<InventoryContainerSlot> craftingGridSlots = getCraftingGridSlots(selectedCraftingGrid);
                    SelectedWindowData windowData = craftingWindow.getWindowData();
                    //Start by trying to stack it with other things and if that fails try to insert it into empty slots
                    stackToInsert = insertItem(craftingGridSlots, stackToInsert, windowData);
                    if (stackToInsert.getCount() != slotStack.getCount()) {
                        //If something changed, decrease the stack by the amount we inserted,
                        // and return it as a new stack for what is now in the slot
                        return transferSuccess(currentSlot, player, slotStack, stackToInsert);
                    }
                    //Otherwise, if nothing changed, try to transfer into the QIO Frequency
                }
            }
            QIOFrequency frequency = getFrequency();
            if (frequency != null) {
                if (currentSlot.hasItem()) {
                    //Make sure that we copy it so that we aren't just pointing to the reference of it
                    slotStack = slotStack.copy();
                    ItemStack ret = frequency.addItem(slotStack);
                    if (slotStack.getCount() == ret.getCount()) {
                        return ItemStack.EMPTY;
                    }
                    setTransferTracker(slotStack, slotID);
                    return updateSlot(player, currentSlot, ret);
                } else {
                    if (slotID == lastSlot && !lastStack.isEmpty()) {
                        doDoubleClickTransfer(player);
                    }
                    resetTransferTracker();
                    return ItemStack.EMPTY;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack updateSlot(Player player, Slot currentSlot, ItemStack ret) {
        return transferSuccess(currentSlot, player, currentSlot.getItem(), ret);
    }

    public void handleBatchUpdate(Object2LongMap<UUIDAwareHashedItem> itemMap, long countCapacity, int typeCapacity) {
        cachedInventory = itemMap;
        cachedCountCapacity = countCapacity;
        cachedTypeCapacity = typeCapacity;
        syncItemList();
    }

    public void handleUpdate(Object2LongMap<UUIDAwareHashedItem> itemMap, long countCapacity, int typeCapacity) {
        itemMap.object2LongEntrySet().forEach(entry -> {
            long value = entry.getLongValue();
            if (value == 0) {
                cachedInventory.removeLong(entry.getKey());
            } else {
                cachedInventory.put(entry.getKey(), value);
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

    public QIOCraftingTransferHelper getTransferHelper(Player player, QIOCraftingWindow craftingWindow) {
        return new QIOCraftingTransferHelper(cachedInventory, hotBarSlots, mainInventorySlots, craftingWindow, player);
    }

    private void syncItemList() {
        if (itemList == null) {
            itemList = new ArrayList<>();
        }
        itemList.clear();
        searchCache.clear();
        totalItems = 0;
        cachedInventory.forEach((key, value) -> {
            itemList.add(new ItemSlotData(key, key.getUUID(), value));
            totalItems += value;
        });
        sortItemList();
        if (!searchQuery.isEmpty()) {
            updateSearch(searchQuery);
        }
    }

    private void sortItemList() {
        if (itemList != null) {
            sortType.sort(itemList, sortDirection);
        }
    }

    public void setSortDirection(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
        MekanismConfig.client.qioItemViewerSortDirection.set(sortDirection);
        MekanismConfig.client.save();
        sortItemList();
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public void setSortType(ListSortType sortType) {
        this.sortType = sortType;
        MekanismConfig.client.qioItemViewerSortType.set(sortType);
        MekanismConfig.client.save();
        sortItemList();
    }

    public ListSortType getSortType() {
        return sortType;
    }

    public List<IScrollableSlot> getQIOItemList() {
        return searchQuery.isEmpty() ? itemList : searchList;
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
        return itemList == null ? 0 : itemList.size();
    }

    public byte getSelectedCraftingGrid() {
        return getSelectedCraftingGrid(getSelectedWindow());
    }

    /**
     * @apiNote Only call on server
     */
    public byte getSelectedCraftingGrid(UUID player) {
        return getSelectedCraftingGrid(getSelectedWindow(player));
    }

    private byte getSelectedCraftingGrid(@Nullable SelectedWindowData selectedWindow) {
        if (selectedWindow != null && selectedWindow.type == WindowType.CRAFTING) {
            return selectedWindow.extraData;
        }
        return (byte) -1;
    }

    public QIOCraftingWindow getCraftingWindow(int selectedCraftingGrid) {
        if (selectedCraftingGrid < 0 || selectedCraftingGrid >= IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS) {
            throw new IllegalArgumentException("Selected crafting grid not in range.");
        }
        return craftingWindowHolder.getCraftingWindows()[selectedCraftingGrid];
    }

    /**
     * @apiNote Only call on server
     */
    public ItemStack insertIntoPlayerInventory(UUID player, ItemStack stack) {
        SelectedWindowData selectedWindow = getSelectedWindow(player);
        stack = insertItem(hotBarSlots, stack, true, selectedWindow);
        stack = insertItem(mainInventorySlots, stack, true, selectedWindow);
        stack = insertItem(hotBarSlots, stack, false, selectedWindow);
        stack = insertItem(mainInventorySlots, stack, false, selectedWindow);
        return stack;
    }

    public void updateSearch(String queryText) {
        // searches should only be updated on the client-side
        if (!isRemote() || itemList == null) {
            return;
        }

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
            if (slot != null) {
                Mekanism.packetHandler().sendToServer(PacketQIOItemViewerSlotInteract.shiftTake(slot.getItemUUID()));
            }
            return;
        }
        if (button == 0) {
            if (heldItem.isEmpty() && slot != null) {
                int toTake = Math.min(slot.getItem().getStack().getMaxStackSize(), MathUtils.clampToInt(slot.getCount()));
                Mekanism.packetHandler().sendToServer(PacketQIOItemViewerSlotInteract.take(slot.getItemUUID(), toTake));
            } else if (!heldItem.isEmpty()) {
                Mekanism.packetHandler().sendToServer(PacketQIOItemViewerSlotInteract.put(heldItem.getCount()));
            }
        } else if (button == 1) {
            if (heldItem.isEmpty() && slot != null) {
                //Cap it out at the max stack size of the item, but try to take half of what is stored (taking at least one if it is a single item)
                int toTake = Math.min(slot.getItem().getStack().getMaxStackSize(), Math.max(1, MathUtils.clampToInt(slot.getCount() / 2)));
                Mekanism.packetHandler().sendToServer(PacketQIOItemViewerSlotInteract.take(slot.getItemUUID(), toTake));
            } else if (!heldItem.isEmpty()) {
                Mekanism.packetHandler().sendToServer(PacketQIOItemViewerSlotInteract.put(1));
            }
        }
    }

    public static class ItemSlotData implements IScrollableSlot {

        private final HashedItem itemType;
        private final UUID typeUUID;
        private final long count;

        private ItemSlotData(HashedItem itemType, UUID typeUUID, long count) {
            this.itemType = itemType;
            this.typeUUID = typeUUID;
            this.count = count;
        }

        @Override
        public HashedItem getItem() {
            return itemType;
        }

        @Override
        public UUID getItemUUID() {
            return typeUUID;
        }

        @Override
        public long getCount() {
            return count;
        }

        @Override
        public String getModID() {
            return MekanismUtils.getModId(getItem().getStack());
        }

        @Override
        public String getDisplayName() {
            return getItem().getStack().getHoverName().getString();
        }
    }

    public enum SortDirection implements IToggleEnum<SortDirection> {
        ASCENDING(MekanismUtils.getResource(ResourceType.GUI, "arrow_up.png"), MekanismLang.LIST_SORT_ASCENDING_DESC),
        DESCENDING(MekanismUtils.getResource(ResourceType.GUI, "arrow_down.png"), MekanismLang.LIST_SORT_DESCENDING_DESC);

        private final ResourceLocation icon;
        private final ILangEntry tooltip;

        SortDirection(ResourceLocation icon, ILangEntry tooltip) {
            this.icon = icon;
            this.tooltip = tooltip;
        }

        @Override
        public ResourceLocation getIcon() {
            return icon;
        }

        @Override
        public Component getTooltip() {
            return tooltip.translate();
        }

        public boolean isAscending() {
            return this == ASCENDING;
        }
    }

    public enum ListSortType implements IDropdownEnum<ListSortType> {
        NAME(MekanismLang.LIST_SORT_NAME, MekanismLang.LIST_SORT_NAME_DESC, Comparator.comparing(IScrollableSlot::getDisplayName)),
        SIZE(MekanismLang.LIST_SORT_COUNT, MekanismLang.LIST_SORT_COUNT_DESC, Comparator.comparingLong(IScrollableSlot::getCount).thenComparing(IScrollableSlot::getDisplayName),
              Comparator.comparingLong(IScrollableSlot::getCount).reversed().thenComparing(IScrollableSlot::getDisplayName)),
        MOD(MekanismLang.LIST_SORT_MOD, MekanismLang.LIST_SORT_MOD_DESC, Comparator.comparing(IScrollableSlot::getModID).thenComparing(IScrollableSlot::getDisplayName),
              Comparator.comparing(IScrollableSlot::getModID).reversed().thenComparing(IScrollableSlot::getDisplayName));

        private final ILangEntry name;
        private final ILangEntry tooltip;
        private final Comparator<IScrollableSlot> ascendingComparator;
        private final Comparator<IScrollableSlot> descendingComparator;

        ListSortType(ILangEntry name, ILangEntry tooltip, Comparator<IScrollableSlot> ascendingComparator) {
            this(name, tooltip, ascendingComparator, ascendingComparator.reversed());
        }

        ListSortType(ILangEntry name, ILangEntry tooltip, Comparator<IScrollableSlot> ascendingComparator, Comparator<IScrollableSlot> descendingComparator) {
            this.name = name;
            this.tooltip = tooltip;
            this.ascendingComparator = ascendingComparator;
            this.descendingComparator = descendingComparator;
        }

        public void sort(List<IScrollableSlot> list, SortDirection direction) {
            list.sort(direction.isAscending() ? ascendingComparator : descendingComparator);
        }

        @Override
        public Component getTooltip() {
            return tooltip.translate();
        }

        @Override
        public Component getShortName() {
            return name.translate();
        }
    }
}
