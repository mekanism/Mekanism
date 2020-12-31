package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.bytes.Byte2IntArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.MainInventorySlot;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.inventory.HashedItem.UUIDAwareHashedItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class QIOCraftingTransferHelper {

    /**
     * A map of {@link HashedItem}s to counts for stored items in the frequency, the selected crafting grid, and the player's inventory. Any UUID distinct items get
     * merged into one as the client for checking amounts for JEI filling doesn't have access to the extra data anyways so makes do without it.
     *
     * @implNote We use raw hashed items as none of this stuff should or will be modified while doing these checks so we may as well remove some unneeded copies.
     */
    private final Object2LongMap<HashedItem> availableItems;
    private final Map<HashedItem, HashedItemSource> reverseLookup;

    public QIOCraftingTransferHelper(Object2LongMap<UUIDAwareHashedItem> cachedInventory, List<HotBarSlot> hotBarSlots, List<MainInventorySlot> mainInventorySlots,
          QIOCraftingWindow craftingWindow, PlayerEntity player) {
        availableItems = new Object2LongOpenHashMap<>();
        reverseLookup = new HashMap<>();
        for (Object2LongMap.Entry<UUIDAwareHashedItem> entry : cachedInventory.object2LongEntrySet()) {
            UUIDAwareHashedItem source = entry.getKey();
            long stored = entry.getLongValue();
            HashedItem hashedItem = source.asRawHashedItem();
            availableItems.mergeLong(hashedItem, stored, Long::sum);
            reverseLookup.computeIfAbsent(hashedItem, item -> new HashedItemSource()).addQIOSlot(source.getUUID(), stored);
        }
        byte inventorySlotIndex = 0;
        for (; inventorySlotIndex < 9; inventorySlotIndex++) {
            IInventorySlot slot = craftingWindow.getInputSlot(inventorySlotIndex);
            //Note: This isn't a super accurate validation of if we can take the stack or not, given in theory we
            // always should be able to, but we have this check that mimics our implementation here just in case
            if (!slot.isEmpty() && !slot.extractItem(1, Action.SIMULATE, AutomationType.MANUAL).isEmpty()) {
                int stored = slot.getCount();
                HashedItem hashedItem = HashedItem.raw(slot.getStack());
                availableItems.mergeLong(hashedItem, stored, Long::sum);
                reverseLookup.computeIfAbsent(hashedItem, item -> new HashedItemSource()).addSlot(inventorySlotIndex, stored);
            }
        }
        inventorySlotIndex = addSlotsToMap(player, hotBarSlots, inventorySlotIndex);
        addSlotsToMap(player, mainInventorySlots, inventorySlotIndex);
    }

    private byte addSlotsToMap(PlayerEntity player, List<? extends Slot> slots, byte inventorySlotIndex) {
        for (Slot slot : slots) {
            if (slot.getHasStack() && slot.canTakeStack(player)) {
                ItemStack stack = slot.getStack();
                int stored = stack.getCount();
                HashedItem hashedItem = HashedItem.raw(stack);
                availableItems.mergeLong(hashedItem, stored, Long::sum);
                reverseLookup.computeIfAbsent(hashedItem, item -> new HashedItemSource()).addSlot(inventorySlotIndex, stored);
            }
            inventorySlotIndex++;
        }
        return inventorySlotIndex;
    }

    public Object2LongMap<HashedItem> getAvailableItems() {
        return availableItems;
    }

    @Nullable
    public HashedItemSource getSource(@Nonnull HashedItem item) {
        return reverseLookup.get(item);
    }

    public static class HashedItemSource {

        @Nullable
        private Object2LongMap<UUID> qioSources;
        @Nullable
        private Byte2IntMap slots;

        private void addQIOSlot(UUID source, long stored) {
            if (qioSources == null) {
                qioSources = new Object2LongOpenHashMap<>();
            }
            qioSources.put(source, stored);
        }

        private void addSlot(byte slot, int count) {
            if (slots == null) {
                slots = new Byte2IntArrayMap();
            }
            slots.put(slot, count);
        }

        @Nullable
        public SingularHashedItemSource use() {
            //Start by checking the crafting grid, hotbar, and main inventory for our items
            if (slots != null) {
                //Note: This checks it in the order crafting grid, hotbar, main inventory
                // because it is an array map and we insert in the order that we expect
                ObjectIterator<Byte2IntMap.Entry> iter = slots.byte2IntEntrySet().iterator();
                if (iter.hasNext()) {
                    Byte2IntMap.Entry entry = iter.next();
                    int stored = entry.getIntValue();
                    //Get the key before we potentially remove it as after removing it fast util
                    // makes it so that the entry is no longer valid
                    byte slot = entry.getByteKey();
                    if (stored == 1) {
                        iter.remove();
                        if (slots.isEmpty()) {
                            slots = null;
                        }
                    } else {
                        //Note: entry.setValue is not supported in array maps
                        slots.put(slot, stored - 1);
                    }
                    return new SingularHashedItemSource(slot);
                }
            }
            //If we didn't find an item to use for it, we look at the qio slots
            if (qioSources != null) {
                ObjectIterator<Object2LongMap.Entry<UUID>> iter = qioSources.object2LongEntrySet().iterator();
                if (iter.hasNext()) {
                    Object2LongMap.Entry<UUID> entry = iter.next();
                    long stored = entry.getLongValue();
                    //Get the key before we potentially remove it as after removing it fast util
                    // makes it so that the entry is no longer valid
                    UUID key = entry.getKey();
                    if (stored == 1) {
                        iter.remove();
                        if (qioSources.isEmpty()) {
                            qioSources = null;
                        }
                    } else {
                        entry.setValue(stored - 1);
                    }
                    return new SingularHashedItemSource(key);
                }
            }
            //Something went wrong, fallback to null. We tried to use more than we have
            return null;
        }
    }

    public static class SingularHashedItemSource {

        @Nullable
        private final UUID qioSource;
        private final byte slot;

        public SingularHashedItemSource(@Nonnull UUID qioSource) {
            this.qioSource = qioSource;
            this.slot = -1;
        }

        public SingularHashedItemSource(byte slot) {
            this.qioSource = null;
            this.slot = slot;
        }

        public byte getSlot() {
            return slot;
        }

        @Nullable
        public UUID getQioSource() {
            return qioSource;
        }
    }
}