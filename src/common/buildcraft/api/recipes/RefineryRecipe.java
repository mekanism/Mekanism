/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.recipes;

import java.util.LinkedList;

import buildcraft.api.liquids.LiquidStack;


public class RefineryRecipe {

	private static LinkedList<RefineryRecipe> recipes = new LinkedList<RefineryRecipe>();
	
	public static void registerRefineryRecipe(RefineryRecipe recipe) {
		if (!recipes.contains(recipe)) {
			recipes.add(recipe);
		}
	}

	public static RefineryRecipe findRefineryRecipe(LiquidStack liquid1, LiquidStack liquid2) {
		for(RefineryRecipe recipe : recipes)
			if(recipe.matches(liquid1, liquid2))
				return recipe;
		
		return null;
	}

	public final LiquidStack ingredient1;
	public final LiquidStack ingredient2;
	public final LiquidStack result;
	
	public final int energy;
	public final int delay;

	public RefineryRecipe(int ingredientId1, int ingredientQty1, int ingredientId2, int ingredientQty2, int resultId, int resultQty,
			int energy, int delay) {
		this(new LiquidStack(ingredientId1, ingredientQty1, 0), new LiquidStack(ingredientId2, ingredientQty2, 0), new LiquidStack(resultId, resultQty, 0), energy, delay);
	}
	public RefineryRecipe(LiquidStack ingredient1, LiquidStack ingredient2, LiquidStack result, int energy, int delay) {
		this.ingredient1 = ingredient1;
		this.ingredient2 = ingredient2;
		this.result = result;
		this.energy = energy;
		this.delay = delay;
	}

	public boolean matches(LiquidStack liquid1, LiquidStack liquid2) {
		
		// No inputs, return.
		if(liquid1 == null && liquid2 == null)
			return false;

		// Return if two ingredients are required but only one was supplied.
		if((ingredient1 != null && ingredient2 != null)
				&& (liquid1 == null || liquid2 == null))
			return false;
		
		if(ingredient1 != null) {
			
			if(ingredient2 == null)
				return ingredient1.isLiquidEqual(liquid1) || ingredient1.isLiquidEqual(liquid2);
			else
				return (ingredient1.isLiquidEqual(liquid1) && ingredient2.isLiquidEqual(liquid2))
						|| (ingredient2.isLiquidEqual(liquid1) && ingredient1.isLiquidEqual(liquid2));
			
		} else if(ingredient2 != null)
			return ingredient2.isLiquidEqual(liquid1) || ingredient2.isLiquidEqual(liquid2);
		else
			return false;

	}
}
