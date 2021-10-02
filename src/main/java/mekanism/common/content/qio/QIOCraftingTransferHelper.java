package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.bytes.Byte2IntArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.MainInventorySlot;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.inventory.HashedItem.UUIDAwareHashedItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class QIOCraftingTransferHelper {

    /**
     * A map of {@link HashedItem}s to the item's sources for stored items in the frequency, the selected crafting grid, and the player's inventory. Any UUID distinct
     * items get merged into one as the client for checking amounts for JEI filling doesn't have access to the extra data anyways so makes do without it.
     *
     * @implNote We use raw hashed items as none of this stuff should or will be modified while doing these checks, so we may as well remove some unneeded copies.
     */
    public final Map<HashedItem, HashedItemSource> reverseLookup;

    public QIOCraftingTransferHelper(Object2LongMap<UUIDAwareHashedItem> cachedInventory, List<HotBarSlot> hotBarSlots, List<MainInventorySlot> mainInventorySlots,
          QIOCraftingWindow craftingWindow, PlayerEntity player) {
        reverseLookup = new HashMap<>();
        for (Object2LongMap.Entry<UUIDAwareHashedItem> entry : cachedInventory.object2LongEntrySet()) {
            UUIDAwareHashedItem source = entry.getKey();
            reverseLookup.computeIfAbsent(source.asRawHashedItem(), item -> new HashedItemSource()).addQIOSlot(source.getUUID(), entry.getLongValue());
        }
        byte inventorySlotIndex = 0;
        for (; inventorySlotIndex < 9; inventorySlotIndex++) {
            IInventorySlot slot = craftingWindow.getInputSlot(inventorySlotIndex);
            //Note: This isn't a super accurate validation of if we can take the stack or not, given in theory we
            // always should be able to, but we have this check that mimics our implementation here just in case
            if (!slot.isEmpty() && !slot.extractItem(1, Action.SIMULATE, AutomationType.MANUAL).isEmpty()) {
                reverseLookup.computeIfAbsent(HashedItem.raw(slot.getStack()), item -> new HashedItemSource()).addSlot(inventorySlotIndex, slot.getCount());
            }
        }
        inventorySlotIndex = addSlotsToMap(player, hotBarSlots, inventorySlotIndex);
        addSlotsToMap(player, mainInventorySlots, inventorySlotIndex);
    }

    private byte addSlotsToMap(PlayerEntity player, List<? extends Slot> slots, byte inventorySlotIndex) {
        for (Slot slot : slots) {
            if (slot.hasItem() && slot.mayPickup(player)) {
                ItemStack stack = slot.getItem();
                reverseLookup.computeIfAbsent(HashedItem.raw(stack), item -> new HashedItemSource()).addSlot(inventorySlotIndex, stack.getCount());
            }
            inventorySlotIndex++;
        }
        return inventorySlotIndex;
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
                for (ObjectIterator<Byte2IntMap.Entry> iter = slots.byte2IntEntrySet().iterator(); iter.hasNext(); ) {
                    Byte2IntMap.Entry entry = iter.next();
                    int stored = entry.getIntValue();
                    byte slot = entry.getByteKey();
                    if (stored > toUse) {
                        //We have more stored than we need, use it and return
                        //Note: We need to use put, as entry#setValue is not supported in fastutil maps
                        slots.put(slot, stored - toUse);
                        available -= toUse;
                        sources.add(new SingularHashedItemSource(slot, toUse));
                        return sources;
                    }
                    //We have less stored than we need, use what we can and remove the source
                    available -= stored;
                    sources.add(new SingularHashedItemSource(slot, MathUtils.clampToInt(stored)));
                    iter.remove();
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
                //TODO - 10.1: Do we even want to allow this to be like this as if the stacks have different UUIDs then they aren't going to end up being stackable
                for (ObjectIterator<Object2LongMap.Entry<UUID>> iter = qioSources.object2LongEntrySet().iterator(); iter.hasNext(); ) {
                    Object2LongMap.Entry<UUID> entry = iter.next();
                    long stored = entry.getLongValue();
                    UUID key = entry.getKey();
                    if (stored > toUse) {
                        //We have more stored than we need, use it and return
                        //Note: We need to use put, as entry#setValue is not supported in fastutil maps
                        qioSources.put(key, stored - toUse);
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
                    toUse -= stored;
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

        public SingularHashedItemSource(@Nonnull UUID qioSource, int used) {
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
}