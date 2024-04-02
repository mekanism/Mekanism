package mekanism.common.attachments;

import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongSortedMap;
import it.unimi.dsi.fastutil.objects.Object2LongSortedMaps;
import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.content.qio.QIODriveData;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.lib.inventory.HashedItem;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class DriveMetadata implements INBTSerializable<CompoundTag> {

    public static DriveMetadata create() {
        return new DriveMetadata(0, 0, new Object2LongLinkedOpenHashMap<>());
    }

    //Sorted map so that the save order is consistent
    private final Object2LongSortedMap<UUID> namedItemMap;
    private final Object2LongSortedMap<UUID> namedItemMapView;
    private long count;
    private int types;

    private DriveMetadata(long count, int types, Object2LongSortedMap<UUID> namedItemMap) {
        this.count = count;
        this.types = types;
        this.namedItemMap = namedItemMap;
        this.namedItemMapView = Object2LongSortedMaps.unmodifiable(this.namedItemMap);
    }

    public long count() {
        return count;
    }

    public int types() {
        return types;
    }

    public Object2LongMap<UUID> namedItemMap() {
        return namedItemMapView;
    }

    public void update(QIODriveData drive) {
        this.count = drive.getTotalCount();
        this.types = drive.getTotalTypes();
    }

    public void update(Object2LongMap<UUID> itemMap, long count) {
        this.count = count;
        this.types = itemMap.size();
        this.namedItemMap.clear();
        for (Object2LongMap.Entry<UUID> entry : itemMap.object2LongEntrySet()) {
            this.namedItemMap.put(entry.getKey(), entry.getLongValue());
        }
    }

    public void copyItemMap(QIODriveData data) {
        this.namedItemMap.clear();
        for (Object2LongMap.Entry<HashedItem> entry : data.getItemMap().object2LongEntrySet()) {
            this.namedItemMap.put(QIOGlobalItemLookup.INSTANCE.getOrTrackUUID(entry.getKey()), entry.getLongValue());
        }
    }

    public void loadItemMap(QIODriveData data) {
        Object2LongMap<HashedItem> itemMap = data.getItemMap();
        for (Object2LongMap.Entry<UUID> entry : namedItemMap.object2LongEntrySet()) {
            HashedItem type = QIOGlobalItemLookup.INSTANCE.getTypeByUUID(entry.getKey());
            if (type != null) {
                //Only add the item if the item type is known. If it can't that means the mod adding the item was probably removed
                //TODO: Eventually we may want to keep the UUID so that if the mod gets added back it exists again?
                itemMap.put(type, entry.getLongValue());
            }
        }
    }

    public boolean isCompatible(DriveMetadata other) {
        return other == this || count == other.count && types == other.types && namedItemMap.equals(other.namedItemMap);
    }

    @Nullable
    @Override
    public CompoundTag serializeNBT() {
        if (count == 0 && types == 0 && namedItemMap.isEmpty()) {
            return null;
        }
        CompoundTag nbt = new CompoundTag();
        if (count > 0) {
            nbt.putLong(NBTConstants.QIO_META_COUNT, count);
        }
        if (types > 0) {
            nbt.putInt(NBTConstants.QIO_META_TYPES, types);
        }
        if (!namedItemMap.isEmpty()) {
            nbt.putLongArray(NBTConstants.QIO_ITEM_MAP, serializeItemMap());
        }
        return nbt;
    }

    /**
     * Writes the item map in a compact form to the stack. This compact form is a single long array tag that stores the data in partitions of three. The first partition
     * stores the most significant bits of the UUID that represents the stack, the second partition stores the least significant bits, and the final partition stores the
     * amount of the item that is stored in the drive. This maxes out at using {@code 3 * types per drive size * bytes per long + bytes per int} bytes to store just the
     * array of items to in the drive. For our max drive size this is equivalent to {@code 3 * 8,192 * 8 + 4 = 196,612} bytes.
     */
    private long[] serializeItemMap() {
        int i = 0;
        long[] serializedItemMap = new long[3 * namedItemMap.size()];
        for (Object2LongMap.Entry<UUID> entry : namedItemMap.object2LongEntrySet()) {
            UUID uuid = entry.getKey();
            serializedItemMap[i++] = uuid.getMostSignificantBits();
            serializedItemMap[i++] = uuid.getLeastSignificantBits();
            serializedItemMap[i++] = entry.getLongValue();
        }
        return serializedItemMap;
    }

    @Override
    public void deserializeNBT(@NotNull CompoundTag nbt) {
        this.count = nbt.getLong(NBTConstants.QIO_META_COUNT);
        this.types = nbt.getInt(NBTConstants.QIO_META_TYPES);
        readSerializedItemMap(nbt.getLongArray(NBTConstants.QIO_ITEM_MAP));
    }

    private void readSerializedItemMap(long[] serializedItemMap) {
        namedItemMap.clear();
        if (serializedItemMap.length > 0 && serializedItemMap.length % 3 == 0) {
            //Ensure we have valid data and not some value we don't know how to process
            for (int i = 0; i < serializedItemMap.length; i++) {
                namedItemMap.put(new UUID(serializedItemMap[i++], serializedItemMap[i++]), serializedItemMap[i]);
            }
        }
    }

    @Nullable
    public DriveMetadata copy(IAttachmentHolder holder) {
        if (count == 0 && types == 0 && namedItemMap.isEmpty()) {
            return null;
        }
        return new DriveMetadata(count, types, new Object2LongLinkedOpenHashMap<>(namedItemMap));
    }
}