package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public interface IQIODriveItem {

    default boolean hasStoredItemMap(ItemStack stack) {
        return ItemDataUtils.hasData(stack, NBTConstants.QIO_ITEM_MAP, Tag.TAG_LONG_ARRAY);
    }

    default void loadItemMap(ItemStack stack, QIODriveData data) {
        if (hasStoredItemMap(stack)) {
            Object2LongMap<HashedItem> itemMap = data.getItemMap();
            long[] array = ItemDataUtils.getLongArray(stack, NBTConstants.QIO_ITEM_MAP);
            if (array.length % 3 == 0) {
                //Ensure we have valid data and not some unknown thing
                for (int i = 0; i < array.length; i++) {
                    UUID uuid = new UUID(array[i++], array[i++]);
                    HashedItem type = QIOGlobalItemLookup.INSTANCE.getTypeByUUID(uuid);
                    if (type != null) {
                        //Only add the item if the item type is known. If it can't that means the mod adding the item was probably removed
                        //TODO: Eventually we may want to keep the UUID so that if the mod gets added back it exists again?
                        itemMap.put(type, array[i]);
                    }
                }
            }
        }
    }

    /**
     * Writes the item map in a compact form to the stack. This compact form is a single long array tag that stores the data in partitions of three. The first partition
     * stores the most significant bits of the UUID that represents the stack, the second partition stores the least significant bits, and the final partition stores the
     * amount of the item that is stored in the drive. This maxes out at using {@code 3 * types per drive size * bytes per long + bytes per int} bytes to store just the
     * array of items to in the drive. For our max drive size this is equivalent to {@code 3 * 8,192 * 8 + 4 = 196,612} bytes.
     */
    default void writeItemMap(ItemStack stack, QIODriveData map) {
        int i = 0;
        Object2LongMap<HashedItem> itemMap = map.getItemMap();
        long[] serializedMap = new long[3 * itemMap.size()];
        for (Entry<HashedItem> entry : itemMap.object2LongEntrySet()) {
            UUID uuid = QIOGlobalItemLookup.INSTANCE.getOrTrackUUID(entry.getKey());
            serializedMap[i++] = uuid.getMostSignificantBits();
            serializedMap[i++] = uuid.getLeastSignificantBits();
            serializedMap[i++] = entry.getLongValue();
        }
        ItemDataUtils.setLongArrayOrRemove(stack, NBTConstants.QIO_ITEM_MAP, serializedMap);
    }

    long getCountCapacity(ItemStack stack);

    int getTypeCapacity(ItemStack stack);

    record DriveMetadata(long count, int types) {

        public void write(ItemStack stack) {
            ItemDataUtils.setLongOrRemove(stack, NBTConstants.QIO_META_COUNT, count);
            ItemDataUtils.setIntOrRemove(stack, NBTConstants.QIO_META_TYPES, types);
        }

        public static DriveMetadata load(ItemStack stack) {
            return new DriveMetadata(ItemDataUtils.getLong(stack, NBTConstants.QIO_META_COUNT), ItemDataUtils.getInt(stack, NBTConstants.QIO_META_TYPES));
        }
    }
}
