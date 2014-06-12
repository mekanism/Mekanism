package mekanism.client.nei;

import java.util.Set;

import mekanism.client.gui.GuiEnrichmentChamber;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.MekanismUtils;

public class EnrichmentChamberRecipeHandler extends MachineRecipeHandler
{
	@Override
	public String getRecipeName()
	{
		return MekanismUtils.localize("tile.MachineBlock.EnrichmentChamber.name");
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
	public Class getGuiClass()
	{
		return GuiEnrichmentChamber.class;
	}
}
