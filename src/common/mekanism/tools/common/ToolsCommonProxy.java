package mekanism.tools.common;

import net.minecraft.src.*;

/**
 * Common proxy for the Mekanism Tools module.
 * @author AidanBrady
 *
 */
public class ToolsCommonProxy
{
	/**
	 * Register and load client-only render information.
	 */
	public void registerRenderInformation() {}
	
	/**
	 * Gets the armor index number from ClientProxy.
	 * @param armor indicator
	 * @return armor index number
	 */
	public int getArmorIndex(String string) 
	{
		return 0;
	}
	
	/**
	 * Set and load the mod's common configuration properties.
	 */
	public void loadConfiguration() {}
}
