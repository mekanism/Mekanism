package mekanism.common.multiblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MultiblockManager<T extends SynchronizedData<T>> {

    private static Set<MultiblockManager<?>> managers = new HashSet<>();

    public String name;

    /**
     * A map containing references to all multiblock inventory caches.
     */
    public Map<String, MultiblockCache<T>> inventories = new HashMap<>();

    public MultiblockManager(String s) {
        name = s;
        managers.add(this);
    }

    public static void tick(World world) {
        for (MultiblockManager<?> manager : managers) {
            manager.tickSelf(world);
        }
    }

    public static String getStructureId(TileEntityMultiblock<?> tile) {
        return tile.structure != null ? tile.getSynchronizedData().inventoryID : null;
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
    public MultiblockCache<T> pullInventory(World world, String id) {
        MultiblockCache<T> toReturn = inventories.get(id);
        for (Coord4D obj : inventories.get(id).locations) {
            TileEntityMultiblock<T> tileEntity = (TileEntityMultiblock<T>) MekanismUtils.getTileEntity(TileEntityMultiblock.class, world, obj.getPos());
            if (tileEntity != null) {
                tileEntity.cachedData = tileEntity.getNewCache();
                tileEntity.cachedID = null;
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
    public String getUniqueInventoryID() {
        return UUID.randomUUID().toString();
    }

    public void tickSelf(World world) {
        ArrayList<String> idsToKill = new ArrayList<>();
        Map<String, Set<Coord4D>> tilesToKill = new HashMap<>();
        for (Entry<String, MultiblockCache<T>> entry : inventories.entrySet()) {
            String inventoryID = entry.getKey();
            for (Coord4D obj : entry.getValue().locations) {
                if (obj.dimension.equals(world.getDimension().getType()) && world.isBlockLoaded(obj.getPos())) {
                    TileEntity tileEntity = MekanismUtils.getTileEntity(world, obj.getPos());
                    if (!(tileEntity instanceof TileEntityMultiblock) || ((TileEntityMultiblock<?>) tileEntity).getManager() != this ||
                        (getStructureId(((TileEntityMultiblock<?>) tileEntity)) != null && !Objects.equals(getStructureId(((TileEntityMultiblock<?>) tileEntity)), inventoryID))) {
                        if (!tilesToKill.containsKey(inventoryID)) {
                            tilesToKill.put(inventoryID, new HashSet<>());
                        }
                        tilesToKill.get(inventoryID).add(obj);
                    }
                }
            }
            if (entry.getValue().locations.isEmpty()) {
                idsToKill.add(inventoryID);
            }
        }
        for (Entry<String, Set<Coord4D>> entry : tilesToKill.entrySet()) {
            for (Coord4D obj : entry.getValue()) {
                inventories.get(entry.getKey()).locations.remove(obj);
            }
        }
        for (String inventoryID : idsToKill) {
            inventories.remove(inventoryID);
        }
    }

    public void updateCache(TileEntityMultiblock<T> tile) {
        if (!inventories.containsKey(tile.cachedID)) {
            tile.cachedData.locations.add(Coord4D.get(tile));
            inventories.put(tile.cachedID, tile.cachedData);
        } else {
            inventories.get(tile.cachedID).locations.add(Coord4D.get(tile));
        }
    }
}