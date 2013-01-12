package mekanism.nei;

import java.util.Set;

import mekanism.client.GuiElectricMachine;
import mekanism.common.TileEntityEnrichmentChamber;

public class EnrichmentChamberRecipeHandler extends MachineRecipeHandler
{
	@Override
	public String getRecipeName()
	{
		return "Enrichment Chamber";
	}

	@Override
	public String getRecipeId()
	{
		return "mekanism.chamber";
	}
	
	@Override
	public String getOverlayIdentifier()
	{
		return "chamber";
	}

	@Override
	public Set getRecipes()
	{
		return TileEntityEnrichmentChamber.recipes.entrySet();
	}

	@Override
	public String getGuiTexture()
	{
		return "/resources/mekanism/gui/GuiChamber.png";
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiElectricMachine.class;
	}
}
