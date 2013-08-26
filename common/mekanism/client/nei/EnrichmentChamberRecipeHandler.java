package mekanism.client.nei;

import java.util.Set;

import mekanism.client.gui.GuiEnrichmentChamber;
import mekanism.common.RecipeHandler.Recipe;

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
		return Recipe.ENRICHMENT_CHAMBER.get().entrySet();
	}

	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/GuiChamber.png";
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiEnrichmentChamber.class;
	}
}
