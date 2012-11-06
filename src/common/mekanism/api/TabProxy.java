package mekanism.api;

import net.minecraft.src.*;

public final class TabProxy
{
	public static Class Mekanism;
	
	public static CreativeTabs tabMekanism()
	{
		try {
			if(Mekanism == null)
			{
				Mekanism = Class.forName("mekanism.common.Mekanism");
			}
			
			Object ret = Mekanism.getField("tabMekanism").get(null);
			
			if(ret instanceof CreativeTabs)
			{
				return (CreativeTabs)ret;
			}
			else {
				return null;
			}
		} catch(Exception e) {
			System.err.println("[Mekanism] Error retrieving Mekanism creative tab.");
			return null;
		}
	}
}
