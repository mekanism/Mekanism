package mekanism.client.nei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.util.ListUtils;
import mekanism.client.gui.GuiChemicalInjectionChamber;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.InjectionRecipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ChemicalInjectionChamberRecipeHandler extends AdvancedMachineRecipeHandler
{
	@Override
	public String getRecipeName()
	{
		return MekanismUtils.localize("nei.chemicalInjectionChamber");
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
	public Collection<InjectionRecipe> getRecipes()
	{
		return Recipe.CHEMICAL_INJECTION_CHAMBER.get().values();
	}

	@Override
	public List<ItemStack> getFuelStacks(Gas gasType)
	{
		if(gasType == GasRegistry.getGas("sulfuricAcid"))
		{
			List<ItemStack> fuels = new ArrayList<ItemStack>();
			fuels.addAll(OreDictionary.getOres("dustSulfur"));
			fuels.add(MekanismUtils.getFullGasTank(GasRegistry.getGas("sulfuricAcid")));
			return fuels;
		}
		else if(gasType == GasRegistry.getGas("water"))
		{
			return ListUtils.asList(MekanismUtils.getFullGasTank(GasRegistry.getGas("water")));
		}
		else if(gasType == GasRegistry.getGas("hydrogenChloride"))
		{
			List<ItemStack> fuels = new ArrayList<ItemStack>();
			fuels.addAll(OreDictionary.getOres("dustSalt"));
			fuels.add(MekanismUtils.getFullGasTank(GasRegistry.getGas("hydrogenChloride")));
			return fuels;
		}

		return new ArrayList<ItemStack>();
	}
	
	@Override
	public ProgressBar getProgressType()
	{
		return ProgressBar.YELLOW;
	}

	@Override
	public Class getGuiClass()
	{
		return GuiChemicalInjectionChamber.class;
	}
}
