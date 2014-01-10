package mekanism.client.nei;

import java.util.List;
import java.util.Set;

import mekanism.api.ListUtils;
import mekanism.api.gas.GasRegistry;
import mekanism.client.gui.GuiChemicalInjectionChamber;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ChemicalInjectionChamberRecipeHandler extends AdvancedMachineRecipeHandler
{
	@Override
	public String getRecipeName()
	{
		return "C. Injection Chamber";
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
		return Recipe.CHEMICAL_INJECTION_CHAMBER.get().entrySet();
	}

	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/GuiChemicalInjectionChamber.png";
	}
	
	@Override
	public List<ItemStack> getFuelStacks()
	{
		List<ItemStack> fuels = OreDictionary.getOres("dustSulfur");
		fuels.add(MekanismUtils.getFullGasTank(GasRegistry.getGas("sulfuricAcid")));
		return fuels;
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiChemicalInjectionChamber.class;
	}
}
