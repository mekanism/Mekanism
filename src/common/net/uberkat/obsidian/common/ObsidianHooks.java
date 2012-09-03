package net.uberkat.obsidian.common;

import net.minecraft.src.*;

public class ObsidianHooks 
{
	private Class Ic2Items;
	
	public ItemStack IC2IronDust;
	public ItemStack IC2GoldDust;
	
	public boolean IC2Loaded;
	
	public void hook()
	{
		if(getIC2Item("uraniumOre", true) != null) IC2Loaded = true;
		
		if(IC2Loaded)
		{
			IC2IronDust = getIC2Item("goldDust", false);
			IC2GoldDust = getIC2Item("ironDust", false);
		}
	}
	
	public ItemStack getIC2Item(String name, boolean test)
	{
		try {
			if(Ic2Items == null) Ic2Items = Class.forName("ic2.common.Ic2Items");
			if(Ic2Items == null) Ic2Items = Class.forName("net.minecraft.src.ic2.common.Ic2Items");
			Object ret = Ic2Items.getField(name).get(null);
			
			if(ret instanceof ItemStack)
			{
				return (ItemStack)ret;
			}
			else {
				return null;
			}
		} catch(Exception e) {
			if(!test)
			{
				System.out.println("[ObsidianIngots] Unable to retrieve IC2 item " + name + ".");
			}
			else {
				System.out.println("[ObsidianIngots] Unable to hook into IC2.");
			}
			return null;
		}
	}
}
