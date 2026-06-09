package mekanism.common.content.qio;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import mekanism.api.SerializationConstants;
import mekanism.common.Mekanism;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO - 1.19: Keep track of UUIDs synced to a given player, and clear when they disconnect. How quickly does the memory impact grow for the user to cache them??
// Maybe have the client send a thing like: No I can't cache them for if it only has a certain amount of ram?
// In theory that should drastically cut down the network packet sizes
public class QIOGlobalItemLookup {

    private static QIOGlobalItemLookupDataHandler INSTANCE = new QIOGlobalItemLookupDataHandler();

    public static QIOGlobalItemLookupDataHandler instance() {
        return INSTANCE;
    }

    private QIOGlobalItemLookup() {
    }

    /**
     * Note: This should only be called from the server side
     */
    public static void serverLoad(MinecraftServer server) {
        INSTANCE = server.getDataStorage().computeIfAbsent(TYPE);
    }

    public static void reset() {
        INSTANCE = new QIOGlobalItemLookupDataHandler();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(QIOGlobalItemLookup.class);
    static final Codec<QIOGlobalItemLookupDataHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Codec.unboundedMap(UUIDUtil.STRING_CODEC, ItemResource.CODEC)
                .promotePartial(err -> LOGGER.error("Some QIO item data failed to load, items may be missing: {}", err))
                .fieldOf(SerializationConstants.ITEMS)
                .forGetter(it -> it.itemCache),
          Codec.unboundedMap(UUIDUtil.STRING_CODEC, UUIDUtil.CODEC)
                .promotePartial(err -> LOGGER.warn("Some QIO alias data failed to load, unmigrated items may be missing: {}", err))
                .fieldOf(SerializationConstants.ALIASES)
                .forGetter(it -> it.mergedIds)
    ).apply(instance, QIOGlobalItemLookupDataHandler::new));
    static final SavedDataType<QIOGlobalItemLookupDataHandler> TYPE = new SavedDataType<>(
          Mekanism.rl("qio_type_cache"),
          QIOGlobalItemLookupDataHandler::new,
          CODEC
    );

    public static class QIOGlobalItemLookupDataHandler extends SavedData {

        //TODO - 1.19: Do we need to worry about synchronization for this map?
        //keep track of a UUID for each hashed item. Note every hashed item in this can be assumed to be serializable
        // we only don't store them as such for the generic so that we don't have to create extra objects for purposes
        // of getting the uuid for a given item type
        private final BiMap<UUID, ItemResource> itemCache = HashBiMap.create();
        /**
         * Map of "No longer valid" -> "New Id"
         */
        private final Map<UUID, UUID> mergedIds;

        private QIOGlobalItemLookupDataHandler() {
            mergedIds = Collections.emptyMap();
        }

        private QIOGlobalItemLookupDataHandler(Map<UUID, ItemResource> loadedData, Map<UUID, UUID> loadedAliases) {
            Map<UUID, UUID> aliases = new HashMap<>(loadedAliases);//make it mutable
            loadedData.forEach((uuid, item) -> {
                try {
                    itemCache.put(uuid, item);
                } catch (IllegalArgumentException e) {
                    UUID winningId = itemCache.inverse().get(item);
                    if (winningId == null) {
                        Mekanism.logger.error("Failed to resolve conflict for UUID ({}) for item {} with components: {}. Skipping", uuid, item.getItem(),
                              item.getComponentsPatch());
                    } else {
                        Mekanism.logger.warn("Adding alias between UUID ({}) to ({}) for item {} with components: {}", uuid, winningId, item.getItem(),
                              item.getComponentsPatch());
                        //Try to add it as an alias
                        aliases.put(uuid, winningId);
                    }
                }
            });
            if (aliases.isEmpty()) {
                mergedIds = Collections.emptyMap();
            } else {
                mergedIds = aliases;
            }
        }

        public boolean hasAliases() {
            return !mergedIds.isEmpty();
        }

        public UUID getWinningId(UUID uuid) {
            return mergedIds.getOrDefault(uuid, uuid);
        }

        @Nullable
        public UUID getUUIDForType(ItemResource item) {
            return itemCache.inverse().get(item);
        }

        /**
         * @apiNote Only call this with non-raw hashed items
         */
        public UUID getOrTrackUUID(ItemResource item) {
            BiMap<ItemResource, UUID> inverseCache = itemCache.inverse();
            UUID uuid = inverseCache.get(item);
            if (uuid == null) {
                //Calculate and return a new UUID and mark the save data as dirty
                uuid = UUID.randomUUID();
                itemCache.put(uuid, item);
                setDirty();
            }
            return uuid;
        }

        public ItemResource getTypeByUUID(@Nullable UUID uuid) {
            return uuid == null ? ItemResource.EMPTY : itemCache.getOrDefault(uuid, ItemResource.EMPTY);
        }

    }
}