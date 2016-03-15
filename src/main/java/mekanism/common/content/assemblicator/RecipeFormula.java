package mekanism.common.content.assemblicator;

import mekanism.api.util.StackUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.RecipeUtils;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class RecipeFormula 
{
	private InventoryCrafting dummy = MekanismUtils.getDummyCraftingInv();
	
	public ItemStack[] input = new ItemStack[9];
	
	public RecipeFormula(ItemStack[] inv)
	{
		this(inv, 0);
	}
	
	public RecipeFormula(ItemStack[] inv, int start)
	{
		for(int i = 0; i < 9; i++)
		{
			input[i] = StackUtils.size(inv[start+i], 1);
		}
	}
	
	private void resetToRecipe()
	{
		for(int i = 0; i < 9; i++)
		{
			dummy.setInventorySlotContents(i, input[i]);
		}
	}
	
	public boolean matches(World world, ItemStack[] input, int start)
	{
		resetToRecipe();
		
		IRecipe origRecipe = RecipeUtils.getRecipeFromGrid(dummy, world);
		
		for(int i = 0; i < 9; i++)
		{
			dummy.setInventorySlotContents(i, input[start+i]);
		}
		
		IRecipe newRecipe = RecipeUtils.getRecipeFromGrid(dummy, world);
		
		return origRecipe == newRecipe;
	}
	
	public boolean isIngredientInPos(World world, ItemStack stack, int i)
	{
		resetToRecipe();
		
		IRecipe origRecipe = RecipeUtils.getRecipeFromGrid(dummy, world);
		dummy.setInventorySlotContents(i, stack);
		IRecipe newRecipe = RecipeUtils.getRecipeFromGrid(dummy, world);
		
		return origRecipe == newRecipe;
	}
	
	public boolean isIngredient(World world, ItemStack stack)
	{
		resetToRecipe();
		
		IRecipe origRecipe = RecipeUtils.getRecipeFromGrid(dummy, world);
		
		for(int i = 0; i < 9; i++)
		{
			dummy.setInventorySlotContents(i, stack);
			
			IRecipe newRecipe = RecipeUtils.getRecipeFromGrid(dummy, world);
			
			if(origRecipe == newRecipe)
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
		resetToRecipe();
		
		return RecipeUtils.getRecipeFromGrid(dummy, world);
	}
	
	public boolean isFormulaEqual(World world, RecipeFormula formula)
	{
		return formula.getRecipe(world) == getRecipe(world);
	}
}
