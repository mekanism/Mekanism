package mekanism.nei;

import java.util.Set;

import mekanism.client.GuiAdvancedElectricMachine;
import mekanism.client.GuiPurificationChamber;
import mekanism.common.Mekanism;
import mekanism.common.RecipeHandler.Recipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class PurificationChamberRecipeHandler extends AdvancedMachineRecipeHandler
{
	@Override
	public String getRecipeName()
	{
		return "Purification Chamber";
	}

	@Override
	public String getRecipeId()
	{
		return "mekanism.purificationchamber";
	}
	
	@Override
	public String getOverlayIdentifier()
	{
		return "purificationchamber";
	}

	@Override
	public Set getRecipes()
	{
		return Recipe.PURIFICATION_CHAMBER.get().entrySet();
	}

	@Override
	public String getGuiTexture()
	{
		return "/resources/mekanism/gui/GuiPurificationChamber.png";
	}
	
	@Override
	public ItemStack getFuelStack()
	{
		return new ItemStack(Item.flint);
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiPurificationChamber.class;
	}
}
