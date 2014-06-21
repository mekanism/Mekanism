package mekanism.tools.common;

import mekanism.common.Mekanism;
import net.minecraftforge.common.config.Configuration;

public class ToolsCommonProxy 
{
	/**
	 * Set and load the mod's common configuration properties.
	 */
	public void loadConfiguration()
	{
		Mekanism.configuration.load();
		MekanismTools.armourSpawnRate = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "mob-armor-spawn-rate", 0.03).getDouble(0.03);
		Mekanism.configuration.save();
	}
}
