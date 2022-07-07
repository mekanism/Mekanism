package mekanism.common.content.qio;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.File;
import java.util.Map;
import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.common.Mekanism;
import mekanism.common.lib.inventory.HashedItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.server.ServerLifecycleHooks;
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
        return itemCache.inverse().computeIfAbsent(serializable, s -> {
            //Calculate and return a new UUID and mark the save data as dirty
            markDirty();
            return UUID.randomUUID();
        });
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
            DimensionDataStorage savedData = ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage();
            dataHandler = savedData.computeIfAbsent(tag -> {
                QIOGlobalItemLookupDataHandler handler = new QIOGlobalItemLookupDataHandler();
                handler.load(tag);
                return handler;
            }, QIOGlobalItemLookupDataHandler::new, DATA_HANDLER_NAME);
        }
    }

    public void reset() {
        //Reset instance as we may have massively different sizes for different worlds,
        // so we want to free up as much memory as we can
        itemCache = HashBiMap.create();
        dataHandler = null;
    }

    private static class QIOGlobalItemLookupDataHandler extends SavedData {

        private void load(@NotNull CompoundTag nbt) {
            //TODO - 1.19: Do we want to clear existing elements
            for (String key : nbt.getAllKeys()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(key);
                } catch (IllegalArgumentException e) {
                    Mekanism.logger.warn("Invalid UUID ({}) stored in {} saved data.", key, DATA_HANDLER_NAME);
                    continue;
                }
                ItemStack stack = ItemStack.of(nbt.getCompound(key));
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
        public CompoundTag save(@NotNull CompoundTag nbt) {
            //TODO - 1.19: See if we can further improve this
            for (Map.Entry<UUID, HashedItem> entry : QIOGlobalItemLookup.INSTANCE.itemCache.entrySet()) {
                nbt.put(entry.getKey().toString(), ((SerializedHashedItem) entry.getValue()).getNbtRepresentation());
            }
            return nbt;
        }

        @Override
        public void save(@NotNull File file) {
            if (this.isDirty()) {
                //This is loosely based on Refined Storage's RSSavedData's system of saving first to a temp file
                // to reduce the odds of corruption if the user's computer crashes while the file is being written
                File tempFile = file.toPath().getParent().resolve(file.getName() + ".temp").toFile();
                super.save(tempFile);
                if (file.exists() && !file.delete()) {
                    Mekanism.logger.error("Failed to delete " + file.getName());
                }
                if (!tempFile.renameTo(file)) {
                    Mekanism.logger.error("Failed to rename " + tempFile.getName());
                }
            }
        }
    }

    private static class SerializedHashedItem extends HashedItem {

        private CompoundTag nbtRepresentation;

        @Nullable
        protected static SerializedHashedItem read(CompoundTag nbtRepresentation) {
            ItemStack stack = ItemStack.of(nbtRepresentation);
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

        public CompoundTag getNbtRepresentation() {
            if (nbtRepresentation == null) {
                nbtRepresentation = getStack().serializeNBT();
                //Override to ensure that it gets stored with a count of one in case it was raw
                // and that then when we read it we don't create it with extra size
                nbtRepresentation.putByte(NBTConstants.COUNT, (byte) 1);
            }
            return nbtRepresentation;
        }
    }
}