package mekanism.client.jei;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraft.item.ItemStack;

import com.google.common.base.Objects;

public class GasStackHelper implements IIngredientHelper<GasStack>
{
	@Override
	public List<GasStack> expandSubtypes(List<GasStack> contained) 
	{
		return contained;
	}

	@Override
	@Nullable
	public GasStack getMatch(Iterable<GasStack> ingredients, GasStack toMatch) 
	{
		for(GasStack stack : ingredients) 
		{
			if(toMatch.getGas() == stack.getGas()) 
			{
				return stack;
			}
		}
		
		return null;
	}

	@Override
	public String getDisplayName(GasStack ingredient)
	{
		return ingredient.getGas().getLocalizedName();
	}

	@Override
	public String getUniqueId(GasStack ingredient) 
	{
		return "gas:" + ingredient.getGas().getName();
	}

	@Override
	public String getWildcardId(GasStack ingredient) 
	{
		return getUniqueId(ingredient);
	}

	@Override
	public String getModId(GasStack ingredient) 
	{
		return ingredient.getGas().getIcon().getResourceDomain();
	}

	@Override
	public Iterable<Color> getColors(GasStack ingredient) 
	{
		return Collections.emptyList();
	}

	//@Override
	public ItemStack cheatIngredient(GasStack ingredient, boolean fullStack) 
	{
		return null;
	}

	@Override
	public String getErrorInfo(GasStack ingredient) 
	{
		Objects.ToStringHelper toStringHelper = Objects.toStringHelper(GasStack.class);

		Gas gas = ingredient.getGas();
		
		if(gas != null) 
		{
			toStringHelper.add("Gas", gas.getLocalizedName());
		} 
		else {
			toStringHelper.add("Gas", "null");
		}

		toStringHelper.add("Amount", ingredient.amount);

		return toStringHelper.toString();
	}
}
