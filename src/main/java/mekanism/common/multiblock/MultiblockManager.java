package mekanism.common.multiblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.tank.DynamicTankCache;
import mekanism.common.tank.SynchronizedTankData;
import mekanism.common.tile.TileEntityDynamicTank;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;

public class MultiblockManager
{
	private static Set<MultiblockManager> managers = new HashSet<MultiblockManager>();
	
	public DataHandler dataHandler;
	
	public String name;
	
	/** A map containing references to all dynamic tank inventory caches. */
	public Map<Integer, DynamicTankCache> inventories = new HashMap<Integer, DynamicTankCache>();
	
	public MultiblockManager(String s)
	{
		name = s;
		managers.add(this);
	}
	
	public void createOrLoad(World world)
	{
		if(dataHandler == null)
		{
			dataHandler = (DataHandler)world.perWorldStorage.loadData(DataHandler.class, name);
			
			if(dataHandler == null)
			{
				dataHandler = new DataHandler();
				world.perWorldStorage.setData(name, dataHandler);
			}
		}
	}
	
	/**
	 * Grabs an inventory from the world's caches, and removes all the world's references to it.
	 * @param world - world the cache is stored in
	 * @param id - inventory ID to pull
	 * @return correct Dynamic Tank inventory cache
	 */
	public DynamicTankCache pullInventory(World world, int id)
	{
		DynamicTankCache toReturn = inventories.get(id);
		
		inventories.remove(id);
		dataHandler.markDirty();

		return toReturn;
	}

	/**
	 * Updates a dynamic tank cache with the defined inventory ID with the parameterized values.
	 * @param inventoryID - inventory ID of the dynamic tank
	 * @param cache - cache of the dynamic tank
	 * @param tileEntity - dynamic tank TileEntity
	 */
	public void updateCache(TileEntityDynamicTank tileEntity)
	{
		if(!inventories.containsKey(tileEntity.structure.inventoryID))
		{
			DynamicTankCache cache = new DynamicTankCache();
			cache.sync(tileEntity.structure);
			cache.locations.add(Coord4D.get(tileEntity));

			inventories.put(tileEntity.structure.inventoryID, cache);

			return;
		}
		
		inventories.get(tileEntity.structure.inventoryID).sync(tileEntity.structure);
		inventories.get(tileEntity.structure.inventoryID).locations.add(Coord4D.get(tileEntity));
		dataHandler.markDirty();
	}

	/**
	 * Grabs a unique inventory ID for a dynamic tank.
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
			ArrayList<Integer> idsToKill = new ArrayList<Integer>();
			HashMap<Integer, HashSet<Coord4D>> tilesToKill = new HashMap<Integer, HashSet<Coord4D>>();
			
			for(Map.Entry<Integer, DynamicTankCache> entry : manager.inventories.entrySet())
			{
				int inventoryID = entry.getKey();
	
				for(Coord4D obj : entry.getValue().locations)
				{
					if(obj.dimensionId == world.provider.dimensionId)
					{
						TileEntity tileEntity = obj.getTileEntity(world);
	
						if(!(tileEntity instanceof TileEntityDynamicTank) || getStructureId(((TileEntityDynamicTank)tileEntity)) != inventoryID)
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
					manager.inventories.get(entry.getKey()).locations.remove(obj);
					manager.dataHandler.markDirty();
				}
			}
	
			for(int inventoryID : idsToKill)
			{
				manager.inventories.remove(inventoryID);
				manager.dataHandler.markDirty();
			}
		}
	}
	
	public static int getStructureId(TileEntityDynamicTank tile)
	{
		return tile.structure != null ? tile.structure.inventoryID : -1;
	}
	
	public int getInventoryId(TileEntityDynamicTank tile)
	{
		Coord4D coord = Coord4D.get(tile);
		
		for(Map.Entry<Integer, DynamicTankCache> entry : inventories.entrySet())
		{
			if(entry.getValue().locations.contains(coord))
			{
				return entry.getKey();
			}
		}
		
		return -1;
	}
	
	public static void load(World world)
	{
		for(MultiblockManager manager : managers)
		{
			manager.createOrLoad(world);
		}
	}
	
	public static void reset()
	{
		for(MultiblockManager manager : managers)
		{
			manager.inventories.clear();
			manager.dataHandler = null;
		}
	}
	
	public class DataHandler extends WorldSavedData
	{
		public DataHandler()
		{
			super(name);
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbtTags) 
		{
			NBTTagList list = nbtTags.getTagList("invList", NBT.TAG_COMPOUND);
			
			for(int i = 0; i < list.tagCount(); i++)
			{
				NBTTagCompound compound = list.getCompoundTagAt(i);
				DynamicTankCache cache = new DynamicTankCache();
				cache.load(compound);
				
				NBTTagList coordsList = compound.getTagList("coordsList", NBT.TAG_COMPOUND);
				
				for(int j = 0; j < coordsList.tagCount(); j++)
				{
					cache.locations.add(Coord4D.read(coordsList.getCompoundTagAt(j)));
				}
			}
		}

		@Override
		public void writeToNBT(NBTTagCompound nbtTags) 
		{
			NBTTagList list = new NBTTagList();
			
			for(Map.Entry<Integer, DynamicTankCache> entry : inventories.entrySet())
			{
				NBTTagCompound compound = new NBTTagCompound();
				compound.setInteger("id", entry.getKey());
				entry.getValue().save(compound);
				
				NBTTagList coordsList = new NBTTagList();
				
				for(Coord4D coord : entry.getValue().locations)
				{
					coordsList.appendTag(coord.write(new NBTTagCompound()));
				}
				
				compound.setTag("coordsList", coordsList);
				list.appendTag(compound);
			}
			
			nbtTags.setTag("invList", list);
		}
	}
}
