package mekanism.nei;

import mekanism.client.GuiCombiner;
import mekanism.client.GuiCrusher;
import mekanism.client.GuiEnrichmentChamber;
import mekanism.client.GuiMetallurgicInfuser;
import mekanism.client.GuiOsmiumCompressor;
import mekanism.client.GuiPurificationChamber;
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
		API.registerRecipeHandler(new MekanismRecipeHandler());
		API.registerUsageHandler(new MekanismRecipeHandler());
		
		API.setGuiOffset(GuiEnrichmentChamber.class, 16, 5);
		API.setGuiOffset(GuiOsmiumCompressor.class, 16, 5);
		API.setGuiOffset(GuiCrusher.class, 16, 5);
		API.setGuiOffset(GuiCombiner.class, 16, 5);
		API.setGuiOffset(GuiPurificationChamber.class, 16, 5);
		API.setGuiOffset(GuiMetallurgicInfuser.class, 5, 15);
		
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
		return "1.0.4";
	}
}
