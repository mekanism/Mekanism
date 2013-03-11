package mekanism.nei;

import mekanism.common.Mekanism;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEIMekanismConfig implements IConfigureNEI
{
	@Override
	public void loadConfig()
	{
		API.registerRecipeHandler(new EnrichmentChamberRecipeHandler());
		API.registerUsageHandler(new EnrichmentChamberRecipeHandler());
		API.registerRecipeHandler(new OsmiumCompressorRecipeHandler());
		API.registerUsageHandler(new OsmiumCompressorRecipeHandler());
		API.registerRecipeHandler(new CrusherRecipeHandler());
		API.registerUsageHandler(new CrusherRecipeHandler());
		API.registerRecipeHandler(new CombinerRecipeHandler());
		API.registerUsageHandler(new CombinerRecipeHandler());
		API.registerRecipeHandler(new MetallurgicInfuserRecipeHandler());
		API.registerUsageHandler(new MetallurgicInfuserRecipeHandler());
		API.registerRecipeHandler(new PurificationChamberRecipeHandler());
		API.registerUsageHandler(new PurificationChamberRecipeHandler());
		
		API.hideItem(Mekanism.boundingBlockID);
	}

	@Override
	public String getName()
	{
		return "Mekanism NEI Plugin";
	}

	@Override
	public String getVersion()
	{
		return "1.0.2";
	}
}
