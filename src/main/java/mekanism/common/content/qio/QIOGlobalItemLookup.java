package mekanism.common.content.qio;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Map;
import java.util.UUID;
import mekanism.api.SerializationConstants;
import mekanism.common.Mekanism;
import mekanism.common.lib.MekanismSavedData;
import mekanism.common.lib.inventory.HashedItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO - 1.19: Keep track of UUIDs synced to a given player, and clear when they disconnect. How quickly does the memory impact grow for the user to cache them??
// Maybe have the client send a thing like: No I can't cache them for if it only has a certain amount of ram?
// In theory that should drastically cut down the network packet sizes
public class QIOGlobalItemLookup {

    public static final QIOGlobalItemLookup INSTANCE = new QIOGlobalItemLookup();
    private static final String DATA_HANDLER_NAME = "qio_type_cache";

    private QIOGlobalItemLookup() {
    }

    /**
     * Note: This can and will be null on the client side
     */
    @Nullable
    private QIOGlobalItemLookupDataHandler dataHandler;
    //TODO - 1.19: Do we need to worry about synchronization for this map?
    //keep track of a UUID for each hashed item. Note every hashed item in this can be assumed to be serializable
    // we only don't store them as such for the generic so that we don't have to create extra objects for purposes
    // of getting the uuid for a given item type
    private BiMap<UUID, HashedItem> itemCache = HashBiMap.create();

    @Nullable
    public UUID getUUIDForType(HashedItem item) {
        return itemCache.inverse().get(item);
    }

    /**
     * @apiNote Only call this with non-raw hashed items
     */
    public UUID getOrTrackUUID(HashedItem item) {
        //TODO - 1.19: Do we want this/other methods to error if we are called before the save data is loaded?
        //Note: Unlike for getUUIDForType we have to wrap the hashed item into a SerializedHashedItem here in case it isn't present
        // as we want to make sure only serialized hashed items are put into the map
        SerializedHashedItem serializable = new SerializedHashedItem(item);
        BiMap<HashedItem, UUID> inverseCache = itemCache.inverse();
        UUID uuid = inverseCache.get(serializable);
        if (uuid == null) {
            //Calculate and return a new UUID and mark the save data as dirty
            uuid = UUID.randomUUID();
            itemCache.put(uuid, serializable);
            markDirty();
        }
        return uuid;
    }

    @Nullable
    public HashedItem getTypeByUUID(@Nullable UUID uuid) {
        return uuid == null ? null : itemCache.get(uuid);
    }

    private void markDirty() {
        if (dataHandler != null) {
            dataHandler.setDirty();
        }
    }

    /**
     * Note: This should only be called from the server side
     */
    public void createOrLoad() {
        //TODO - 1.19: Figure out if we need to call this on tick if it hasn't loaded yet??? I don't think so but the other ones do so maybe?
        if (dataHandler == null) {
            //Always associate the world with the overworld as the items are the same regardless of dimension
            dataHandler = MekanismSavedData.createSavedData(QIOGlobalItemLookupDataHandler::new, DATA_HANDLER_NAME);
        }
    }

    public void reset() {
        //Reset instance as we may have massively different sizes for different worlds,
        // so we want to free up as much memory as we can
        itemCache = HashBiMap.create();
        dataHandler = null;
    }

    private static class QIOGlobalItemLookupDataHandler extends MekanismSavedData {

        @Override
        public void load(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
            //TODO - 1.19: Do we want to clear existing elements
            for (String key : nbt.getAllKeys()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(key);
                } catch (IllegalArgumentException e) {
                    Mekanism.logger.warn("Invalid UUID ({}) stored in {} saved data.", key, DATA_HANDLER_NAME);
                    continue;
                }
                ItemStack stack = ItemStack.parseOptional(provider, nbt.getCompound(key));
                //Only add the item if the item could be read. If it can't that means the mod adding the item was probably removed
                if (stack.isEmpty()) {
                    Mekanism.logger.debug("Failed to read corresponding item for UUID ({}) stored in {} saved data. "
                                          + "This most likely means the mod adding the item was removed.", uuid, DATA_HANDLER_NAME);
                } else {
                    //Note: We can't cache the nbt we read from as something might have changed related to caps just from loading it, and we
                    // want to make sure that we save it with the proper corresponding data
                    //TODO: Eventually we may want to keep the NBT so that if the mod gets added back it exists again
                    QIOGlobalItemLookup.INSTANCE.itemCache.put(uuid, new SerializedHashedItem(stack));
                }
            }
        }

        @NotNull
        @Override
        public CompoundTag save(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
            //TODO - 1.19: See if we can further improve this
            for (Map.Entry<UUID, HashedItem> entry : QIOGlobalItemLookup.INSTANCE.itemCache.entrySet()) {
                nbt.put(entry.getKey().toString(), ((SerializedHashedItem) entry.getValue()).getNbtRepresentation(provider));
            }
            return nbt;
        }
    }

    private static class SerializedHashedItem extends HashedItem {

        private Tag nbtRepresentation;

        @Nullable
        protected static SerializedHashedItem read(HolderLookup.Provider provider, CompoundTag nbtRepresentation) {
            ItemStack stack = ItemStack.parseOptional(provider, nbtRepresentation);
            //If the stack is empty something went wrong so return null, otherwise just create a new serializable hashed item
            // We can't cache the nbt we read from as something might have changed related to caps just from loading it, and we
            // want to make sure that we save it with the proper corresponding data
            return stack.isEmpty() ? null : new SerializedHashedItem(stack);
        }

        private SerializedHashedItem(ItemStack stack) {
            super(stack);
        }

        protected SerializedHashedItem(HashedItem other) {
            super(other);
        }

        public Tag getNbtRepresentation(@NotNull HolderLookup.Provider provider) {
            if (nbtRepresentation == null) {
                nbtRepresentation = internalToNBT(provider);
                //Override to ensure that it gets stored with a count of one in case it was raw
                // and that then when we read it we don't create it with extra size
                ((CompoundTag) nbtRepresentation).putByte(SerializationConstants.COUNT, (byte) 1);
            }
            return nbtRepresentation;
        }
    }
}