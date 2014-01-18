package mekanism.common;

import mekanism.api.AdvancedInput;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.tile.TileEntityAdvancedElectricMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

/**
 * Internal interface for managing various Factory types.
 * @author AidanBrady
 *
 */
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
	
	public static enum RecipeType
	{
		SMELTING("smelting", "Smelter.ogg", MachineType.ENERGIZED_SMELTER.getStack(), false, null),
		ENRICHING("enriching", "Chamber.ogg", MachineType.ENRICHMENT_CHAMBER.getStack(), false, Recipe.ENRICHMENT_CHAMBER),
		CRUSHING("crushing", "Crusher.ogg", MachineType.CRUSHER.getStack(), false, Recipe.CRUSHER),
		COMPRESSING("compressing", "Compressor.ogg", MachineType.OSMIUM_COMPRESSOR.getStack(), true, Recipe.OSMIUM_COMPRESSOR),
		COMBINING("combining", "Combiner.ogg", MachineType.COMBINER.getStack(), true, Recipe.COMBINER),
		PURIFYING("purifying", "PurificationChamber.ogg", MachineType.PURIFICATION_CHAMBER.getStack(), true, Recipe.PURIFICATION_CHAMBER),
		INJECTING("injecting", "ChemicalInjectionChamber.ogg", MachineType.CHEMICAL_INJECTION_CHAMBER.getStack(), true, Recipe.CHEMICAL_INJECTION_CHAMBER);
		
		private String name;
		private String sound;
		private ItemStack stack;
		private boolean usesFuel;
		private Recipe recipe;
		private TileEntityAdvancedElectricMachine cacheTile;
		
		public ItemStack getCopiedOutput(ItemStack input, Gas gas, boolean stackDecrease)
		{
			if(input == null)
			{
				return null;
			}
			
			if(this == SMELTING)
			{
				if(FurnaceRecipes.smelting().getSmeltingResult(input) != null)
				{
					ItemStack toReturn = FurnaceRecipes.smelting().getSmeltingResult(input).copy();
					
					if(stackDecrease)
					{
						input.stackSize--;
					}
					
					return toReturn;
				}
				
				return null;
			}
			
			if(usesFuel())
			{
				return RecipeHandler.getOutput(new AdvancedInput(input, gas), stackDecrease, recipe.get());
			}
			else {
				return RecipeHandler.getOutput(input, stackDecrease, recipe.get());
			}
		}
		
		public GasStack getItemGas(ItemStack itemstack)
		{
			if(usesFuel)
			{
				return getTile().getItemGas(itemstack);
			}
			
			return null;
		}
		
		public int getSecondaryEnergyPerTick()
		{
			if(usesFuel)
			{
				return getTile().SECONDARY_ENERGY_PER_TICK;
			}
			
			return 0;
		}
		
		public TileEntityAdvancedElectricMachine getTile()
		{
			if(cacheTile == null)
			{
				MachineType type = MachineType.get(getStack().itemID, getStack().getItemDamage());
				cacheTile = (TileEntityAdvancedElectricMachine)type.create();
			}
			
			return cacheTile;
		}
		
		public int getMaxSecondaryEnergy()
		{
			return 200;
		}
		
		public ItemStack getStack()
		{
			return stack;
		}
		
		public String getName()
		{
			return MekanismUtils.localize("gui.factory." + name);
		}
		
		public String getSound()
		{
			return sound;
		}
		
		public boolean usesFuel()
		{
			return usesFuel;
		}
		
		private RecipeType(String s, String s1, ItemStack is, boolean b, Recipe r)
		{
			name = s;
			sound = s1;
			stack = is;
			usesFuel = b;
			recipe = r;
		}
	}
}
