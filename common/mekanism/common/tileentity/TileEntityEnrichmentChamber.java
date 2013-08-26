package mekanism.common.tileentity;

import java.util.Map;

import mekanism.common.Mekanism;
import mekanism.common.RecipeHandler.Recipe;
import mekanism.common.block.BlockMachine.MachineType;
import net.minecraft.util.ResourceLocation;

public class TileEntityEnrichmentChamber extends TileEntityElectricMachine
{
	public TileEntityEnrichmentChamber()
	{
		super("Chamber.ogg", "Enrichment Chamber", new ResourceLocation("mekanism", "gui/GuiChamber.png"), Mekanism.enrichmentChamberUsage, 200, MachineType.ENRICHMENT_CHAMBER.baseEnergy);
	}
	
	@Override
	public Map getRecipes()
	{
		return Recipe.ENRICHMENT_CHAMBER.get();
	}
	
	@Override
	public float getVolumeMultiplier()
	{
		return 0.3F;
	}
}
