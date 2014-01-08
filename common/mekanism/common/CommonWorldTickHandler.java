package mekanism.common;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import mekanism.api.Coord4D;
import mekanism.common.tank.DynamicTankCache;
import mekanism.common.tile.TileEntityDynamicTank;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class CommonWorldTickHandler implements ITickHandler
{
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		if(tickData[0] instanceof World)
		{
			ArrayList<Integer> idsToKill = new ArrayList<Integer>();
			HashMap<Integer, HashSet<Coord4D>> tilesToKill = new HashMap<Integer, HashSet<Coord4D>>();
			
			World world = (World)tickData[0];
			
			if(!world.isRemote)
			{
				for(Map.Entry<Integer, DynamicTankCache> entry : Mekanism.dynamicInventories.entrySet())
				{
					int inventoryID = entry.getKey();
					
					for(Coord4D obj : entry.getValue().locations)
					{
						if(obj.dimensionId == world.provider.dimensionId)
						{
							TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)obj.getTileEntity(world);
							
							if(tileEntity == null || tileEntity.inventoryID != inventoryID)
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
						Mekanism.dynamicInventories.get(entry.getKey()).locations.remove(obj);
					}
				}
				
				for(int inventoryID : idsToKill)
				{	
					for(Coord4D obj : Mekanism.dynamicInventories.get(inventoryID).locations)
					{
						TileEntityDynamicTank dynamicTank = (TileEntityDynamicTank)obj.getTileEntity(world);
						
						if(dynamicTank != null)
						{
							dynamicTank.cachedFluid = null;
							dynamicTank.inventory = new ItemStack[2];
							dynamicTank.inventoryID = -1;
						}
					}
					
					Mekanism.dynamicInventories.remove(inventoryID);
				}
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel()
	{
		return "MekanismCommonWorld";
	}
}
