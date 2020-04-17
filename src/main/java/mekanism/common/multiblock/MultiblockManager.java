package mekanism.common.multiblock;

import java.util.ArrayList;
import java.util.List;
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
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MultiblockManager<T extends SynchronizedData<T>> {

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
    public static UUID getStructureId(TileEntityMultiblock<?> tile) {
        return tile.structure == null ? null : tile.getSynchronizedData().inventoryID;
    }

    public static boolean areEqual(TileEntity tile1, TileEntity tile2) {
        if (tile1 instanceof TileEntityMultiblock && tile2 instanceof TileEntityMultiblock) {
            return ((TileEntityMultiblock<?>) tile1).getManager() == ((TileEntityMultiblock<?>) tile2).getManager();
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
                tile.cachedData = getNewCache();
                tile.cachedID = null;
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
        List<UUID> idsToKill = new ArrayList<>();
        Map<UUID, Set<Coord4D>> tilesToKill = new Object2ObjectOpenHashMap<>();
        for (Entry<UUID, MultiblockCache<T>> entry : inventories.entrySet()) {
            UUID inventoryID = entry.getKey();
            for (Coord4D obj : entry.getValue().locations) {
                if (obj.dimension.equals(world.getDimension().getType()) && world.isBlockPresent(obj.getPos())) {
                    TileEntity tile = MekanismUtils.getTileEntity(world, obj.getPos());
                    if (!(tile instanceof TileEntityMultiblock) || ((TileEntityMultiblock<?>) tile).getManager() != this ||
                        (getStructureId(((TileEntityMultiblock<?>) tile)) != null && !Objects.equals(getStructureId(((TileEntityMultiblock<?>) tile)), inventoryID))) {
                        if (!tilesToKill.containsKey(inventoryID)) {
                            tilesToKill.put(inventoryID, new ObjectOpenHashSet<>());
                        }
                        tilesToKill.get(inventoryID).add(obj);
                    }
                }
            }
            if (entry.getValue().locations.isEmpty()) {
                idsToKill.add(inventoryID);
            }
        }
        for (Entry<UUID, Set<Coord4D>> entry : tilesToKill.entrySet()) {
            for (Coord4D obj : entry.getValue()) {
                inventories.get(entry.getKey()).locations.remove(obj);
            }
        }
        for (UUID inventoryID : idsToKill) {
            inventories.remove(inventoryID);
        }
    }

    public void updateCache(TileEntityMultiblock<T> tile, boolean force) {
        if (!inventories.containsKey(tile.cachedID)) {
            tile.cachedData.locations.add(Coord4D.get(tile));
            inventories.put(tile.cachedID, tile.cachedData);
        } else {
            MultiblockCache<T> cache = inventories.get(tile.cachedID);
            if (force) {
                tile.cachedData.locations = cache.locations;
                inventories.put(tile.cachedID, tile.cachedData);
                cache = tile.cachedData;
            }
            cache.locations.add(Coord4D.get(tile));
        }
    }
}