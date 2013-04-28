package mekanism.common;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import mekanism.api.Object3D;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class CommonWorldTickHandler implements ITickHandler
{
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		if(tickData[0] instanceof World)
		{
			ArrayList<Integer> idsToKill = new ArrayList<Integer>();
			HashMap<Integer, HashSet<Object3D>> tilesToKill = new HashMap<Integer, HashSet<Object3D>>();
			
			World world = (World)tickData[0];
			
			if(!world.isRemote)
			{
				for(Map.Entry<Integer, HashSet<Object3D>> entry : Mekanism.inventoryLocations.entrySet())
				{
					int inventoryID = entry.getKey();
					
					for(Object3D obj : entry.getValue())
					{
						if(obj.dimensionId == world.provider.dimensionId)
						{
							TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)obj.getTileEntity(world);
							
							if(tileEntity == null || tileEntity.inventoryID != inventoryID)
							{
								if(!tilesToKill.containsKey(inventoryID))
								{
									tilesToKill.put(inventoryID, new HashSet<Object3D>());
								}
								
								tilesToKill.get(inventoryID).add(obj);
							}
						}
					}
					
					if(entry.getValue().isEmpty())
					{
						idsToKill.add(inventoryID);
					}
				}
				
				for(Map.Entry<Integer, HashSet<Object3D>> entry : tilesToKill.entrySet())
				{
					for(Object3D obj : entry.getValue())
					{
						Mekanism.inventoryLocations.get(entry.getKey()).remove(obj);
					}
				}
				
				for(int inventoryID : idsToKill)
				{	
					for(Object3D obj : Mekanism.inventoryLocations.get(inventoryID))
					{
						TileEntityDynamicTank dynamicTank = (TileEntityDynamicTank)obj.getTileEntity(world);
						
						if(dynamicTank != null)
						{
							dynamicTank.cachedLiquid = null;
							dynamicTank.inventory = new ItemStack[2];
							dynamicTank.inventoryID = -1;
						}
					}
					
					Mekanism.inventoryLocations.remove(inventoryID);
					Mekanism.dynamicInventories.remove(inventoryID);
				}
			}
			
			for(Object obj : world.loadedEntityList)
			{
				if(obj instanceof EntityItem)
				{
					EntityItem item = (EntityItem)obj;
					
					if(item.getEntityItem() != null)
					{
						ItemStack itemStack = item.getEntityItem();
						
						if(itemStack.getItem() instanceof IElectricChest)
						{
							if(((IElectricChest)itemStack.getItem()).isElectricChest(itemStack))
							{
								itemStack.getItem().onUpdate(itemStack, world, null, 0, false);
							}
						}
					}
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
