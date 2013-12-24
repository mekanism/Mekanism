package mekanism.client.nei;

import java.util.Set;

import mekanism.client.gui.GuiChemicalInjectionChamber;
import mekanism.common.RecipeHandler.Recipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ChemicalInjectionChamberRecipeHandler extends AdvancedMachineRecipeHandler
{
	@Override
	public String getRecipeName()
	{
		return "Chemical Injection Chamber";
	}

	@Override
	public String getRecipeId()
	{
		return "mekanism.chemicalinjectionchamber";
	}
	
	@Override
	public String getOverlayIdentifier()
	{
		return "chemicalinjectionchamber";
	}

	@Override
	public Set getRecipes()
	{
		return Recipe.PURIFICATION_CHAMBER.get().entrySet();
	}

	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/GuiChemicalInjectionChamber.png";
	}
	
	@Override
	public ItemStack getFuelStack()
	{
		return new ItemStack(Item.gunpowder);
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiChemicalInjectionChamber.class;
	}
}
