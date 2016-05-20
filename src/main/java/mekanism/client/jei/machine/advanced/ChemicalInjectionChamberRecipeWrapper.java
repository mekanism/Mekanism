package mekanism.client.jei.machine.advanced;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.util.ListUtils;
import mekanism.client.jei.machine.AdvancedMachineRecipeCategory;
import mekanism.client.jei.machine.AdvancedMachineRecipeWrapper;
import mekanism.common.Tier.GasTankTier;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ChemicalInjectionChamberRecipeWrapper extends AdvancedMachineRecipeWrapper
{
	public ChemicalInjectionChamberRecipeWrapper(AdvancedMachineRecipe r, AdvancedMachineRecipeCategory c)
	{
		super(r, c);
	}
	
	@Override
	public List<ItemStack> getFuelStacks(Gas gasType)
	{
		if(gasType == GasRegistry.getGas("sulfuricAcid"))
		{
			List<ItemStack> fuels = new ArrayList<ItemStack>();
			fuels.addAll(OreDictionary.getOres("dustSulfur"));
			fuels.add(MekanismUtils.getFullGasTank(GasTankTier.BASIC, GasRegistry.getGas("sulfuricAcid")));
			
			return fuels;
		}
		else if(gasType == GasRegistry.getGas("water"))
		{
			return ListUtils.asList(MekanismUtils.getFullGasTank(GasTankTier.BASIC, GasRegistry.getGas("water")));
		}
		else if(gasType == GasRegistry.getGas("hydrogenChloride"))
		{
			List<ItemStack> fuels = new ArrayList<ItemStack>();
			fuels.addAll(OreDictionary.getOres("dustSalt"));
			fuels.add(MekanismUtils.getFullGasTank(GasTankTier.BASIC, GasRegistry.getGas("hydrogenChloride")));
			
			return fuels;
		}

		return new ArrayList<ItemStack>();
	}
}
