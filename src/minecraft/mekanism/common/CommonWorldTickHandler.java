package mekanism.common;

import java.util.EnumSet;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
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
			World world = (World)tickData[0];
			
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
