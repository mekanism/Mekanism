package mekanism.common.attachments.qio;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongSortedMap;
import it.unimi.dsi.fastutil.objects.Object2LongSortedMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.UUID;
import java.util.function.LongBinaryOperator;
import java.util.stream.LongStream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.content.qio.QIODriveData;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.lib.inventory.HashedItem;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault//Sorted map so that the save order is consistent
public record DriveContents(Object2LongSortedMap<UUID> namedItemMap) {

    public static final DriveContents EMPTY = new DriveContents(Object2LongSortedMaps.emptyMap());
    private static final LongBinaryOperator SUM = Long::sum;

    public static final Codec<DriveContents> CODEC = Codec.LONG_STREAM.xmap(
          stream -> readSerializedItemMap(stream.toArray()),
          contents -> LongStream.of(contents.serializeItemMap())
    );
    public static final StreamCodec<ByteBuf, DriveContents> STREAM_CODEC = ByteBufCodecs.<ByteBuf, UUID, Long, Object2LongSortedMap<UUID>>map(
                Object2LongLinkedOpenHashMap::new, UUIDUtil.STREAM_CODEC, ByteBufCodecs.VAR_LONG)
          .map(DriveContents::new, DriveContents::namedItemMap);

    public DriveContents {
        //Make the map unmodifiable to ensure we don't accidentally mutate it
        namedItemMap = Object2LongSortedMaps.unmodifiable(namedItemMap);
    }

    public static DriveContents create(QIODriveData data) {
        Object2LongMap<HashedItem> itemMap = data.getItemMap();
        if (itemMap.isEmpty()) {
            return EMPTY;
        }
        Object2LongSortedMap<UUID> namedItemMap = new Object2LongLinkedOpenHashMap<>(itemMap.size());
        for (ObjectIterator<Object2LongMap.Entry<HashedItem>> iterator = Object2LongMaps.fastIterator(data.getItemMap()); iterator.hasNext(); ) {
            Object2LongMap.Entry<HashedItem> entry = iterator.next();
            namedItemMap.put(QIOGlobalItemLookup.INSTANCE.getOrTrackUUID(entry.getKey()), entry.getLongValue());
        }
        return new DriveContents(namedItemMap);
    }

    public void loadItemMap(QIODriveData data) {
        Object2LongMap<HashedItem> itemMap = data.getItemMap();
        for (ObjectIterator<Object2LongMap.Entry<UUID>> iterator = Object2LongMaps.fastIterator(namedItemMap); iterator.hasNext(); ) {
            Object2LongMap.Entry<UUID> entry = iterator.next();
            HashedItem type = QIOGlobalItemLookup.INSTANCE.getTypeByUUID(entry.getKey());
            if (type != null) {
                //Only add the item if the item type is known. If it can't that means the mod adding the item was probably removed
                //TODO: Eventually we may want to keep the UUID so that if the mod gets added back it exists again?
                itemMap.put(type, entry.getLongValue());
            }
        }
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
        for (ObjectIterator<Object2LongMap.Entry<UUID>> iterator = Object2LongMaps.fastIterator(namedItemMap); iterator.hasNext(); ) {
            Object2LongMap.Entry<UUID> entry = iterator.next();
            UUID uuid = entry.getKey();
            serializedItemMap[i++] = uuid.getMostSignificantBits();
            serializedItemMap[i++] = uuid.getLeastSignificantBits();
            serializedItemMap[i++] = entry.getLongValue();
        }
        return serializedItemMap;
    }

    private static DriveContents readSerializedItemMap(long[] serializedItemMap) {
        if (serializedItemMap.length > 0 && serializedItemMap.length % 3 == 0) {
            //Ensure we have valid data and not some value we don't know how to process
            Object2LongSortedMap<UUID> namedItemMap = new Object2LongLinkedOpenHashMap<>();
            boolean hasAliases = QIOGlobalItemLookup.INSTANCE.hasAliases();
            for (int i = 0; i < serializedItemMap.length; i++) {
                UUID savedUUID = new UUID(serializedItemMap[i++], serializedItemMap[i++]);
                long storedCount = serializedItemMap[i];
                if (!hasAliases) {
                    //If we have no aliases stored in the lookup, we can just short circuit to directly adding the id
                    namedItemMap.put(savedUUID, storedCount);
                } else {
                    //Note: getWinningId, will return the passed in id if there isn't an id to remap it to
                    UUID winningId = QIOGlobalItemLookup.INSTANCE.getWinningId(savedUUID);
                    //We merge regardless of if our item had an alias or not, so that we don't fail on the
                    // case where we load the alias version first, and then the one that is new might override it
                    namedItemMap.mergeLong(winningId, storedCount, SUM);
                }
            }
            return new DriveContents(namedItemMap);
        }
        return EMPTY;
    }
}