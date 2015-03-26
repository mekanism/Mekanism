package mekanism.common.multiblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.tile.TileEntityMultiblock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MultiblockManager<T extends SynchronizedData<T>>
{
	private static Set<MultiblockManager> managers = new HashSet<MultiblockManager>();
	
	public String name;
	
	/** A map containing references to all multiblock inventory caches. */
	public Map<Integer, MultiblockCache<T>> inventories = new HashMap<Integer, MultiblockCache<T>>();
	
	public MultiblockManager(String s)
	{
		name = s;
		managers.add(this);
	}
	
	/**
	 * Grabs an inventory from the world's caches, and removes all the world's references to it.
	 * @param world - world the cache is stored in
	 * @param id - inventory ID to pull
	 * @return correct multiblock inventory cache
	 */
	public MultiblockCache<T> pullInventory(World world, int id)
	{
		MultiblockCache<T> toReturn = inventories.get(id);
		
		for(Coord4D obj : inventories.get(id).locations)
		{
			TileEntityMultiblock<T> tileEntity = (TileEntityMultiblock<T>)obj.getTileEntity(world);

			if(tileEntity != null)
			{
				tileEntity.cachedData = tileEntity.getNewCache();
				tileEntity.cachedID = -1;
			}
		}
		
		inventories.remove(id);

		return toReturn;
	}

	/**
	 * Grabs a unique inventory ID for a multiblock.
	 * @return unique inventory ID
	 */
	public int getUniqueInventoryID()
	{
		int id = 0;

		while(true)
		{
			for(Integer i : inventories.keySet())
			{
				if(id == i)
				{
					id++;
					continue;
				}
			}

			return id;
		}
	}
	
	public static void tick(World world)
	{
		for(MultiblockManager manager : managers)
		{
			manager.tickSelf(world);
		}
	}

	public void tickSelf(World world)
	{
		ArrayList<Integer> idsToKill = new ArrayList<Integer>();
		HashMap<Integer, HashSet<Coord4D>> tilesToKill = new HashMap<Integer, HashSet<Coord4D>>();

		for(Map.Entry<Integer, MultiblockCache<T>> entry : inventories.entrySet())
		{
			int inventoryID = entry.getKey();

			for(Coord4D obj : entry.getValue().locations)
			{
				if(obj.dimensionId == world.provider.dimensionId && obj.exists(world))
				{
					TileEntity tileEntity = obj.getTileEntity(world);

					if(!(tileEntity instanceof TileEntityMultiblock) || ((TileEntityMultiblock)tileEntity).getManager() != this || (getStructureId(((TileEntityMultiblock<?>)tileEntity)) != -1 && getStructureId(((TileEntityMultiblock)tileEntity)) != inventoryID))
					{
						if(!tilesToKill.containsKey(inventoryID))
						{
							tilesToKill.put(inventoryID, new HashSet<Coord4D>());
						}

						tilesToKill.get(inventoryID).add(obj);
					}
				}
			}

			if(entry.getValue().locations.isEmpty())
			{
				idsToKill.add(inventoryID);
			}
		}

		for(Map.Entry<Integer, HashSet<Coord4D>> entry : tilesToKill.entrySet())
		{
			for(Coord4D obj : entry.getValue())
			{
				inventories.get(entry.getKey()).locations.remove(obj);
			}
		}

		for(int inventoryID : idsToKill)
		{
			inventories.remove(inventoryID);
		}
	}
	
	public static int getStructureId(TileEntityMultiblock<?> tile)
	{
		return tile.structure != null ? tile.getSynchronizedData().inventoryID : -1;
	}
	
	public static boolean areEqual(TileEntity tile1, TileEntity tile2)
	{
		if(!(tile1 instanceof TileEntityMultiblock) || !(tile2 instanceof TileEntityMultiblock))
		{
			return false;
		}
		
		return ((TileEntityMultiblock)tile1).getManager() == ((TileEntityMultiblock)tile2).getManager();
	}
	
	public void updateCache(TileEntityMultiblock<T> tile)
	{
		if(!inventories.containsKey(tile.cachedID))
		{
			tile.cachedData.locations.add(Coord4D.get(tile));
			inventories.put(tile.cachedID, tile.cachedData);

			return;
		}

		inventories.get(tile.cachedID).locations.add(Coord4D.get(tile));
	}
	
	public static void reset()
	{
		for(MultiblockManager manager : managers)
		{
			manager.inventories.clear();
		}
	}
}
