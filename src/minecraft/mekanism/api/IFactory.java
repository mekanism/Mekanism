package mekanism.api;

import mekanism.common.RecipeHandler;
import mekanism.common.RecipeHandler.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public interface IFactory 
{
	/**
	 * Gets the recipe type this Smelting Factory currently has.
	 * @param itemStack - stack to check
	 * @return RecipeType ordinal
	 */
	public int getRecipeType(ItemStack itemStack);
	
	/**
	 * Sets the recipe type of this Smelting Factory to a new value.
	 * @param type - RecipeType ordinal
	 * @param itemStack - stack to set
	 */
	public void setRecipeType(int type, ItemStack itemStack);
	
	/**
	 * Whether or not this item is a Smelting Factory.
	 * @param itemStack - stack to check
	 * @return if the item is a smelting factory
	 */
	public boolean isFactory(ItemStack itemStack);
	
	public static enum RecipeType
	{
		SMELTING("Smelting", "Smelter.ogg"),
		ENRICHING("Enriching", "Chamber.ogg"),
		CRUSHING("Crushing", "Crusher.ogg");
		
		private String name;
		private String sound;
		
		public ItemStack getCopiedOutput(ItemStack input, boolean stackDecrease)
		{
			if(this == SMELTING)
			{
				ItemStack toReturn = FurnaceRecipes.smelting().getSmeltingResult(input).copy();
				
				if(stackDecrease)
				{
					input.stackSize--;
				}
				
				return toReturn;
			}
			else if(this == ENRICHING)
			{
				return RecipeHandler.getOutput(input, stackDecrease, Recipe.ENRICHMENT_CHAMBER.get());
			}
			else if(this == CRUSHING)
			{
				return RecipeHandler.getOutput(input, stackDecrease, Recipe.CRUSHER.get());
			}
			
			return null;
		}
		
		public String getName()
		{
			return name;
		}
		
		public String getSound()
		{
			return sound;
		}
		
		private RecipeType(String s, String s1)
		{
			name = s;
			sound = s1;
		}
	}
}
