package mekanism.common.inventory.container;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTranslationKey.IHasEnumNameTranslationKey;
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
import mekanism.common.inventory.container.slot.InsertableSlot;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.VirtualCraftingOutputSlot;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.inventory.HashedItem.UUIDAwareHashedItem;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.qio.BulkQIOData;
import mekanism.common.network.to_server.qio.PacketQIOItemViewerSlotPlace;
import mekanism.common.network.to_server.qio.PacketQIOItemViewerSlotShiftTake;
import mekanism.common.network.to_server.qio.PacketQIOItemViewerSlotTake;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.TranslatableEnum;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.lwjgl.glfw.GLFW;

public abstract class QIOItemViewerContainer extends MekanismContainer implements ISlotClickHandler {

    public static final int SLOTS_X_MIN = 8, SLOTS_X_MAX = 16, SLOTS_Y_MIN = 2, SLOTS_Y_MAX = 48;
    public static final int SLOTS_START_Y = 43;
    private static final int DOUBLE_CLICK_TRANSFER_DURATION = SharedConstants.TICKS_PER_SECOND;

    public static int getSlotsYMax() {
        int maxY = Mth.ceil(Minecraft.getInstance().getWindow().getGuiScaledHeight() * 0.05 - 8) + 1;
        return Mth.clamp(maxY, SLOTS_Y_MIN, SLOTS_Y_MAX);
    }

    private final Map<UUID, ItemSlotData> cachedInventory;
    protected final IQIOCraftingWindowHolder craftingWindowHolder;
    private final List<IScrollableSlot> searchList;
    private final List<IScrollableSlot> itemList;

    private long cachedCountCapacity;
    private int cachedTypeCapacity;
    private long totalItems;

    private ListSortType sortType;
    private SortDirection sortDirection;
    private String rawSearchQuery;
    private ISearchQuery searchQuery;

    private int doubleClickTransferTicks = 0;
    private int lastSlot = -1;
    private ItemStack lastStack = ItemStack.EMPTY;
    private List<InventoryContainerSlot>[] craftingGridInputSlots;
    private final VirtualInventoryContainerSlot[][] craftingSlots = new VirtualInventoryContainerSlot[IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS][10];

    private boolean sortingPaused;
    private SortingNeeded sortingNeeded;
    private final Set<UUID> queuedForRemoval;

    protected QIOItemViewerContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, boolean remote, IQIOCraftingWindowHolder craftingWindowHolder,
          BulkQIOData itemData, CachedSearchData searchData, CachedSortingData sortingData, @Nullable SelectedWindowData selectedWindow) {
        super(type, id, inv);
        this.craftingWindowHolder = craftingWindowHolder;
        this.cachedCountCapacity = itemData.countCapacity();
        this.cachedTypeCapacity = itemData.typeCapacity();
        this.cachedInventory = itemData.inventory();
        this.totalItems = itemData.totalItems();
        this.itemList = itemData.items();
        this.searchList = searchData.cachedList();
        this.rawSearchQuery = searchData.rawQuery();
        this.searchQuery = searchData.query();
        this.sortType = sortingData.sortType();
        this.sortDirection = sortingData.sortDirection();
        this.sortingNeeded = sortingData.sortingNeeded();
        this.selectedWindow = selectedWindow;
        this.queuedForRemoval = remote ? new HashSet<>() : Collections.emptySet();
        if (this.craftingWindowHolder == null) {
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

            //If we are on the client, so we may have items from the server, make sure we sort it;
            // this ensures it matches the order defined by the sort type and direction stored in the client config
            //Note: While this also is called when we recreate the viewer, it will get NO-OP'd because the SortingNeeded will be NONE
            updateSort();
            //If we want to rebuild the search (such as if we eventually make it so that we can persist the current search query in a client config)
            // then we need to update the search list
            if (sortingData.rebuildSearch()) {
                updateSearch(getLevel(), rawSearchQuery, false);
            }
        } else {
            craftingGridInputSlots = new List[IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS];
        }
    }

    @Nullable
    public QIOFrequency getFrequency() {
        return craftingWindowHolder.getFrequency();
    }

    public abstract boolean shiftClickIntoFrequency();

    public abstract void toggleTargetDirection();

    /**
     * @apiNote Only used on the client
     */
    public QIOItemViewerContainer recreate() {
        //If sorting is currently paused, unpause it and apply any sorting necessary so that we don't have to transfer what the sorting state is
        boolean wasPaused = sortingPaused;
        pauseSorting(false);
        QIOItemViewerContainer container = recreateUnchecked();
        //Note: We want to make sure to pause sorting again on the new container as we only pause/unpause on key press/release and not when holding it
        container.pauseSorting(wasPaused);
        return container;
    }

    /**
     * @apiNote Only used on the client
     */
    protected abstract QIOItemViewerContainer recreateUnchecked();

    @Override
    protected int getInventoryYOffset() {
        //Use get or default as server side these configs don't exist but the config should be just fine
        return SLOTS_START_Y + MekanismConfig.client.qioItemViewerSlotsY.getOrDefault() * 18 + 15;
    }

    @Override
    protected int getInventoryXOffset() {
        //Use get or default as server side these configs don't exist but the config should be just fine
        return super.getInventoryXOffset() + (MekanismConfig.client.qioItemViewerSlotsX.getOrDefault() - 8) * 18 / 2;
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

    private void addCraftingSlot(IInventorySlot slot, byte tableIndex, int slotIndex) {
        VirtualInventoryContainerSlot containerSlot = (VirtualInventoryContainerSlot) slot.createContainerSlot();
        craftingSlots[tableIndex][slotIndex] = containerSlot;
        addSlot(containerSlot);
    }

    public VirtualInventoryContainerSlot getCraftingWindowSlot(byte tableIndex, int slotIndex) {
        return craftingSlots[tableIndex][slotIndex];
    }

    @Override
    protected void openInventory(@NotNull Inventory inv) {
        super.openInventory(inv);
        if (!getLevel().isClientSide()) {
            QIOFrequency freq = getFrequency();
            if (freq != null) {
                freq.openItemViewer((ServerPlayer) inv.player);
            }
        }
    }

    @Override
    protected void closeInventory(@NotNull Player player) {
        super.closeInventory(player);
        if (!player.level().isClientSide()) {
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
        if (freq != null) {
            for (InsertableSlot slot : mainInventorySlots) {
                handleDoDoubleClickTransfer(player, slot, freq);
            }
            for (InsertableSlot slot : hotBarSlots) {
                handleDoDoubleClickTransfer(player, slot, freq);
            }
        }
    }

    private void handleDoDoubleClickTransfer(Player player, InsertableSlot slot, QIOFrequency freq) {
        if (slot.hasItem() && slot.mayPickup(player)) {
            //Note: We don't need to sanitize the slot's items as these are just InsertableSlots which have no restrictions on them on how much
            // can be extracted at once so even if they somehow have an oversized stack it will be fine
            ItemStack slotItem = slot.getItem();
            if (InventoryUtils.areItemsStackable(lastStack, slotItem)) {
                this.transferSuccess(slot, player, slotItem, freq.addItem(slotItem));
            }
        }
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

    @NotNull
    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int slotID) {
        Slot currentSlot = slots.get(slotID);
        switch (currentSlot) {
            case null -> {
                return ItemStack.EMPTY;
            }
            case VirtualCraftingOutputSlot virtualSlot -> {
                //If we are clicking an output crafting slot, allow the slot itself to handle the transferring
                return virtualSlot.shiftClickSlot(player, hotBarSlots, mainInventorySlots);
            }
            case InventoryContainerSlot inventoryContainerSlot -> {
                //Otherwise, if we are an inventory container slot (crafting input slots in this case)
                // use our normal handling to attempt and transfer the contents to the player's inventory
                return super.quickMoveStack(player, slotID);
            }
            default -> {
            }
        }
        // special handling for shift-clicking into GUI
        if (!player.level().isClientSide()) {
            //Note: We don't need to sanitize the slot's items as these are just InsertableSlots which have no restrictions on them on how much
            // can be extracted at once so even if they somehow have an oversized stack it will be fine
            ItemStack slotStack = currentSlot.getItem();
            if (!shiftClickIntoFrequency()) {
                Optional<ItemStack> windowHandling = tryTransferToWindow(player, currentSlot, slotStack);
                if (windowHandling.isPresent()) {
                    return windowHandling.get();
                }
            }
            QIOFrequency frequency = getFrequency();
            if (frequency != null) {
                if (!slotStack.isEmpty()) {
                    //There is an item in the slot
                    ItemStack ret = frequency.addItem(slotStack);
                    if (slotStack.getCount() != ret.getCount()) {
                        //We were able to insert some of it
                        //Make sure that we copy it so that we aren't just pointing to the reference of it
                        setTransferTracker(slotStack.copy(), slotID);
                        return transferSuccess(currentSlot, player, slotStack, ret);
                    }
                } else {
                    if (slotID == lastSlot && !lastStack.isEmpty()) {
                        doDoubleClickTransfer(player);
                    }
                    resetTransferTracker();
                    return ItemStack.EMPTY;
                }
            }
            if (shiftClickIntoFrequency()) {
                //If we tried to shift click it into the frequency first, but weren't able to transfer it
                // either because we don't have a frequency or the frequency is full:
                // try to transfer it a potentially open window
                return tryTransferToWindow(player, currentSlot, slotStack).orElse(ItemStack.EMPTY);
            }
        }
        return ItemStack.EMPTY;
    }

    private Optional<ItemStack> tryTransferToWindow(Player player, Slot currentSlot, ItemStack slotStack) {
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
                    return Optional.of(transferSuccess(currentSlot, player, slotStack, stackToInsert));
                }
                //Otherwise, if nothing changed, try to transfer into the QIO Frequency
            }
        }
        return Optional.empty();
    }

    public void handleUpdate(Object2LongMap<UUIDAwareHashedItem> itemMap, long countCapacity, int typeCapacity) {
        cachedCountCapacity = countCapacity;
        cachedTypeCapacity = typeCapacity;
        if (itemMap.isEmpty()) {
            //No items need updating, we just changed the counts/capacities, in general this should never be the case, but in case it is
            // just short circuit a lot of logic
            return;
        }
        for (ObjectIterator<Object2LongMap.Entry<UUIDAwareHashedItem>> iterator = Object2LongMaps.fastIterator(itemMap); iterator.hasNext(); ) {
            Object2LongMap.Entry<UUIDAwareHashedItem> entry = iterator.next();
            UUIDAwareHashedItem itemKey = entry.getKey();
            UUID itemUUID = itemKey.getUUID();
            long value = entry.getLongValue();
            if (value == 0) {
                //Note: No sorting is required when removing as the lists will already be in the correct order
                if (sortingPaused) {
                    ItemSlotData oldData = cachedInventory.get(itemUUID);
                    if (oldData == null) {
                        //Skip any keys we don't actually have stored
                        continue;
                    }
                    //Remove the item from the stored total count. Even if we for some reason already removed it,
                    // this will just subtract zero so won't make the value incorrect
                    totalItems -= oldData.count();
                    oldData.count = 0;
                    queuedForRemoval.add(itemUUID);
                } else {
                    ItemSlotData oldData = removeItemBasic(itemUUID);
                    if (oldData != null) {
                        //If we did in fact have old data stored (that has now been removed), remove the item from the stored total count
                        totalItems -= oldData.count();
                    }
                }
            } else {
                ItemSlotData slotData = cachedInventory.get(itemUUID);
                if (slotData == null) {
                    //If it is a new item, add the amount to the total items, and start tracking it
                    totalItems += value;
                    slotData = new ItemSlotData(itemKey, value);
                    itemList.add(slotData);
                    cachedInventory.put(itemUUID, slotData);
                    //Mark that we have some items that changed (which may affect the sort order)
                    sortingNeeded = sortingNeeded.concat(SortingNeeded.ITEMS_ONLY);
                    //If the item we added matches the current search query
                    if (searchQuery.test(getLevel(), inv.player, slotData.getInternalStack())) {
                        // add it to the end of the search list
                        // Note: We already know it isn't part of the searchList, as it wasn't part of our universe (cachedInventory)
                        searchList.add(slotData);
                        // and mark that we will need to sort the search list as well
                        sortingNeeded = sortingNeeded.concat(SortingNeeded.SEARCH_ONLY);
                    }
                } else {
                    //If an existing item is updated, update the stored amount by the change in quantity
                    totalItems += value - slotData.count();
                    slotData.count = value;
                    if (sortType.usesCount()) {
                        //If our sort type actually makes use of the item count on some level, then we need to mark that the item list needs to be sorted
                        sortingNeeded = sortingNeeded.concat(SortingNeeded.ITEMS_ONLY);
                        if (searchQuery.test(getLevel(), inv.player, slotData.getInternalStack())) {
                            // and if the item is in our search query, then we also need to sort the search list
                            sortingNeeded = sortingNeeded.concat(SortingNeeded.SEARCH_ONLY);
                        }
                    }
                }
            }
        }
        if (!sortingPaused) {
            //Try updating the sort as if anything got added/removed or changed so that it is potentially now in the wrong spot
            // we will have set how much we need to sort above.
            // Note: We will properly short circuit in the below method if no sorting is needed
            updateSort();
        }
    }

    /**
     * Removes an item from the cached inventory, item list, and search list
     *
     * @return The previously stored cached data, or null if the item was not part of the cached inventory.
     */
    @Nullable
    private ItemSlotData removeItemBasic(UUID itemUUID) {
        ItemSlotData oldData = cachedInventory.remove(itemUUID);
        if (oldData != null) {//Note: Implementation detail is that we use a ReferenceArrayList in BulkQIOData#fromPacket to ensure that when removing
            // we only need to do reference equality instead of object equality
            //TODO: Can we somehow make removing more efficient by taking advantage of the fact that itemList is sorted?
            itemList.remove(oldData);
            if (searchQuery.test(getLevel(), inv.player, oldData.getInternalStack())) {
                //If item being removed matched the existing search, we want to remove it from the search list as well
                searchList.remove(oldData);
            }
        }
        return oldData;
    }

    public void pauseSorting(boolean pause) {
        if (this.sortingPaused != pause) {
            this.sortingPaused = pause;
            if (!this.sortingPaused) {
                //The user was holding shift (had sorting paused), and no longer is
                // We now need to perform and queued sorting and item removal
                for (UUID toRemove : queuedForRemoval) {
                    ItemSlotData slotData = cachedInventory.get(toRemove);
                    if (slotData != null && slotData.count() == 0) {
                        //If we have the item stored, and we haven't gotten any added back since we started pausing (aka we have zero stored), then we want to remove it
                        // Note: Theoretically this will never be null as we only would add things for removal if we had them stored.
                        // We also don't have to adjust the total item count, as we did that when we initially got the removal data
                        removeItemBasic(toRemove);
                    }
                }
                queuedForRemoval.clear();
                //Attempt to run sorting. This will NO-OP if we didn't actually end up needing any sorting
                // Note: If all that changed was removal, we won't need to do any sorting, as the lists should already be in the correct order
                updateSort();
            }
        }
    }

    public void handleKill() {
        cachedInventory.clear();
        searchList.clear();
        itemList.clear();
        rawSearchQuery = "";
        searchQuery = ISearchQuery.INVALID;
        sortingPaused = false;
        sortingNeeded = SortingNeeded.NONE;
        queuedForRemoval.clear();
    }

    public QIOCraftingTransferHelper getTransferHelper(Player player, QIOCraftingWindow craftingWindow) {
        return new QIOCraftingTransferHelper(cachedInventory.values(), hotBarSlots, mainInventorySlots, craftingWindow, player);
    }

    /**
     * @apiNote Only call this client side
     */
    public void setSortDirection(SortDirection sortDirection) {
        if (this.sortDirection != sortDirection) {
            this.sortDirection = sortDirection;
            MekanismConfig.client.qioItemViewerSortDirection.set(sortDirection);
            applySortingOptionChange();
        }
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    /**
     * @apiNote Only call this client side
     */
    public void setSortType(ListSortType sortType) {
        if (this.sortType != sortType) {
            this.sortType = sortType;
            MekanismConfig.client.qioItemViewerSortType.set(sortType);
            applySortingOptionChange();
        }
    }

    /**
     * @apiNote Only call this client side
     */
    private void applySortingOptionChange() {
        MekanismConfig.client.save();
        this.sortingNeeded = SortingNeeded.ALL;
        updateSort();
    }

    public ListSortType getSortType() {
        return sortType;
    }

    @NotNull
    public List<IScrollableSlot> getQIOItemList() {
        return searchQuery.isInvalid() ? itemList : searchList;
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

    /**
     * @apiNote Only call on server
     */
    public ItemStack simulateInsertIntoPlayerInventory(UUID player, ItemStack stack) {
        SelectedWindowData selectedWindow = getSelectedWindow(player);
        stack = insertItemCheckAll(hotBarSlots, stack, selectedWindow, Action.SIMULATE);
        stack = insertItemCheckAll(mainInventorySlots, stack, selectedWindow, Action.SIMULATE);
        return stack;
    }

    private void updateSort() {
        if (sortingNeeded.sortItemList()) {
            sortType.sort(itemList, sortDirection);
        }
        if (sortingNeeded.sortSearchList()) {
            sortType.sort(searchList, sortDirection);
        }
        //Fully sorted, we can unmark that we need to do any sorting
        sortingNeeded = SortingNeeded.NONE;
    }

    public void updateSearch(@Nullable Level level, String queryText, boolean skipSameQuery) {
        // searches should only be updated on the client-side
        if (level == null || !level.isClientSide()) {
            return;
        }
        queryText = queryText.trim().toLowerCase(Locale.ROOT);
        if (skipSameQuery && rawSearchQuery.equals(queryText)) {
            //Short circuit and skip updating the search if we already have the results
            //TODO: Do we want to compare if the search queries are equal here instead of just the raw text?
            // That way we can potentially handle ignoring things like lower vs uppercase of the same letter.
            // For now it doesn't matter as the search query doesn't match equality if the capitalization is different
            return;
        }
        rawSearchQuery = queryText;
        //TODO: Improve how we cache to allow for some form of incremental updating based on the search text changing?
        searchQuery = SearchQueryParser.parse(rawSearchQuery);

        if (sortingPaused) {
            //If we are updating the search and sorting is currently paused, we want to sort everything before processing the changed search text
            // This is because the most likely occurrence will be when typing a capital letter, and we want to make sure that they can see the results
            // of their new search.
            if (sortingNeeded.sortSearchList()) {
                //If we needed to sort the search list as well, then removal that from the desired sorting type as we will be recreating the search list
                // from a sorted itemList
                sortingNeeded = sortingNeeded.sortItemList() ? SortingNeeded.ITEMS_ONLY : SortingNeeded.NONE;
            }
            //Note: We unpause sorting and then pause it again rather than just calling updateSort directly so that we can prune any items that are queued for removal
            pauseSorting(false);
            pauseSorting(true);
        }

        searchList.clear();
        if (!searchQuery.isInvalid()) {
            for (IScrollableSlot slot : itemList) {
                if (searchQuery.test(level, inv.player, slot.getInternalStack())) {
                    searchList.add(slot);
                }
            }
        }
    }

    protected BulkQIOData asBulkData() {
        return new BulkQIOData(cachedInventory, getCountCapacity(), getTypeCapacity(), getTotalItems(), itemList);
    }

    protected CachedSearchData asCachedSearchData() {
        return new CachedSearchData(searchQuery, rawSearchQuery, searchList);
    }

    protected CachedSortingData currentSortingData() {
        return new CachedSortingData(sortType, sortDirection);
    }

    @Override
    public void onClick(Supplier<@Nullable IScrollableSlot> slotProvider, int button, boolean hasShiftDown, ItemStack heldItem) {
        if (hasShiftDown) {
            IScrollableSlot slot = slotProvider.get();
            if (slot != null) {
                PacketUtils.sendToServer(new PacketQIOItemViewerSlotShiftTake(slot.itemUUID()));
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT || button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            if (heldItem.isEmpty()) {
                IScrollableSlot slot = slotProvider.get();
                if (slot != null && slot.count() > 0) {
                    int maxStackSize = Math.min(MathUtils.clampToInt(slot.count()), slot.item().getMaxStackSize());
                    //Left click -> as much as possible, right click -> half of a stack, middle click -> 1
                    //Cap it out at the max stack size of the item, but otherwise try to take the desired amount (taking at least one if it is a single item)
                    int toTake;
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        toTake = maxStackSize;
                    } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                        toTake = 1;
                    } else {
                        toTake = Math.max(1, maxStackSize / 2);
                    }
                    PacketUtils.sendToServer(new PacketQIOItemViewerSlotTake(slot.itemUUID(), toTake));
                }
            } else {
                //middle click -> add to current stack if over slot and stackable, else normal storage functionality
                IScrollableSlot slot;
                if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && (slot = slotProvider.get()) != null && InventoryUtils.areItemsStackable(heldItem, slot.getInternalStack())) {
                    PacketUtils.sendToServer(new PacketQIOItemViewerSlotTake(slot.itemUUID(), 1));
                } else {
                    //Left click -> all held, right click -> single item
                    int toAdd = button == GLFW.GLFW_MOUSE_BUTTON_LEFT ? heldItem.getCount() : 1;
                    PacketUtils.sendToServer(new PacketQIOItemViewerSlotPlace(toAdd));
                }
            }
        }
    }

    public static final class ItemSlotData implements IScrollableSlot {

        private final UUIDAwareHashedItem item;
        private long count;

        public ItemSlotData(UUIDAwareHashedItem item, long count) {
            this.item = item;
            this.count = count;
        }

        @Override
        public HashedItem asRawHashedItem() {
            return item.asRawHashedItem();
        }

        @Override
        public UUIDAwareHashedItem item() {
            return item;
        }

        @Override
        public UUID itemUUID() {
            return item.getUUID();
        }

        @Override
        @Range(from = 0, to = Long.MAX_VALUE)
        public long count() {
            return count;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            //TODO: Strictly speaking we might be able to get away with just checking the uuid instead of if the entire item is equal
            // Or maybe we want to just use custom hash strategy? Though maybe none of that matters as I think we only use this for a reference array list
            // so we already skip using this method
            ItemSlotData other = (ItemSlotData) obj;
            return this.count == other.count && this.item.equals(other.item);
        }

        @Override
        public int hashCode() {
            int result = item.hashCode();
            result = 31 * result + Long.hashCode(count);
            return result;
        }
    }

    public enum SortDirection implements IToggleEnum<SortDirection>, IHasEnumNameTranslationKey {
        ASCENDING(MekanismUtils.getResource(ResourceType.GUI, "arrow_up.png"), MekanismLang.LIST_SORT_ASCENDING, MekanismLang.LIST_SORT_ASCENDING_DESC),
        DESCENDING(MekanismUtils.getResource(ResourceType.GUI, "arrow_down.png"), MekanismLang.LIST_SORT_DESCENDING, MekanismLang.LIST_SORT_DESCENDING_DESC);

        private final ResourceLocation icon;
        private final ILangEntry name;
        private final ILangEntry tooltip;

        SortDirection(ResourceLocation icon, ILangEntry name, ILangEntry tooltip) {
            this.icon = icon;
            this.name = name;
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

        @NotNull
        @Override
        public String getTranslationKey() {
            return name.getTranslationKey();
        }
    }

    public enum ListSortType implements IDropdownEnum<ListSortType>, TranslatableEnum {
        NAME(MekanismLang.LIST_SORT_NAME, MekanismLang.LIST_SORT_NAME_DESC, false, Comparator.comparing(IScrollableSlot::getDisplayName)),
        SIZE(MekanismLang.LIST_SORT_COUNT, MekanismLang.LIST_SORT_COUNT_DESC, true,
              Comparator.comparingLong(IScrollableSlot::count).thenComparing(IScrollableSlot::getDisplayName),
              Comparator.comparingLong(IScrollableSlot::count).reversed().thenComparing(IScrollableSlot::getDisplayName)),
        MOD(MekanismLang.LIST_SORT_MOD, MekanismLang.LIST_SORT_MOD_DESC, false,
              Comparator.comparing(IScrollableSlot::getModID).thenComparing(IScrollableSlot::getDisplayName),
              Comparator.comparing(IScrollableSlot::getModID).reversed().thenComparing(IScrollableSlot::getDisplayName)),
        REGISTRY_NAME(MekanismLang.LIST_SORT_REGISTRY_NAME, MekanismLang.LIST_SORT_REGISTRY_NAME_DESC, true,
              Comparator.comparing(IScrollableSlot::getRegistryName, ResourceLocation::compareNamespaced).thenComparingLong(IScrollableSlot::count),
              Comparator.comparing(IScrollableSlot::getRegistryName, ResourceLocation::compareNamespaced).reversed().thenComparingLong(IScrollableSlot::count));

        private final ILangEntry name;
        private final ILangEntry tooltip;
        private final boolean usesCount;
        private final Comparator<IScrollableSlot> ascendingComparator;
        private final Comparator<IScrollableSlot> descendingComparator;

        ListSortType(ILangEntry name, ILangEntry tooltip, boolean usesCount, Comparator<IScrollableSlot> ascendingComparator) {
            this(name, tooltip, usesCount, ascendingComparator, ascendingComparator.reversed());
        }

        ListSortType(ILangEntry name, ILangEntry tooltip, boolean usesCount, Comparator<IScrollableSlot> ascendingComparator,
              Comparator<IScrollableSlot> descendingComparator) {
            this.name = name;
            this.tooltip = tooltip;
            this.usesCount = usesCount;
            this.ascendingComparator = ascendingComparator;
            this.descendingComparator = descendingComparator;
        }

        public void sort(List<IScrollableSlot> list, SortDirection direction) {
            if (!list.isEmpty()) {
                list.sort(direction.isAscending() ? ascendingComparator : descendingComparator);
            }
        }

        /**
         * @return true if the sort type has any level of sorting based on count
         */
        public boolean usesCount() {
            return usesCount;
        }

        @Override
        public Component getTooltip() {
            return tooltip.translate();
        }

        @Override
        public Component getShortName() {
            return name.translate();
        }

        @NotNull
        @Override
        public Component getTranslatedName() {
            return getShortName();
        }
    }

    private enum SortingNeeded {
        NONE(false, false),
        ITEMS_ONLY(true, false),
        SEARCH_ONLY(false, true),
        ALL(true, true);

        private final boolean sortItems, sortSearch;

        SortingNeeded(boolean sortItems, boolean sortSearch) {
            this.sortItems = sortItems;
            this.sortSearch = sortSearch;
        }

        public SortingNeeded concat(SortingNeeded toConcat) {
            boolean sortSearch = sortSearchList() || toConcat.sortSearchList();
            if (sortItemList() || toConcat.sortItemList()) {
                return sortSearch ? ALL : ITEMS_ONLY;
            }
            return sortSearch ? SEARCH_ONLY : NONE;
        }

        public boolean sortItemList() {
            return sortItems;
        }

        public boolean sortSearchList() {
            return sortSearch;
        }
    }

    public record CachedSearchData(ISearchQuery query, String rawQuery, List<IScrollableSlot> cachedList) {

        public static final CachedSearchData INITIAL_SERVER = new CachedSearchData(ISearchQuery.INVALID, "", Collections.emptyList());

        public static CachedSearchData initialClient() {
            //Note: Use a ReferenceArrayList to allow for instance equality checking when removing elements
            return new CachedSearchData(ISearchQuery.INVALID, "", new ReferenceArrayList<>());
        }
    }

    public record CachedSortingData(ListSortType sortType, SortDirection sortDirection, SortingNeeded sortingNeeded, boolean rebuildSearch) {

        public static final CachedSortingData SERVER = new CachedSortingData(ListSortType.NAME, SortDirection.ASCENDING);

        public static CachedSortingData currentClient() {
            //Note: As we are receiving this from packet, we have no existing search data, so we can skip attempting to recreate the search list
            return new CachedSortingData(MekanismConfig.client.qioItemViewerSortType.get(), MekanismConfig.client.qioItemViewerSortDirection.get(),
                  SortingNeeded.ITEMS_ONLY, false);
        }

        public CachedSortingData(ListSortType sortType, SortDirection sortDirection) {
            this(sortType, sortDirection, SortingNeeded.NONE, false);
        }

        public CachedSortingData {
            //If we want to rebuild the search, don't bother also sorting the search list first
            if (rebuildSearch && sortingNeeded.sortSearchList()) {
                sortingNeeded = sortingNeeded.sortItemList() ? SortingNeeded.ITEMS_ONLY : SortingNeeded.NONE;
            }
        }
    }
}