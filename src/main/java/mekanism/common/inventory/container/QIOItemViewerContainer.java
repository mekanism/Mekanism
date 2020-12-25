package mekanism.common.inventory.container;

import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.math.MathUtils;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.SearchQueryParser;
import mekanism.common.content.qio.SearchQueryParser.ISearchQuery;
import mekanism.common.inventory.GuiComponents.IDropdownEnum;
import mekanism.common.inventory.GuiComponents.IToggleEnum;
import mekanism.common.inventory.ISlotClickHandler;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import mekanism.common.inventory.slot.CraftingWindowInventorySlot;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.inventory.HashedItem.UUIDAwareHashedItem;
import mekanism.common.network.PacketGuiItemDataRequest;
import mekanism.common.network.PacketQIOCraftingWindowSelect;
import mekanism.common.network.PacketQIOItemViewerSlotInteract;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class QIOItemViewerContainer extends MekanismContainer implements ISlotClickHandler {

    public static final int SLOTS_X_MIN = 8, SLOTS_X_MAX = 16, SLOTS_Y_MIN = 2, SLOTS_Y_MAX = 48;
    public static final int SLOTS_START_Y = 43;
    private static final int DOUBLE_CLICK_TRANSFER_DURATION = 20;

    public static int getSlotsYMax() {
        int maxY = (int) Math.ceil(Minecraft.getInstance().getMainWindow().getScaledHeight() * 0.05 - 8) + 1;
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

    /**
     * Keeps track of which crafting grid the player has open. Only used on the client for use in JEI, so doesn't need to keep track of other players.
     */
    private byte selectedCraftingGrid = -1;
    private Object2ByteMap<UUID> selectedCraftingGrids;
    private List<InventoryContainerSlot>[] craftingGridInputSlots;

    private int doubleClickTransferTicks = 0;
    private int lastSlot = -1;
    private ItemStack lastStack = ItemStack.EMPTY;
    private final QIOCraftingWindow[] craftingWindows;
    private final VirtualInventoryContainerSlot[][] craftingSlots = new VirtualInventoryContainerSlot[IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS][10];

    protected QIOItemViewerContainer(ContainerTypeRegistryObject<?> type, int id, PlayerInventory inv, boolean remote, IQIOCraftingWindowHolder holder) {
        super(type, id, inv);
        if (holder == null) {
            //Should never happen, but in case there was an error getting the tile it may have
            craftingWindows = null;
            Mekanism.logger.error("Error getting crafting window holder, closing.");
            closeInventory(inv.player);
            return;
        }
        this.craftingWindows = holder.getCraftingWindows();
        if (craftingWindows.length != IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS) {
            throw new IllegalArgumentException("Invalid number of crafting windows, expected " + IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS + " received " +
                                               craftingWindows.length);
        }
        if (remote) {
            //Validate the max size when we are on the client, and fix it if it is incorrect
            int maxY = getSlotsYMax();
            if (MekanismConfig.client.qioItemViewerSlotsY.get() > maxY) {
                MekanismConfig.client.qioItemViewerSlotsY.set(maxY);
                // save the updated config info
                MekanismConfig.client.getConfigSpec().save();
            }
        }
        if (!remote) {
            //Only keep track of uuid based selected grids on the server
            selectedCraftingGrids = new Object2ByteOpenHashMap<>();
            craftingGridInputSlots = new List[IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS];
        }
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
        container.selectedCraftingGrid = selectedCraftingGrid;
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
        for (QIOCraftingWindow craftingWindow : craftingWindows) {
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
    protected void openInventory(@Nonnull PlayerInventory inv) {
        super.openInventory(inv);
        if (inv.player.world.isRemote()) {
            Mekanism.packetHandler.sendToServer(PacketGuiItemDataRequest.qioItemViewer());
        }
    }

    @Override
    protected void closeInventory(@Nonnull PlayerEntity player) {
        super.closeInventory(player);
        if (!player.world.isRemote()) {
            clearSelectedCraftingGrid(player.getUniqueID());
            QIOFrequency freq = getFrequency();
            if (freq != null) {
                freq.closeItemViewer((ServerPlayerEntity) player);
            }
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
        }
        return craftingGridSlots;
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(@Nonnull PlayerEntity player, int slotID) {
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot == null) {
            return ItemStack.EMPTY;
        }
        if (currentSlot instanceof InventoryContainerSlot) {
            return super.transferStackInSlot(player, slotID);
        }
        // special handling for shift-clicking into GUI
        if (!player.world.isRemote()) {
            ItemStack slotStack = currentSlot.getStack();
            byte selectedCraftingGrid = selectedCraftingGrids.getOrDefault(player.getUniqueID(), (byte) -1);
            if (selectedCraftingGrid != -1) {
                //If the player has a crafting window open, try transferring into it before
                // transferring into the frequency
                ItemStack stackToInsert = slotStack;
                List<InventoryContainerSlot> craftingGridSlots =getCraftingGridSlots(selectedCraftingGrid);
                //Start by trying to stack it with other things
                stackToInsert = insertItem(craftingGridSlots, stackToInsert, true);
                // and if that fails try to insert it into empty slots
                stackToInsert = insertItem(craftingGridSlots, stackToInsert, false);
                if (stackToInsert.getCount() != slotStack.getCount()) {
                    //If something changed, decrease the stack by the amount we inserted,
                    // and return it as a new stack for what is now in the slot
                    return transferSuccess(currentSlot, player, slotStack, stackToInsert);
                }
                //Otherwise if nothing changed, allow the
            }
            QIOFrequency frequency = getFrequency();
            if (frequency != null) {
                if (currentSlot.getHasStack()) {
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

    private ItemStack updateSlot(PlayerEntity player, Slot currentSlot, ItemStack ret) {
        return transferSuccess(currentSlot, player, currentSlot.getStack(), ret);
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

    public byte getSelectedCraftingGrid() {
        return selectedCraftingGrid;
    }

    /**
     * @apiNote Only call on client
     */
    public void setSelectedCraftingGrid(byte selectedCraftingGrid) {
        if (this.selectedCraftingGrid != selectedCraftingGrid) {
            this.selectedCraftingGrid = selectedCraftingGrid;
            Mekanism.packetHandler.sendToServer(new PacketQIOCraftingWindowSelect(this.selectedCraftingGrid));
        }
    }

    /**
     * @apiNote Only call on server
     */
    public void setSelectedCraftingGrid(UUID player, byte selectedCraftingGrid) {
        if (selectedCraftingGrid == -1) {
            clearSelectedCraftingGrid(player);
        } else if (selectedCraftingGrid < IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS) {
            //Validate that the packet sent a valid crafting grid
            selectedCraftingGrids.put(player, selectedCraftingGrid);
        }
    }

    /**
     * @apiNote Only call on server
     */
    private void clearSelectedCraftingGrid(UUID player) {
        selectedCraftingGrids.removeByte(player);
    }

    public ItemStack insertIntoPlayerInventory(ItemStack stack) {
        stack = insertItem(hotBarSlots, stack, true);
        stack = insertItem(mainInventorySlots, stack, true);
        stack = insertItem(hotBarSlots, stack, false);
        stack = insertItem(mainInventorySlots, stack, false);
        return stack;
    }

    public void updateSearch(String queryText) {
        // searches should only updated on client-side
        if (!inv.player.world.isRemote() || itemList == null) {
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
                Mekanism.packetHandler.sendToServer(PacketQIOItemViewerSlotInteract.shiftTake(slot.getItemUUID()));
            }
            return;
        }
        if (button == 0) {
            if (heldItem.isEmpty() && slot != null) {
                int toTake = Math.min(slot.getItem().getStack().getMaxStackSize(), MathUtils.clampToInt(slot.getCount()));
                Mekanism.packetHandler.sendToServer(PacketQIOItemViewerSlotInteract.take(slot.getItemUUID(), toTake));
            } else if (!heldItem.isEmpty()) {
                Mekanism.packetHandler.sendToServer(PacketQIOItemViewerSlotInteract.put(heldItem.getCount()));
            }
        } else if (button == 1) {
            if (heldItem.isEmpty() && slot != null) {
                //Cap it out at the max stack size of the item, but try to take half of what is stored (taking at least one if it is a single item)
                int toTake = Math.min(slot.getItem().getStack().getMaxStackSize(), Math.max(1, MathUtils.clampToInt(slot.getCount() / 2)));
                Mekanism.packetHandler.sendToServer(PacketQIOItemViewerSlotInteract.take(slot.getItemUUID(), toTake));
            } else if (!heldItem.isEmpty()) {
                Mekanism.packetHandler.sendToServer(PacketQIOItemViewerSlotInteract.put(1));
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
            return getItem().getStack().getDisplayName().getString();
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
        public ITextComponent getTooltip() {
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
        public ITextComponent getTooltip() {
            return tooltip.translate();
        }

        @Override
        public ITextComponent getShortName() {
            return name.translate();
        }
    }
}
