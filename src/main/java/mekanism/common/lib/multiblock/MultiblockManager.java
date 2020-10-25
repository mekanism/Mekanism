package mekanism.common.lib.multiblock;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.WorldUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MultiblockManager<T extends MultiblockData> {

    private static final Set<MultiblockManager<?>> managers = new ObjectOpenHashSet<>();

    private final String name;

    private final Supplier<MultiblockCache<T>> cacheSupplier;
    private final Supplier<IStructureValidator<T>> validatorSupplier;

    /**
     * A map containing references to all multiblock inventory caches.
     */
    public final Map<UUID, CacheWrapper> inventories = new Object2ObjectOpenHashMap<>();

    public MultiblockManager(String name, Supplier<MultiblockCache<T>> cacheSupplier, Supplier<IStructureValidator<T>> validatorSupplier) {
        this.name = name;
        this.cacheSupplier = cacheSupplier;
        this.validatorSupplier = validatorSupplier;
        managers.add(this);
    }

    public MultiblockCache<T> createCache() {
        return cacheSupplier.get();
    }

    public IStructureValidator<T> createValidator() {
        return validatorSupplier.get();
    }

    public String getName() {
        return name;
    }

    @Nullable
    public static UUID getMultiblockID(TileEntityMultiblock<?> tile) {
        return tile.getMultiblock().inventoryID;
    }

    public boolean isCompatible(TileEntity tile) {
        if (tile instanceof IMultiblock) {
            return ((IMultiblock<?>) tile).getManager() == this;
        }
        return false;
    }

    public static void reset() {
        for (MultiblockManager<?> manager : managers) {
            manager.inventories.clear();
        }
    }

    public void invalidate(IMultiblock<?> multiblock) {
        CacheWrapper cache = inventories.get(multiblock.getCacheID());
        if (cache != null) {
            cache.locations.remove(Coord4D.get((TileEntity) multiblock));
            if (cache.locations.isEmpty()) {
                inventories.remove(multiblock.getCacheID());
            }
        }
    }

    /**
     * Grabs an inventory from the world's caches, and removes all the world's references to it. NOTE: this is not guaranteed to remove all references if somehow blocks
     * with this inventory ID exist in unloaded chunks when the inventory is pulled. We should consider whether we should implement a way to mitigate this.
     *
     * @param world - world the cache is stored in
     * @param id    - inventory ID to pull
     *
     * @return correct multiblock inventory cache
     */
    public MultiblockCache<T> pullInventory(World world, UUID id) {
        CacheWrapper toReturn = inventories.get(id);
        for (Coord4D obj : toReturn.locations) {
            TileEntity tile = WorldUtils.getTileEntity(TileEntity.class, world, obj.getPos());
            if (tile instanceof IMultiblock) {
                ((IMultiblock<?>) tile).resetCache();
            }
        }
        inventories.remove(id);
        return toReturn.getCache();
    }

    /**
     * Grabs a unique inventory ID for a multiblock.
     *
     * @return unique inventory ID
     */
    public UUID getUniqueInventoryID() {
        return UUID.randomUUID();
    }

    public void updateCache(IMultiblock<T> tile, T multiblock) {
        inventories.computeIfAbsent(tile.getCacheID(), id -> new CacheWrapper()).update(tile, multiblock);
    }

    private class CacheWrapper {

        private MultiblockCache<T> cache;
        private final Set<Coord4D> locations = new ObjectOpenHashSet<>();

        public MultiblockCache<T> getCache() {
            return cache;
        }

        public void update(IMultiblock<T> tile, T multiblock) {
            locations.add(Coord4D.get((TileEntity) tile));
            if (multiblock.isFormed()) {
                if (tile.isMaster()) {
                    // create a new cache for the tile if it needs one
                    if (!tile.hasCache()) {
                        tile.setCache(createCache());
                    }
                    // if this is the master tile, sync the cache with the multiblock and then update our reference
                    tile.getCache().sync(multiblock);
                    cache = tile.getCache();
                }
            } else {
                if (tile.hasCache()) {
                    // if the tile doesn't have a formed multiblock but has a cache, update our reference
                    cache = tile.getCache();
                } else if (cache != null) {
                    // if the tile doesn't have a cache but we do, update the tile's reference
                    tile.setCache(cache);
                }
            }
        }
    }
}