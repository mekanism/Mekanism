package mekanism.common.attachments;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Function;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.content.qio.QIODriveData;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class DriveMetadata implements INBTSerializable<CompoundTag> {

    private static final long[] EMPTY_ITEM_MAP = new long[0];

    //Note: We only bother storing the partially serialized item map as we don't have to read it often and as we can't make custom
    // attachment copiers yet, there is not much sense in fully serializing and deserializing it given how little we read it and
    // how often MC has stacks get copied around
    //TODO: If/when attachments support custom copying we may want to move this to either a Map<UUID, long> or Map<HashedItem, long>
    // given we already have the hashed items in memory so it would just be a pointer (though that may not actually be the case on the client)
    private long[] serializedItemMap = EMPTY_ITEM_MAP;
    private long count;
    private int types;

    public DriveMetadata(IAttachmentHolder attachmentHolder) {
        loadLegacyData(attachmentHolder);
    }

    @Deprecated//TODO - 1.21: Remove this legacy way of loading data
    private void loadLegacyData(IAttachmentHolder attachmentHolder) {
        if (attachmentHolder instanceof ItemStack stack && !stack.isEmpty()) {
            if (ItemDataUtils.hasData(stack, NBTConstants.QIO_META_COUNT, Tag.TAG_LONG)) {
                CompoundTag dataMap = ItemDataUtils.getDataMapIfPresent(stack);
                if (dataMap != null) {
                    this.count = dataMap.getLong(NBTConstants.QIO_META_COUNT);
                }
                ItemDataUtils.removeData(stack, NBTConstants.QIO_META_COUNT);
            }
            if (ItemDataUtils.hasData(stack, NBTConstants.QIO_META_TYPES, Tag.TAG_INT)) {
                this.types = ItemDataUtils.getInt(stack, NBTConstants.QIO_META_TYPES);
                ItemDataUtils.removeData(stack, NBTConstants.QIO_META_TYPES);
            }
            if (ItemDataUtils.hasData(stack, NBTConstants.QIO_ITEM_MAP, Tag.TAG_LONG_ARRAY)) {
                CompoundTag dataMap = ItemDataUtils.getDataMapIfPresent(stack);
                long[] itemMap = dataMap == null ? EMPTY_ITEM_MAP : dataMap.getLongArray(NBTConstants.QIO_ITEM_MAP);
                if (itemMap.length > 0 && itemMap.length % 3 == 0) {
                    //Ensure we have valid data and not some value we don't know how to process
                    serializedItemMap = itemMap;
                }
                ItemDataUtils.removeData(stack, NBTConstants.QIO_ITEM_MAP);
            }
        }
    }

    public long count() {
        return count;
    }

    public int types() {
        return types;
    }

    public void update(QIODriveData drive) {
        this.count = drive.getTotalCount();
        this.types = drive.getTotalTypes();
    }

    public void update(Object2LongMap<UUID> itemMap, long count) {
        this.count = count;
        this.types = itemMap.size();
        copyItemMap(itemMap, Function.identity());
    }

    public void copyItemMap(QIODriveData data) {
        copyItemMap(data.getItemMap(), QIOGlobalItemLookup.INSTANCE::getOrTrackUUID);
    }

    /**
     * Writes the item map in a compact form to the stack. This compact form is a single long array tag that stores the data in partitions of three. The first partition
     * stores the most significant bits of the UUID that represents the stack, the second partition stores the least significant bits, and the final partition stores the
     * amount of the item that is stored in the drive. This maxes out at using {@code 3 * types per drive size * bytes per long + bytes per int} bytes to store just the
     * array of items to in the drive. For our max drive size this is equivalent to {@code 3 * 8,192 * 8 + 4 = 196,612} bytes.
     */
    private <KEY> void copyItemMap(Object2LongMap<KEY> itemMap, Function<KEY, UUID> idExtractor) {
        if (itemMap.isEmpty()) {
            serializedItemMap = EMPTY_ITEM_MAP;
            return;
        }
        serializedItemMap = new long[3 * itemMap.size()];
        int i = 0;
        for (Entry<KEY> entry : itemMap.object2LongEntrySet()) {
            UUID uuid = idExtractor.apply(entry.getKey());
            serializedItemMap[i++] = uuid.getMostSignificantBits();
            serializedItemMap[i++] = uuid.getLeastSignificantBits();
            serializedItemMap[i++] = entry.getLongValue();
        }
    }

    public void loadItemMap(QIODriveData data) {
        Object2LongMap<HashedItem> itemMap = data.getItemMap();
        for (int i = 0; i < serializedItemMap.length; i++) {
            UUID uuid = new UUID(serializedItemMap[i++], serializedItemMap[i++]);
            HashedItem type = QIOGlobalItemLookup.INSTANCE.getTypeByUUID(uuid);
            if (type != null) {
                //Only add the item if the item type is known. If it can't that means the mod adding the item was probably removed
                //TODO: Eventually we may want to keep the UUID so that if the mod gets added back it exists again?
                itemMap.put(type, serializedItemMap[i]);
            }
        }
    }

    public Object2LongMap<UUID> uuidBasedMap() {
        Object2LongMap<UUID> itemMap = new Object2LongOpenHashMap<>(types);
        for (int i = 0; i < serializedItemMap.length; i++) {
            UUID uuid = new UUID(serializedItemMap[i++], serializedItemMap[i++]);
            itemMap.put(uuid, serializedItemMap[i]);
        }
        return itemMap;
    }

    public boolean isCompatible(DriveMetadata other) {
        return other == this || count == other.count && types == other.types && Arrays.equals(serializedItemMap, other.serializedItemMap);
    }

    @Nullable
    @Override
    public CompoundTag serializeNBT() {
        if (count == 0 && types == 0 && serializedItemMap.length == 0) {
            return null;
        }
        CompoundTag nbt = new CompoundTag();
        if (count > 0) {
            nbt.putLong(NBTConstants.QIO_META_COUNT, count);
        }
        if (types > 0) {
            nbt.putInt(NBTConstants.QIO_META_TYPES, types);
        }
        if (serializedItemMap.length > 0) {
            nbt.putLongArray(NBTConstants.QIO_ITEM_MAP, serializedItemMap);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(@NotNull CompoundTag nbt) {
        this.count = nbt.getLong(NBTConstants.QIO_META_COUNT);
        this.types = nbt.getInt(NBTConstants.QIO_META_TYPES);
        long[] itemMap = nbt.getLongArray(NBTConstants.QIO_ITEM_MAP);
        if (itemMap.length > 0 && itemMap.length % 3 == 0) {
            //Ensure we have valid data and not some value we don't know how to process
            serializedItemMap = itemMap;
        } else {
            serializedItemMap = EMPTY_ITEM_MAP;
        }
    }
}