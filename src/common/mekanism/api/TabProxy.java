package mekanism.api;

import net.minecraft.src.*;

/**
 * Class used to add items or blocks to the Mekanism creative tab.
 * @author AidanBrady
 *
 */
public final class TabProxy
{
	/** The 'Mekanism' class where the tab instance is stored. */
	public static Class Mekanism;
	
	/**
	 * Attempts to get the Mekanism creative tab instance from the 'Mekanism' class. Will return
	 * the tab if the mod is loaded, but otherwise will return null. Be sure you check it isn't 
	 * null before you use it!
	 * @return mekanism creative tab if can, otherwise null
	 */
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
