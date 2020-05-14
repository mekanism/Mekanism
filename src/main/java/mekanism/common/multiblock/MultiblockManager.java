package mekanism.common.multiblock;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Coord4D;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MultiblockManager<T extends MultiblockData> {

    private static Set<MultiblockManager<?>> managers = new ObjectOpenHashSet<>();

    public String name;
    private Supplier<MultiblockCache<T>> cacheSupplier;

    /**
     * A map containing references to all multiblock inventory caches.
     */
    public Map<UUID, MultiblockCache<T>> inventories = new Object2ObjectOpenHashMap<>();

    public MultiblockManager(String name, Supplier<MultiblockCache<T>> cacheSupplier) {
        this.name = name;
        this.cacheSupplier = cacheSupplier;
        managers.add(this);
    }

    public static void tick(World world) {
        for (MultiblockManager<?> manager : managers) {
            manager.tickSelf(world);
        }
    }

    public MultiblockCache<T> getNewCache() {
        return cacheSupplier.get();
    }

    @Nullable
    public static UUID getMultiblockID(TileEntityMultiblock<?> tile) {
        return tile.getMultiblock().inventoryID;
    }

    public static boolean areCompatible(TileEntity tile1, TileEntity tile2, boolean markUpdated) {
        if (tile1 instanceof TileEntityMultiblock && tile2 instanceof TileEntityMultiblock) {
            boolean valid = ((TileEntityMultiblock<?>) tile1).getManager() == ((TileEntityMultiblock<?>) tile2).getManager();
            if (valid && markUpdated) {
                ((TileEntityMultiblock<?>) tile1).markUpdated();
                ((TileEntityMultiblock<?>) tile2).markUpdated();
            }
            return valid;
        }
        return false;
    }

    public static void reset() {
        for (MultiblockManager<?> manager : managers) {
            manager.inventories.clear();
        }
    }

    /**
     * Grabs an inventory from the world's caches, and removes all the world's references to it.
     *
     * @param world - world the cache is stored in
     * @param id    - inventory ID to pull
     *
     * @return correct multiblock inventory cache
     */
    public MultiblockCache<T> pullInventory(World world, UUID id) {
        MultiblockCache<T> toReturn = inventories.get(id);
        for (Coord4D obj : toReturn.locations) {
            TileEntityMultiblock<T> tile = MekanismUtils.getTileEntity(TileEntityMultiblock.class, world, obj.getPos());
            if (tile != null) {
                tile.resetCache();
            }
        }
        inventories.remove(id);
        return toReturn;
    }

    /**
     * Grabs a unique inventory ID for a multiblock.
     *
     * @return unique inventory ID
     */
    public UUID getUniqueInventoryID() {
        return UUID.randomUUID();
    }

    public void tickSelf(World world) {
        for (Iterator<Entry<UUID, MultiblockCache<T>>> entryIterator = inventories.entrySet().iterator(); entryIterator.hasNext();) {
            Entry<UUID, MultiblockCache<T>> entry = entryIterator.next();
            UUID inventoryID = entry.getKey();
            for (Iterator<Coord4D> coordIterator = entry.getValue().locations.iterator(); coordIterator.hasNext();) {
                Coord4D obj = coordIterator.next();
                if (obj.dimension.equals(world.getDimension().getType()) && world.isBlockPresent(obj.getPos())) {
                    TileEntity tile = MekanismUtils.getTileEntity(world, obj.getPos());
                    if (!(tile instanceof TileEntityMultiblock) || ((TileEntityMultiblock<?>) tile).getManager() != this ||
                        (getMultiblockID(((TileEntityMultiblock<?>) tile)) != null && !Objects.equals(getMultiblockID(((TileEntityMultiblock<?>) tile)), inventoryID))) {
                        coordIterator.remove();
                    }
                }
            }
            if (entry.getValue().locations.isEmpty()) {
                entryIterator.remove();
            }
        }
    }

    public void updateCache(TileEntityMultiblock<T> tile, boolean force) {
        if (!inventories.containsKey(tile.getCacheID())) {
            tile.getCache().locations.add(Coord4D.get(tile));
            inventories.put(tile.getCacheID(), tile.getCache());
        } else {
            MultiblockCache<T> cache = inventories.get(tile.getCacheID());
            if (force) {
                tile.getCache().locations = cache.locations;
                inventories.put(tile.getCacheID(), tile.getCache());
                cache = tile.getCache();
            }
            cache.locations.add(Coord4D.get(tile));
        }
    }
}