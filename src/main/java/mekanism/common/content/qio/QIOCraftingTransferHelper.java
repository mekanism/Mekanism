package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.bytes.Byte2IntArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2IntMap;
import it.unimi.dsi.fastutil.bytes.Byte2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.common.inventory.ISlotClickHandler.IScrollableSlot;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.InsertableSlot;
import mekanism.common.inventory.container.slot.MainInventorySlot;
import mekanism.common.lib.inventory.HashedItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QIOCraftingTransferHelper {

    /**
     * A map of {@link HashedItem}s to the item's sources for stored items in the frequency, the selected crafting grid, and the player's inventory. Any UUID distinct
     * items get merged into one as the client for checking amounts for JEI filling doesn't have access to the extra data anyway so makes do without it.
     *
     * @implNote We use raw hashed items as none of this stuff should or will be modified while doing these checks, so we may as well remove some unneeded copies.
     */
    public final Map<HashedItem, HashedItemSource> reverseLookup;
    private byte emptyInventorySlots;
    private boolean isValid;

    public QIOCraftingTransferHelper(Collection<? extends IScrollableSlot> cachedInventory, List<HotBarSlot> hotBarSlots, List<MainInventorySlot> mainInventorySlots,
          QIOCraftingWindow craftingWindow, Player player) {
        isValid = true;
        reverseLookup = new HashMap<>();
        for (IScrollableSlot source : cachedInventory) {
            //Skip any items that are only still in the cache due to sorting being paused, as we don't have any of it available for crafting
            if (source.count() > 0) {
                reverseLookup.computeIfAbsent(source.asRawHashedItem(), item -> new HashedItemSource()).addQIOSlot(source.itemUUID(), source.count());
            }
        }
        byte inventorySlotIndex = 0;
        for (; inventorySlotIndex < 9; inventorySlotIndex++) {
            IInventorySlot slot = craftingWindow.getInputSlot(inventorySlotIndex);
            if (!slot.isEmpty()) {
                //Note: This isn't a super accurate validation of if we can take the stack or not, given in theory we
                // always should be able to, but we have this check that mimics our implementation here just in case
                if (!slot.extractItem(1, Action.SIMULATE, AutomationType.MANUAL).isEmpty()) {
                    reverseLookup.computeIfAbsent(HashedItem.raw(slot.getStack()), item -> new HashedItemSource()).addSlot(inventorySlotIndex, slot.getCount());
                } else {
                    isValid = false;
                    //Can stop initializing things if we are not valid due to not being able to remove things from the input.
                    // Eventually, we may want to make this be able to special case and allow this to happen for if the items
                    // would end up in this slot anyway, but for now it doesn't really matter as this should never happen
                    return;
                }
            }
        }
        inventorySlotIndex = addSlotsToMap(player, hotBarSlots, inventorySlotIndex);
        addSlotsToMap(player, mainInventorySlots, inventorySlotIndex);
    }

    private byte addSlotsToMap(Player player, List<? extends Slot> slots, byte inventorySlotIndex) {
        for (Slot slot : slots) {
            if (slot.hasItem()) {
                if (slot.mayPickup(player)) {
                    ItemStack stack = slot.getItem();
                    reverseLookup.computeIfAbsent(HashedItem.raw(stack), item -> new HashedItemSource()).addSlot(inventorySlotIndex, stack.getCount());
                }
            } else {
                emptyInventorySlots++;
            }
            inventorySlotIndex++;
        }
        return inventorySlotIndex;
    }

    public boolean isInvalid() {
        return !isValid;
    }

    public byte getEmptyInventorySlots() {
        return emptyInventorySlots;
    }

    @Nullable
    public HashedItemSource getSource(@NotNull HashedItem item) {
        return reverseLookup.get(item);
    }

    public static class HashedItemSource {

        @Nullable
        private Object2LongMap<UUID> qioSources;
        @Nullable
        private Byte2IntMap slots;
        private long available;
        private long matches;

        public long getAvailable() {
            return available;
        }

        public void matchFound() {
            matches++;
        }

        public boolean hasMoreRemaining() {
            return available > matches;
        }

        private void addQIOSlot(UUID source, long stored) {
            if (qioSources == null) {
                qioSources = new Object2LongOpenHashMap<>();
            }
            qioSources.put(source, stored);
            available += stored;
        }

        private void addSlot(byte slot, int count) {
            if (slots == null) {
                slots = new Byte2IntArrayMap();
            }
            slots.put(slot, count);
            available += count;
        }

        public int getSlotRemaining(byte slot) {
            if (slots == null) {
                return 0;
            }
            return slots.getOrDefault(slot, 0);
        }

        public long getQIORemaining(UUID uuid) {
            if (qioSources == null) {
                return 0;
            }
            return qioSources.getOrDefault(uuid, 0);
        }

        public boolean hasQIOSources() {
            return qioSources != null;
        }

        public List<SingularHashedItemSource> use(int toUse) {
            if (toUse > available) {
                //If we don't have as many things available as we needed, then fail
                return Collections.emptyList();
            }
            matches--;
            List<SingularHashedItemSource> sources = new ArrayList<>();
            //Start by checking the crafting grid, hotbar, and main inventory for our items
            if (slots != null) {
                //Note: This checks it in the order crafting grid, hotbar, main inventory
                // because it is an array map, and we insert in the order that we expect
                for (ObjectIterator<Byte2IntMap.Entry> iterator = Byte2IntMaps.fastIterator(slots); iterator.hasNext(); ) {
                    Byte2IntMap.Entry entry = iterator.next();
                    int stored = entry.getIntValue();
                    byte slot = entry.getByteKey();
                    if (stored > toUse) {
                        //We have more stored than we need, use it and return
                        //Note: We need to use put, as entry#setValue is not supported in Byte2IntArrayMaps
                        slots.put(slot, stored - toUse);
                        available -= toUse;
                        sources.add(new SingularHashedItemSource(slot, toUse));
                        return sources;
                    }
                    //We have less stored than we need, use what we can and remove the source
                    available -= stored;
                    sources.add(new SingularHashedItemSource(slot, MathUtils.clampToInt(stored)));
                    iterator.remove();
                    if (stored == toUse) {
                        //If we had the exact amount we needed then return our sources
                        return sources;
                    }
                    //Otherwise, reduce how much we still need by how much we found
                    toUse -= stored;
                }
            }
            //If we didn't find an item to use for it, we look at the qio slots
            if (qioSources != null) {
                //TODO: This needs more thought at some point if we want to allow sending ones that have different UUIDs to the
                // server as we know they won't end up as stackable on the server, but if we are also sending some items, then
                // one of our matching QIO stacks might be able to stack with it and we won't have a good way of knowing which
                for (ObjectIterator<Object2LongMap.Entry<UUID>> iter = Object2LongMaps.fastIterator(qioSources); iter.hasNext(); ) {
                    Object2LongMap.Entry<UUID> entry = iter.next();
                    long stored = entry.getLongValue();
                    UUID key = entry.getKey();
                    if (stored > toUse) {
                        //We have more stored than we need, use it and return
                        entry.setValue(stored - toUse);
                        available -= toUse;
                        sources.add(new SingularHashedItemSource(key, toUse));
                        return sources;
                    }
                    //We have less stored than we need, use what we can and remove the source
                    available -= stored;
                    sources.add(new SingularHashedItemSource(key, MathUtils.clampToInt(stored)));
                    iter.remove();
                    if (stored == toUse) {
                        //If we had the exact amount we needed then return our sources
                        return sources;
                    }
                    //Otherwise, reduce how much we still need by how much we found
                    //Note: We know stored is less than toUse so fits in an int
                    toUse -= (int) stored;
                }
            }
            //Something went wrong, fail. We tried to use more than we have
            // Note: This also happens if we may have some but at a lesser amount as our precheck that calculates how much
            // each slot should get should be correct as it is based on the item type's available amount
            return Collections.emptyList();
        }
    }

    public static class SingularHashedItemSource {

        @Nullable
        private final UUID qioSource;
        private final byte slot;
        private int used;

        public SingularHashedItemSource(@NotNull UUID qioSource, int used) {
            this.qioSource = qioSource;
            this.slot = -1;
            this.used = used;
        }

        public SingularHashedItemSource(byte slot, int used) {
            this.qioSource = null;
            this.slot = slot;
            this.used = used;
        }

        public int getUsed() {
            return used;
        }

        public void setUsed(int used) {
            if (used < 0 || used > this.used) {
                throw new IllegalArgumentException("Used must be a lower amount than currently being used if getting updated.");
            }
            this.used = used;
        }

        public byte getSlot() {
            return slot;
        }

        @Nullable
        public UUID getQioSource() {
            return qioSource;
        }
    }

    /**
     * Class to help keep track of the inventory contents for simulating if there is room to shuffle the items around
     */
    public abstract static class BaseSimulatedInventory {

        private final ItemStack[] inventory;
        private final int[] stackSizes;
        private final int[] slotLimits;

        protected BaseSimulatedInventory(List<HotBarSlot> hotBarSlots, List<MainInventorySlot> mainInventorySlots) {
            int hotBarSize = hotBarSlots.size();
            int slots = hotBarSize + mainInventorySlots.size();
            inventory = new ItemStack[slots];
            stackSizes = new int[slots];
            slotLimits = new int[slots];
            InsertableSlot inventorySlot;
            for (int slot = 0; slot < slots; slot++) {
                if (slot < hotBarSize) {
                    inventorySlot = hotBarSlots.get(slot);
                } else {
                    inventorySlot = mainInventorySlots.get(slot - hotBarSize);
                }
                ItemStack stack = inventorySlot.getItem();
                int remaining = stack.isEmpty() ? 0 : getRemaining(slot, stack);
                if (remaining == 0) {
                    //If there is nothing "available" in the slot anymore that means the slot is "empty"
                    stack = ItemStack.EMPTY;
                }
                stackSizes[slot] = remaining;
                inventory[slot] = stack;
                //Note: For our slots the max stack size of the slot itself is always Item.ABSOLUTE_MAX_STACK_SIZE, but we look it up anyway just in case things change
                if (stack.isEmpty()) {
                    slotLimits[slot] = inventorySlot.getMaxStackSize(stack);
                } else {
                    slotLimits[slot] = Math.min(inventorySlot.getMaxStackSize(stack), stack.getMaxStackSize());
                }
            }
        }

        /**
         * @return The remaining number of items in the slot.
         */
        protected abstract int getRemaining(int slot, ItemStack currentStored);

        /**
         * Tries to shuffle an item into the inventory.
         *
         * @return The amount of the item that couldn't fit into the inventory.
         */
        public int shuffleItem(HashedItem type, int amount) {
            if (amount == 0) {
                return 0;
            }
            //Start by checking for slots it can stack with
            for (int slot = 0; slot < inventory.length; slot++) {
                int currentAmount = stackSizes[slot];
                int max = slotLimits[slot];
                //If the slot has any room left, and our stack is able to stack with it
                if (currentAmount < max && type.isSameItemSameComponents(inventory[slot])) {
                    int toPlace = Math.min(max - currentAmount, amount);
                    stackSizes[slot] = currentAmount + toPlace;
                    amount -= toPlace;
                    if (amount == 0) {
                        //We placed it all, return that we can shuffle
                        return 0;
                    }
                }
            }
            //If we have any remaining amount check the empty slots
            for (int slot = 0; slot < inventory.length; slot++) {
                if (inventory[slot].isEmpty()) {
                    int max = slotLimits[slot];
                    if (max > 0) {
                        //Note: Because player inventory slots don't have any restrictions on what can go in them,
                        // we are not bothering to check if we can actually place the stack in it, though if we keep
                        // track of the actual backing slot we could check it if needed, though that would have a chance
                        // of giving incorrect information anyway if it returns false for mayPlace based oen what was
                        // stored and is no longer stored
                        //Note: We also can use the raw backing stack in this array as we do not do any mutations,
                        // and this allows us to then avoid an extra copy call
                        inventory[slot] = type.getInternalStack();
                        slotLimits[slot] = max = Math.min(max, type.getMaxStackSize());
                        int toPlace = stackSizes[slot] = Math.min(amount, max);
                        amount -= toPlace;
                        if (amount == 0) {
                            //We placed it all, return that we can shuffle
                            return 0;
                        }
                    }
                }
            }
            return amount;
        }

        @Nullable
        public Object2IntMap<HashedItem> shuffleInputs(Object2IntMap<HashedItem> leftOverInput, boolean hasFrequency) {
            Object2IntMap<HashedItem> stillLeftOver = hasFrequency ? new Object2IntArrayMap<>(leftOverInput.size()) : Object2IntMaps.emptyMap();
            for (ObjectIterator<Object2IntMap.Entry<HashedItem>> iterator = Object2IntMaps.fastIterator(leftOverInput); iterator.hasNext(); ) {
                Object2IntMap.Entry<HashedItem> entry = iterator.next();
                // And try to shuffle the remaining contents into the simulation updating as we go
                int remaining = shuffleItem(entry.getKey(), entry.getIntValue());
                if (remaining > 0) {
                    if (!hasFrequency) {
                        //If we have remaining items and no frequency then we don't have room to shuffle
                        return null;
                    }
                    //Otherwise, if we do have a frequency add what we still have left over to a map to
                    // try and insert into the frequency
                    stillLeftOver.put(entry.getKey(), remaining);
                }
            }
            return stillLeftOver;
        }
    }
}