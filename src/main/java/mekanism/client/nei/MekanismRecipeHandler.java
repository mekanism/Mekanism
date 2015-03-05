package mekanism.client.nei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mekanism.api.energy.IEnergizedItem;
import mekanism.api.gas.IGasItem;
import mekanism.common.base.IEnergyCube;
import mekanism.common.base.IFactory;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.recipe.MekanismRecipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.ShapedRecipeHandler;

public class MekanismRecipeHandler extends ShapedRecipeHandler
{
	@Override
	public String getRecipeName()
	{
		return "Mekanism " + MekanismUtils.localize("recipe.mekanismShaped");
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId.equals("crafting") && getClass() == MekanismRecipeHandler.class)
		{
			List<IRecipe> allrecipes = CraftingManager.getInstance().getRecipeList();

			for(IRecipe irecipe : allrecipes)
			{
				if(irecipe instanceof MekanismRecipe)
				{
					MekanismRecipe energyRecipe = (MekanismRecipe)irecipe;
					CachedEnergyRecipe recipe = new CachedEnergyRecipe(energyRecipe.width, energyRecipe.height, energyRecipe.getInput(), energyRecipe.getRecipeOutput());
					arecipes.add(recipe);
				}
			}
		}
		else {
			super.loadCraftingRecipes(outputId, results);
		}
	}

	@Override
	public void loadCraftingRecipes(ItemStack result)
	{
		List<IRecipe> allrecipes = CraftingManager.getInstance().getRecipeList();

		for(IRecipe irecipe : allrecipes)
		{
			if(irecipe instanceof MekanismRecipe && areItemsEqual(irecipe.getRecipeOutput(), result))
			{
				MekanismRecipe energyRecipe = (MekanismRecipe)irecipe;
				CachedEnergyRecipe recipe = new CachedEnergyRecipe(energyRecipe.width, energyRecipe.height, energyRecipe.getInput(), energyRecipe.getRecipeOutput());
				arecipes.add(recipe);
			}
		}
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		List<IRecipe> allrecipes = CraftingManager.getInstance().getRecipeList();

		for(IRecipe irecipe : allrecipes)
		{
			if(irecipe instanceof MekanismRecipe)
			{
				MekanismRecipe energyRecipe = (MekanismRecipe)irecipe;
				CachedEnergyRecipe recipe = new CachedEnergyRecipe(energyRecipe.width, energyRecipe.height, energyRecipe.getInput(), energyRecipe.getRecipeOutput());

				if(recipe.contains(recipe.ingredients, ingredient))
				{
					recipe.setIngredientPermutation(recipe.ingredients, ingredient);
					arecipes.add(recipe);
				}
			}
		}
	}

	public static boolean areItemsEqual(ItemStack target, ItemStack input)
	{
		if(target == null && input != null || target != null && input == null)
		{
			return false;
		}
		else if(target == null && input == null)
		{
			return true;
		}

		if(target.getItem() != input.getItem())
		{
			return false;
		}

		if(!(target.getItem() instanceof IEnergizedItem) && !(input.getItem() instanceof IEnergizedItem) && !(target.getItem() instanceof IGasItem) && !(input.getItem() instanceof IGasItem))
		{
			if(target.getItemDamage() != input.getItemDamage() && target.getItemDamage() != OreDictionary.WILDCARD_VALUE)
			{
				return false;
			}
		}
		else {
			if(target.getItem() instanceof IEnergizedItem && input.getItem() instanceof IEnergizedItem)
			{
				if(((IEnergizedItem)target.getItem()).isMetadataSpecific(target) && ((IEnergizedItem)input.getItem()).isMetadataSpecific(input))
				{
					if(target.getItemDamage() != input.getItemDamage() && target.getItemDamage() != OreDictionary.WILDCARD_VALUE)
					{
						return false;
					}
				}
			}
			
			if(target.getItem() instanceof IGasItem && input.getItem() instanceof IGasItem)
			{
				if(((IGasItem)target.getItem()).isMetadataSpecific(target) && ((IGasItem)input.getItem()).isMetadataSpecific(input))
				{
					if(target.getItemDamage() != input.getItemDamage() && target.getItemDamage() != OreDictionary.WILDCARD_VALUE)
					{
						return false;
					}
				}
			}

			if(target.getItem() instanceof IEnergyCube && input.getItem() instanceof IEnergyCube)
			{
				if(((IEnergyCube)target.getItem()).getEnergyCubeTier(target) != ((IEnergyCube)input.getItem()).getEnergyCubeTier(input))
				{
					return false;
				}
			}
			else if(target.getItem() instanceof ItemBlockBasic && input.getItem() instanceof ItemBlockBasic)
			{
				if(((ItemBlockBasic)target.getItem()).getTier(target) != ((ItemBlockBasic)input.getItem()).getTier(input))
				{
					return false;
				}
			}
			else if(target.getItem() instanceof IFactory && input.getItem() instanceof IFactory)
			{
				if(isFactory(target) && isFactory(input))
				{
					if(((IFactory)target.getItem()).getRecipeType(target) != ((IFactory)input.getItem()).getRecipeType(input))
					{
						return false;
					}
				}
			}
		}

		return true;
	}

	private static boolean isFactory(ItemStack stack)
	{
		return MachineType.get(stack) == MachineType.BASIC_FACTORY || MachineType.get(stack) == MachineType.ADVANCED_FACTORY || MachineType.get(stack) == MachineType.ELITE_FACTORY;
	}

	public class CachedEnergyRecipe extends CachedRecipe
	{
		public ArrayList<PositionedStack> ingredients;
		public PositionedStack result;

		public CachedEnergyRecipe(int width, int height, Object[] items, ItemStack out)
		{
			result = new PositionedStack(out, 119, 24);
			ingredients = new ArrayList<PositionedStack>();
			setIngredients(width, height, items);
		}

		public void setIngredients(int width, int height, Object[] items)
		{
			for(int x = 0; x < width; x++)
			{
				for(int y = 0; y < height; y++)
				{
					if(items[y*width+x] == null)
					{
						continue;
					}

					PositionedStack stack = new PositionedStack(items[y*width+x], 25+x*18, 6+y*18);
					stack.setMaxSize(1);
					ingredients.add(stack);
				}
			}
		}

		@Override
		public ArrayList<PositionedStack> getIngredients()
		{
			return (ArrayList<PositionedStack>)getCycledIngredients(MekanismRecipeHandler.this.cycleticks / 20, ingredients);
		}

		@Override
		public PositionedStack getResult()
		{
			return result;
		}

		@Override
		public boolean contains(Collection<PositionedStack> ingredients, ItemStack ingredient)
		{
			for(PositionedStack stack : ingredients)
			{
				for(ItemStack item : stack.items)
				{
					if(areItemsEqual(item, ingredient))
					{
						return true;
					}
				}
			}

			return false;
		}
	}
}
