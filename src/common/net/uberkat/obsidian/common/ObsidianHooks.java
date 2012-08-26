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
		if(getIC2Item("uraniumOre") != null) IC2Loaded = true;
		
		if(IC2Loaded)
		{
			IC2IronDust = getIC2Item("goldDust");
			IC2GoldDust = getIC2Item("ironDust");
		}
	}
	
	public ItemStack getIC2Item(String name)
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
			System.out.println("[UniversalIC2] Unable to retrieve IC2 item " + name + ".");
			return null;
		}
	}
}
