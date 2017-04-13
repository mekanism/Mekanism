package mekanism.common.content.assemblicator;

import mekanism.common.util.MekanismUtils;
import mekanism.common.util.RecipeUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class RecipeFormula 
{
	private InventoryCrafting dummy = MekanismUtils.getDummyCraftingInv();
	
	public ItemStack[] input = new ItemStack[9];
	
	public IRecipe recipe = null;
	
	public RecipeFormula(World world, ItemStack[] inv)
	{
		this(world, inv, 0);
	}
	
	public RecipeFormula(World world, ItemStack[] inv, int start)
	{
		for(int i = 0; i < 9; i++)
		{
			input[i] = StackUtils.size(inv[start+i], 1);
		}
		
		resetToRecipe();
		
		recipe = RecipeUtils.getRecipeFromGrid(dummy, world);
	}
	
	private void resetToRecipe()
	{
		for(int i = 0; i < 9; i++)
		{
			dummy.setInventorySlotContents(i, input[i]);
		}
	}
	
	public boolean matches(World world, ItemStack[] newInput, int start)
	{
		for(int i = 0; i < 9; i++)
		{
			dummy.setInventorySlotContents(i, newInput[start+i]);
		}
		
		return recipe.matches(dummy, world);
	}
	
	public boolean isIngredientInPos(World world, ItemStack stack, int i)
	{
		resetToRecipe();
		dummy.setInventorySlotContents(i, stack);
		
		return recipe.matches(dummy, world);
	}
	
	public boolean isIngredient(World world, ItemStack stack)
	{
		for(int i = 0; i < 9; i++)
		{
			dummy.setInventorySlotContents(i, stack);
			
			if(recipe.matches(dummy, world))
			{
				return true;
			}
			
			dummy.setInventorySlotContents(i, input[i]);
		}
		
		return false;
	}
	
	public boolean isValidFormula(World world)
	{
		return getRecipe(world) != null;
	}
	
	public IRecipe getRecipe(World world)
	{
		return recipe;
	}
	
	public boolean isFormulaEqual(World world, RecipeFormula formula)
	{
		return formula.getRecipe(world) == getRecipe(world);
	}
}
