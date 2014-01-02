package mekanism.client.nei;

import java.util.List;
import java.util.Set;

import mekanism.api.gas.GasRegistry;
import mekanism.client.gui.GuiPurificationChamber;
import mekanism.common.RecipeHandler.Recipe;
import mekanism.common.util.ListUtils;
import mekanism.common.util.MekanismUtils;
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
		return "mekanism:gui/GuiPurificationChamber.png";
	}
	
	@Override
	public List<ItemStack> getFuelStacks()
	{
		return ListUtils.asList(new ItemStack(Item.flint), MekanismUtils.getFullGasTank(GasRegistry.getGas("oxygen")));
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiPurificationChamber.class;
	}
}
