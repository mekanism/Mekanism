package mekanism.tools.common;

import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;

public class ToolsCommonProxy 
{
	/**
	 * Set and load the mod's common configuration properties.
	 */
	public void loadConfiguration()
	{
		MekanismConfig.current().tools.load(Mekanism.configuration);

		if(Mekanism.configuration.hasChanged())
		{
			Mekanism.configuration.save();
		}
	}
	
	public void registerItemRenders() {}
}
