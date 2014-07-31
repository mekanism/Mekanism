package mekanism.tools.common;

import mekanism.api.MekanismConfig.tools;
import mekanism.common.Mekanism;

public class ToolsCommonProxy 
{
	/**
	 * Set and load the mod's common configuration properties.
	 */
	public void loadConfiguration()
	{
		tools.armorSpawnRate = Mekanism.configuration.get("tools.general", "MobArmorSpawnRate", 0.03, null, 0.00, 1.00).getDouble(0.03);

		if(Mekanism.configuration.hasChanged())
			Mekanism.configuration.save();
	}
}
