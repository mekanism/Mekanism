package mekanism.common.lib.multiblock;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import mekanism.api.SerializationConstants;
import mekanism.common.Mekanism;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO - 26.1: rewrite multiblocks to have the MultiblockData ticked here, without a Cache middleman
// MultiblockData should possibly be renamed MultiblockEntity as it's like a BE, but multi
@EventBusSubscriber(modid = Mekanism.MODID)
public class MultiblockManager<T extends MultiblockData> implements ValueIOSerializable {

    //private static final Set<MultiblockManager<?>> managers = new HashSet<>();

    private final MultiblockType<T> multiblockType;

    /**
     * A map containing references to all multiblock inventory caches.
     */
    private final Map<UUID, MultiblockCache<T>> caches = new HashMap<>();

    private final Queue<T> multiblocksTicked = new ArrayDeque<>();

    public MultiblockManager(MultiblockType<T> multiblockType) {
        this.multiblockType = multiblockType;
        //managers.add(this);
    }

    public MultiblockType<T> getMultiblockType() {
        return multiblockType;
    }

    /**
     * Adds a cache as tracked and marks the manager as dirty.
     */
    public void trackCache(UUID id, MultiblockCache<T> cache) {
        caches.put(id, cache);
        //markDirty();
    }

    @Nullable
    public MultiblockCache<T> getCache(UUID multiblockID) {
        return caches.get(multiblockID);
    }

    @Override
    public String toString() {
        return multiblockType.id().toString();
    }

    public boolean isCompatible(BlockEntity tile) {
        if (tile instanceof IMultiblock<?> multiblock) {
            return multiblock.getMultiblockType() == this.multiblockType;
        }
        return false;
    }

    /**
     * Replaces and invalidates all the caches with the given ids with a new cache with the given id.
     */
    public void replaceCaches(Set<UUID> staleIds, UUID id, MultiblockCache<T> cache) {
        for (UUID staleId : staleIds) {
            caches.remove(staleId);
        }
        trackCache(id, cache);
    }

    public void handleDirtyMultiblock(T multiblock) {
        //Validate the multiblock is actually dirty and needs saving
        if (multiblock.isDirty()) {
            MultiblockCache<T> cache = getCache(multiblock.inventoryID);
            //Validate the multiblock's cache exists as if it doesn't we want to ignore it
            // in theory this method should only be called if the multiblock is valid and formed
            // but in case something goes wrong, don't let it
            if (cache != null) {
                cache.sync(multiblock);
                //If the multiblock is dirty mark the manager's data handler as dirty to ensure that we save
                //markDirty();
                // next we can reset the dirty state of the multiblock
                multiblock.resetDirty();
            }
        }
    }

    public void markTicked(T multiblock) {
        multiblocksTicked.add(multiblock);
    }

    /**
     * Grabs a unique inventory ID for a multiblock.
     *
     * @return unique inventory ID
     */
    public UUID getUniqueInventoryID() {
        return UUID.randomUUID();
    }

    //no longer relevant, attachement data is always saved
    //private void markDirty() {
    //}

    public static <T extends MultiblockData> MultiblockManager<T> get(Level level, MultiblockType<T> type) {
        return level.getData(type.attachment());
    }

    /**
     * Bit of a hack, really the multiblock system needs to not have a 'cache' middle-man.
     * <p></p>
     * Causes any multiblocks that became dirty after the master ticked to have their contents synced and thus saved (if one occurs after this tick)
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void endOfTickEvent(ServerTickEvent.Post event) {
        event.getServer().getAllLevels().forEach(level -> {
            for (MultiblockType<?> multiblockType : MekanismMultiblockRegistry.ALL_TYPES) {
                MultiblockManager<?> manager = level.getExistingDataOrNull(multiblockType.attachment());
                if (manager != null) {
                    manager.endOfTick();
                }
            }
        });
    }

    /**
     * syncs any multiblocks if they're dirty
     */
    private void endOfTick() {
        T item;
        while ((item = multiblocksTicked.poll()) != null) {
            handleDirtyMultiblock(item);
        }
    }


    @Override
    public void deserialize(@NotNull ValueInput input) {
        ValueInputList list = input.childrenListOrEmpty(SerializationConstants.CACHE);
        for (ValueInput child : list) {
            Optional<UUID> id = child.read(SerializationConstants.INVENTORY_ID, UUIDUtil.LENIENT_CODEC);
            if (id.isPresent()) {
                MultiblockCache<T> cachedData = multiblockType.createCache();
                cachedData.load(child);
                caches.put(id.get(), cachedData);
            }
        }
    }

    @Override
    public void serialize(@NotNull ValueOutput output) {
        ValueOutputList outList = output.childrenList(SerializationConstants.CACHE);
        for (Map.Entry<UUID, MultiblockCache<T>> entry : caches.entrySet()) {
            ValueOutput cacheOutput = outList.addChild();
            //Note: We can just store the inventory id in the same compound tag as the rest of the cache data
            // as none of the caches save anything to this tag
            cacheOutput.store(SerializationConstants.INVENTORY_ID, UUIDUtil.LENIENT_CODEC, entry.getKey());
            entry.getValue().save(cacheOutput);
        }
    }
}