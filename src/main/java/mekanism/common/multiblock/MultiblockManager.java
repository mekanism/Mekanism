package mekanism.common.multiblock;

import java.util.Map;
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

    public MultiblockCache<T> getNewCache() {
        return cacheSupplier.get();
    }

    @Nullable
    public static UUID getMultiblockID(TileEntityMultiblock<?> tile) {
        return tile.getMultiblock().inventoryID;
    }

    public static boolean areCompatible(TileEntity tile, TileEntity reference) {
        if (tile instanceof TileEntityMultiblock && reference instanceof TileEntityMultiblock) {
            return ((TileEntityMultiblock<?>) tile).getManager() == ((TileEntityMultiblock<?>) reference).getManager();
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
            TileEntity tile = MekanismUtils.getTileEntity(TileEntity.class, world, obj.getPos());
            if (tile instanceof IMultiblock) {
                ((IMultiblock<?>) tile).resetCache();
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

    public void updateCache(IMultiblock<T> tile, boolean force) {
        if (!inventories.containsKey(tile.getCacheID())) {
            tile.getCache().locations.add(Coord4D.get((TileEntity) tile));
            inventories.put(tile.getCacheID(), tile.getCache());
        } else {
            MultiblockCache<T> cache = inventories.get(tile.getCacheID());
            if (force) {
                tile.getCache().locations = cache.locations;
                inventories.put(tile.getCacheID(), tile.getCache());
                cache = tile.getCache();
            }
            cache.locations.add(Coord4D.get((TileEntity) tile));
        }
    }
}