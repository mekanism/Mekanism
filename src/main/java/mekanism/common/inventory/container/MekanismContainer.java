package mekanism.common.inventory.container;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortUnaryOperator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import mekanism.api.AutomationType;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.slot.ArmorSlot;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.IHasExtraData;
import mekanism.common.inventory.container.slot.ITransactionalSlot;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.MainInventorySlot;
import mekanism.common.inventory.container.slot.OffhandSlot;
import mekanism.common.inventory.container.slot.TransactionalSlot;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.ISyncableData.DirtyType;
import mekanism.common.inventory.container.sync.SyncableBlockPos;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableByte;
import mekanism.common.inventory.container.sync.SyncableByteArray;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableFloat;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.container.sync.SyncableLargeResourceStack;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.container.sync.SyncableRegistryEntry;
import mekanism.common.inventory.container.sync.SyncableResource;
import mekanism.common.inventory.container.sync.SyncableShort;
import mekanism.common.inventory.container.sync.SyncableStreamCodec;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.container.PacketUpdateContainer;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_server.PacketWindowSelect;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public abstract class MekanismContainer extends AbstractContainerMenu implements ISecurityContainer {

    public static final int BASE_Y_OFFSET = 84;
    public static final int TRANSPORTER_CONFIG_WINDOW = 0;
    public static final int SIDE_CONFIG_WINDOW = 1;
    public static final int UPGRADE_WINDOW = 2;
    public static final int SKIN_SELECT_WINDOW = 3;

    protected final Inventory inv;
    protected final List<InventoryContainerSlot> inventoryContainerSlots = new ArrayList<>();
    protected final List<ArmorSlot> armorSlots = new ArrayList<>();
    protected final List<MainInventorySlot> mainInventorySlots = new ArrayList<>();
    protected final List<HotBarSlot> hotBarSlots = new ArrayList<>();
    protected final List<OffhandSlot> offhandSlots = new ArrayList<>();
    private final Iterable<TransactionalSlot> playerSlots = Iterables.concat(hotBarSlots, mainInventorySlots);
    private final List<ISyncableData> trackedData = new ArrayList<>();
    private final Map<Object, List<ISyncableData>> specificTrackedData = new Object2ObjectOpenHashMap<>();
    /// Keeps track of which window the player has open. Only used on the client, so doesn't need to keep track of other players.
    ///
    /// @apiNote Don't set this directly use the [#setSelectedWindow(SelectedWindowData)] instead, this is just protected so that the QIO item viewer container can copy
    /// it directly to the new container.
    @Nullable
    protected SelectedWindowData selectedWindow;
    /// Only used on the server
    private final Map<UUID, SelectedWindowData> selectedWindows;

    protected MekanismContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv) {
        super(type.get(), id);
        this.inv = inv;
        if (!getLevel().isClientSide()) {
            //Only keep track of uuid based selected grids on the server (we use a size of one as for the most part containers are actually 1:1)
            selectedWindows = new HashMap<>(1);
        } else {
            selectedWindows = Collections.emptyMap();
        }
    }

    public UUID getPlayerUUID() {
        return inv.player.getUUID();
    }

    public Level getLevel() {
        return inv.player.level();
    }

    @Override
    protected Slot addSlot(Slot slot) {
        super.addSlot(slot);
        if (slot instanceof IHasExtraData hasExtraData) {
            //If the slot has any extra data, allow it to add any trackers it may have
            hasExtraData.addTrackers(inv.player, this::track);
        }
        if (slot instanceof InventoryContainerSlot inventorySlot) {
            inventoryContainerSlots.add(inventorySlot);
        } else if (slot instanceof ArmorSlot armorSlot) {
            armorSlots.add(armorSlot);
        } else if (slot instanceof MainInventorySlot inventorySlot) {
            mainInventorySlots.add(inventorySlot);
        } else if (slot instanceof HotBarSlot hotBarSlot) {
            hotBarSlots.add(hotBarSlot);
        } else if (slot instanceof OffhandSlot offhandSlot) {
            offhandSlots.add(offhandSlot);
        }
        return slot;
    }

    /// Adds slots and opens, must be called at end of extending classes constructors
    protected void addSlotsAndOpen() {
        addSlots();
        addInventorySlots(inv);
        openInventory(inv);
    }

    public void startTrackingServer(Object key, ISpecificContainerTracker tracker) {
        int currentSize = trackedData.size();
        List<ISyncableData> list = startTracking(key, tracker);
        //Do the initial sync of all newly tracked data
        sendInitialDataToRemote(list, index -> (short) (index + currentSize));
    }

    public List<ISyncableData> startTracking(Object key, ISpecificContainerTracker tracker) {
        List<ISyncableData> list = tracker.getSpecificSyncableData(getLevel());
        for (ISyncableData data : list) {
            track(data);
        }
        specificTrackedData.put(key, list);
        return list;
    }

    public void stopTracking(Object key) {
        List<ISyncableData> list = specificTrackedData.remove(key);
        if (list != null) {
            trackedData.removeAll(list);
        }
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        if (slot instanceof ITransactionalSlot transactionalSlot) {
            if (!transactionalSlot.canMergeWith(stack)) {
                return false;
            }
            SelectedWindowData selectedWindow = getLevel().isClientSide() ? getSelectedWindow() : getSelectedWindow(getPlayerUUID());
            return transactionalSlot.exists(selectedWindow) && super.canTakeItemForPickAll(stack, slot);
        }
        return super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        closeInventory(player);
    }

    protected void closeInventory(Player player) {
        if (!player.level().isClientSide()) {
            clearSelectedWindow(player.getUUID());
        }
    }

    protected void openInventory(Inventory inv) {
    }

    protected int getInventoryYOffset() {
        return BASE_Y_OFFSET;
    }

    protected int getInventoryXOffset() {
        return 8;
    }

    protected void addInventorySlots(Inventory inv) {
        if (this instanceof IEmptyContainer) {
            //Don't include the player's inventory slots
            return;
        }
        int yOffset = getInventoryYOffset();
        int xOffset = getInventoryXOffset();
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                addSlot(new MainInventorySlot(inv, Inventory.getSelectionSize() + slotX + slotY * 9, xOffset + slotX * 18, yOffset + slotY * 18));
            }
        }
        yOffset += 58;
        for (int slotX = 0; slotX < Inventory.getSelectionSize(); slotX++) {
            addSlot(createHotBarSlot(inv, slotX, xOffset + slotX * 18, yOffset));
        }
    }

    protected void addArmorSlots(Inventory inv, int x, int y, int offhandOffset) {
        int armorSlots = 4;
        for (int index = 0; index < armorSlots; index++) {
            final EquipmentSlot slotType = EquipmentSlot.VALUES.get(2 + armorSlots - index - 1);
            addSlot(new ArmorSlot(inv, 36 + armorSlots - index - 1, x, y, slotType));
            y += 18;
        }
        if (offhandOffset != -1) {
            addSlot(new OffhandSlot(inv, Inventory.SLOT_OFFHAND, x, y + offhandOffset, inv.player));
        }
    }

    protected HotBarSlot createHotBarSlot(Inventory inv, int index, int x, int y) {
        return new HotBarSlot(inv, index, x, y);
    }

    protected void addSlots() {
    }

    public List<InventoryContainerSlot> getInventoryContainerSlots() {
        return Collections.unmodifiableList(inventoryContainerSlots);
    }

    public TransactionalSlot getPlayerSlot(int slot) {
        if (slot < Inventory.getSelectionSize()) {//Hotbar
            return hotBarSlots.get(slot);
        }//Main inventory
        return mainInventorySlots.get(slot - Inventory.getSelectionSize());
    }

    public int getNumPlayerSlots() {
        return hotBarSlots.size() + mainInventorySlots.size();
    }

    public Iterable<TransactionalSlot> getPlayerSlots() {
        return Iterables.unmodifiableIterable(playerSlots);
    }

    /// @return The contents in this slot AFTER transferring items away.
    @Override
    public ItemStack quickMoveStack(Player player, int slotID) {
        Slot currentSlot = getSlot(slotID);
        if (currentSlot == null || !currentSlot.hasItem()) {
            return ItemStack.EMPTY;
        }
        SelectedWindowData selectedWindow = player.level().isClientSide() ? getSelectedWindow() : getSelectedWindow(player.getUUID());
        if (currentSlot instanceof ITransactionalSlot insertableSlot && !insertableSlot.exists(selectedWindow)) {
            return ItemStack.EMPTY;
        }
        int inserted;
        try (Transaction transaction = Transaction.openRoot()) {
            ItemStack slotStack = currentSlot.getItem();
            ItemResource itemToInsert = ItemResource.of(slotStack);
            //TODO: Do we want to add support for limiting how much can be extracted at any one time to slots?
            // If so it would probably be easiest to just change this line
            int amountToInsert = slotStack.count();
            if (currentSlot instanceof InventoryContainerSlot) {
                //Insert into stacks that already contain an item in the order armor, hot bar -> main inventory
                // followed by trying to insert into empty slots
                inserted = insertItem(Iterables.concat(armorSlots, hotBarSlots, mainInventorySlots), itemToInsert, amountToInsert, transaction, selectedWindow);
            } else {
                //We are in the main inventory or the hot bar
                //Start by trying to insert it into the tile's inventory slots, first attempting to stack with other items
                inserted = insertItem(inventoryContainerSlots, itemToInsert, amountToInsert, transaction, true, selectedWindow);
                if (inserted == 0) {
                    //Then as long as if we still have the same number of items (failed to insert), try to insert it into the tile's inventory slots allowing for empty items
                    inserted = insertItem(inventoryContainerSlots, itemToInsert, amountToInsert, transaction, false, selectedWindow);
                    if (inserted == 0) {
                        //Else if we failed to do that also, try transferring to armor inventory, main inventory or the hot bar, depending on which one we currently are in
                        if (currentSlot instanceof ArmorSlot || currentSlot instanceof OffhandSlot) {
                            inserted = insertItem(playerSlots, itemToInsert, amountToInsert, transaction, selectedWindow);
                        } else if (currentSlot instanceof MainInventorySlot || currentSlot instanceof HotBarSlot) {
                            inserted = insertItem(armorSlots, itemToInsert, amountToInsert, transaction, false, selectedWindow);
                            if (currentSlot instanceof MainInventorySlot) {
                                inserted += insertItem(hotBarSlots, itemToInsert, amountToInsert - inserted, transaction, selectedWindow);
                            } else {//HotBarSlot
                                inserted += insertItem(mainInventorySlots, itemToInsert, amountToInsert - inserted, transaction, selectedWindow);
                            }
                        } else {
                            //TODO: Should we add a warning message so we can find out if we ever end up here. (Given we should never end up here anyways)
                        }
                    }
                }
            }
            if (inserted == 0) {
                //If nothing changed then return that fact
                return ItemStack.EMPTY;
            }
            //Otherwise, decrease the stack by the amount we inserted, and return it as a new stack for what is now in the slot
            transaction.commit();
            return transferSuccess(currentSlot, player, inserted);
        }
    }

    /// Helper to first try inserting ignoring empty slots, and then insert not ignoring empty slots
    ///
    /// @param slots          Slots to insert into
    /// @param itemType       Type of item to insert.
    /// @param amount         Amount of the item to insert.
    /// @param transaction    The transaction that this operation is part of.
    /// @param selectedWindow Selected window, or null if there is no window selected. This mostly only really matters in relation to VirtualInventoryContainerSlots
    ///
    /// @return Amount inserted
    public static <SLOT extends Slot & ITransactionalSlot> int insertItem(Iterable<SLOT> slots, ItemResource itemType, int amount, TransactionContext transaction,
          @Nullable SelectedWindowData selectedWindow) {
        int inserted = 0;
        inserted += insertItem(slots, itemType, amount, transaction, true, selectedWindow);
        inserted += insertItem(slots, itemType, amount - inserted, transaction, false, selectedWindow);
        //Return how much was actually inserted
        return inserted;
    }

    /// @param slots             Slots to insert into
    /// @param itemType          Type of item to insert.
    /// @param amount            Amount of the item to insert.
    /// @param transaction       The transaction that this operation is part of.
    /// @param ignoreEmpty`true` to ignore/skip empty slots.
    /// @param selectedWindow    Selected window, or null if there is no window selected. This mostly only really matters in relation to VirtualInventoryContainerSlots
    ///
    /// @return Amount inserted
    ///
    /// @see mekanism.api.resource.IMekanismResourceHandler#insert(Resource, int, TransactionContext, AutomationType)
    public static <SLOT extends Slot & ITransactionalSlot> int insertItem(Iterable<SLOT> slots, ItemResource itemType, final int amount, TransactionContext transaction,
          boolean ignoreEmpty, @Nullable SelectedWindowData selectedWindow) {
        int inserted = 0;
        //Skip doing anything if the stack is already empty.
        // Makes it easier to chain calls, rather than having to check if the stack is empty after our previous call
        if (!itemType.isEmpty() && amount > 0) {
            for (SLOT slot : slots) {
                if (ignoreEmpty != slot.hasItem()) {
                    //Skip checking empty stacks if we want to ignore them, and skip non-empty stacks if we don't want ot ignore them
                    continue;
                } else if (!slot.exists(selectedWindow)) {
                    // or if the slot doesn't "exist" for the current window configuration
                    continue;
                }
                //Decrease amount to insert by how much we were able to insert
                inserted += slot.insert(itemType, amount - inserted, transaction);
                if (inserted == amount) {
                    break;
                }
            }
        }
        return inserted;
    }

    protected ItemStack transferSuccess(Slot currentSlot, Player player, int amountInserted) {
        //TODO - 26.2: This remove call has the potential to break the contract that mayPickup is called first?
        // Though all of our callers are from within #quickMoveStack which vanilla checks mayPickup before calling
        ItemStack newStack = currentSlot.remove(amountInserted);
        currentSlot.onTake(player, newStack);
        return newStack;
    }

    /// @apiNote Only call on client
    @Nullable
    public SelectedWindowData getSelectedWindow() {
        return selectedWindow;
    }

    /// @apiNote Only call on server
    @Nullable
    public SelectedWindowData getSelectedWindow(UUID player) {
        return selectedWindows.get(player);
    }

    /// @apiNote Only call on client
    public void setSelectedWindow(@Nullable SelectedWindowData selectedWindow) {
        if (!Objects.equals(this.selectedWindow, selectedWindow)) {
            this.selectedWindow = selectedWindow;
            PacketUtils.sendToServer(new PacketWindowSelect(this.selectedWindow));
        }
    }

    /// @apiNote Only call on server
    public void setSelectedWindow(UUID player, @Nullable SelectedWindowData selectedWindow) {
        if (selectedWindow == null) {
            clearSelectedWindow(player);
        } else {
            selectedWindows.put(player, selectedWindow);
        }
    }

    /// @apiNote Only call on server
    private void clearSelectedWindow(UUID player) {
        selectedWindows.remove(player);
    }

    //Start container sync management
    public void track(ISyncableData data) {
        trackedData.add(data);
    }

    @Override
    protected DataSlot addDataSlot(DataSlot referenceHolder) {
        //Override vanilla's int tracking so that if for some reason this method gets called for our container
        // it properly adds it to our tracking
        track(SyncableInt.create(referenceHolder::get, referenceHolder::set));
        return referenceHolder;
    }

    public void trackArray(boolean[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableBoolean.create(arrayIn, i));
        }
    }

    public void trackArray(byte[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableByte.create(arrayIn, i));
        }
    }

    public void trackArray(double[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableDouble.create(arrayIn, i));
        }
    }

    public void trackArray(float[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableFloat.create(arrayIn, i));
        }
    }

    public void trackArray(int[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableInt.create(arrayIn, i));
        }
    }

    public void trackArray(long[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableLong.create(arrayIn, i));
        }
    }

    public void trackArray(short[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableShort.create(arrayIn, i));
        }
    }

    public void trackArray(boolean[][] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            for (int j = 0; j < arrayIn[i].length; j++) {
                track(SyncableBoolean.create(arrayIn, i, j));
            }
        }
    }

    @Nullable
    private ISyncableData getTrackedData(short property) {
        //In theory the property indexing should always be valid but in case we get something that is out of bounds handle it gracefully
        if (property >= 0 && property < trackedData.size()) {
            return trackedData.get(property);
        }
        Mekanism.logger.warn("Received out of bounds window property {} for container {}. There are currently {} tracked properties.", property,
              Util.getRegisteredName(BuiltInRegistries.MENU, getType()), trackedData.size());
        return null;
    }

    public void handleWindowProperty(short property, boolean value) {
        ISyncableData data = getTrackedData(property);
        if (data instanceof SyncableBoolean syncable) {
            syncable.set(value);
        }
    }

    public void handleWindowProperty(short property, byte value) {
        ISyncableData data = getTrackedData(property);
        if (data instanceof SyncableByte syncable) {
            syncable.set(value);
        }
    }

    public void handleWindowProperty(short property, short value) {
        ISyncableData data = getTrackedData(property);
        if (data instanceof SyncableShort syncable) {
            syncable.set(value);
        }
    }

    public void handleWindowProperty(short property, int value) {
        ISyncableData data = getTrackedData(property);
        if (data instanceof SyncableInt syncable) {
            syncable.set(value);
        } else if (data instanceof SyncableEnum<?> syncable) {
            syncable.set(value);
        } else if (data instanceof SyncableItemStack syncable) {
            syncable.set(value);
        } else if (data instanceof SyncableRegistryEntry<?> syncable) {
            syncable.setFromId(value);
        }
    }

    public void handleWindowProperty(short property, long value) {
        ISyncableData data = getTrackedData(property);
        if (data instanceof SyncableLong syncable) {
            syncable.set(value);
        } else if (data instanceof SyncableLargeResourceStack<?> syncable) {
            syncable.set(value);
        }
    }

    public void handleWindowProperty(short property, float value) {
        ISyncableData data = getTrackedData(property);
        if (data instanceof SyncableFloat syncable) {
            syncable.set(value);
        }
    }

    public void handleWindowProperty(short property, double value) {
        ISyncableData data = getTrackedData(property);
        if (data instanceof SyncableDouble syncable) {
            syncable.set(value);
        }
    }

    public void handleWindowProperty(short property, ItemStack value) {
        ISyncableData data = getTrackedData(property);
        if (data instanceof SyncableItemStack syncable) {
            syncable.set(value);
        }
    }

    public <RESOURCE extends Resource> void handleWindowProperty(short property, RESOURCE value) {
        ISyncableData data = getTrackedData(property);
        if (data instanceof SyncableResource) {
            ((SyncableResource<RESOURCE>) data).set(value);
        }
    }

    public <RESOURCE extends Resource> void handleWindowProperty(short property, LargeResourceStack<RESOURCE> value) {
        ISyncableData data = getTrackedData(property);
        if (data instanceof SyncableLargeResourceStack) {
            ((SyncableLargeResourceStack<RESOURCE>) data).set(value);
        }
    }

    public void handleWindowProperty(short property, @Nullable BlockPos value) {
        ISyncableData data = getTrackedData(property);
        if (data instanceof SyncableBlockPos syncable) {
            syncable.set(value);
        }
    }

    public void handleWindowProperty(short property, byte[] value) {
        ISyncableData data = getTrackedData(property);
        switch (data) {
            case SyncableByteArray syncable -> syncable.set(value);
            case SyncableStreamCodec<?> syncable -> syncable.set(getLevel().registryAccess(), value);
            case null, default -> Mekanism.logger.error("Unknown byte value type: {}, please report", data == null ? null : data.getClass().getName());
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        //Note: We don't bother firing data changed listeners as we have no use for them,
        // and if someone wants to attach one to our containers they can explain what use
        // they need it for before we add a bunch of extra logic to handle them
        if (inv.player instanceof ServerPlayer player) {
            //Only check tracked data for changes if we actually have any listeners
            List<PropertyData> dirtyData = new ArrayList<>();
            RegistryAccess registryAccess = player.registryAccess();
            for (short i = 0; i < trackedData.size(); i++) {
                ISyncableData data = trackedData.get(i);
                DirtyType dirtyType = data.isDirty();
                if (dirtyType != DirtyType.CLEAN) {
                    dirtyData.add(data.getPropertyData(registryAccess, i, dirtyType));
                }
            }
            if (!dirtyData.isEmpty()) {
                PacketDistributor.sendToPlayer(player, new PacketUpdateContainer((short) containerId, dirtyData));
            }
        }
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        sendInitialDataToRemote(trackedData, ShortUnaryOperator.identity());
    }

    private void sendInitialDataToRemote(List<ISyncableData> syncableData, ShortUnaryOperator propertyIndex) {
        if (inv.player instanceof ServerPlayer player) {
            //Send all contents to the listener when it first gets added
            List<PropertyData> dirtyData = new ArrayList<>();
            RegistryAccess registryAccess = player.registryAccess();
            for (short i = 0; i < syncableData.size(); i++) {
                ISyncableData data = syncableData.get(i);
                //Query if the data is dirty or not so that we update our last known value to the initial values
                data.isDirty();
                //And then add the property data as if it was dirty regardless of if it was in case the value is the same as the default
                // as the client may not actually know about it
                dirtyData.add(data.getPropertyData(registryAccess, propertyIndex.apply(i), DirtyType.DIRTY));
            }
            if (!dirtyData.isEmpty()) {
                PacketDistributor.sendToPlayer(player, new PacketUpdateContainer((short) containerId, dirtyData));
            }
        }
    }
    //End container sync management

    @FunctionalInterface
    public interface ISpecificContainerTracker {

        List<ISyncableData> getSpecificSyncableData(Level level);
    }
}
