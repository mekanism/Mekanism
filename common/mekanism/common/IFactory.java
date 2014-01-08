package mekanism.common;

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
		SMELTING("smelting", "Smelter.ogg", MachineType.ENERGIZED_SMELTER.getStack(), false),
		ENRICHING("enriching", "Chamber.ogg", MachineType.ENRICHMENT_CHAMBER.getStack(), false),
		CRUSHING("crushing", "Crusher.ogg", MachineType.CRUSHER.getStack(), false),
		COMPRESSING("compressing", "Compressor.ogg", MachineType.OSMIUM_COMPRESSOR.getStack(), true),
		COMBINING("combining", "Combiner.ogg", MachineType.COMBINER.getStack(), true),
		PURIFYING("purifying", "PurificationChamber.ogg", MachineType.PURIFICATION_CHAMBER.getStack(), true),
		INJECTING("injecting", "ChemicalInjectionChamber.ogg", MachineType.CHEMICAL_INJECTION_CHAMBER.getStack(), true);
		
		private String name;
		private String sound;
		private ItemStack stack;
		private boolean usesFuel;
		
		public ItemStack getCopiedOutput(ItemStack input, boolean stackDecrease)
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
			}
			else if(this == ENRICHING)
			{
				return RecipeHandler.getOutput(input, stackDecrease, Recipe.ENRICHMENT_CHAMBER.get());
			}
			else if(this == CRUSHING)
			{
				return RecipeHandler.getOutput(input, stackDecrease, Recipe.CRUSHER.get());
			}
			else if(this == COMPRESSING)
			{
				return RecipeHandler.getOutput(input, stackDecrease, Recipe.OSMIUM_COMPRESSOR.get());
			}
			else if(this == COMBINING)
			{
				return RecipeHandler.getOutput(input, stackDecrease, Recipe.COMBINER.get());
			}
			else if(this == PURIFYING)
			{
				return RecipeHandler.getOutput(input, stackDecrease, Recipe.PURIFICATION_CHAMBER.get());
			}
			else if(this == INJECTING)
			{
				return RecipeHandler.getOutput(input, stackDecrease, Recipe.CHEMICAL_INJECTION_CHAMBER.get());
			}
			
			return null;
		}
		
		public int getFuelTicks(ItemStack itemstack)
		{
			if(usesFuel)
			{
				MachineType type = MachineType.get(getStack().itemID, getStack().getItemDamage());
				TileEntityAdvancedElectricMachine machine = (TileEntityAdvancedElectricMachine)type.create();
				
				return machine.getFuelTicks(itemstack);
			}
			
			return 0;
		}
		
		public int getSecondaryEnergyPerTick()
		{
			if(usesFuel)
			{
				MachineType type = MachineType.get(getStack().itemID, getStack().getItemDamage());
				TileEntityAdvancedElectricMachine machine = (TileEntityAdvancedElectricMachine)type.create();
				
				return machine.SECONDARY_ENERGY_PER_TICK;
			}
			
			return 0;
		}
		
		public int getMaxSecondaryEnergy()
		{
			if(usesFuel)
			{
				MachineType type = MachineType.get(getStack().itemID, getStack().getItemDamage());
				TileEntityAdvancedElectricMachine machine = (TileEntityAdvancedElectricMachine)type.create();
				
				return machine.MAX_SECONDARY_ENERGY;
			}
			
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
		
		private RecipeType(String s, String s1, ItemStack is, boolean b)
		{
			name = s;
			sound = s1;
			stack = is;
			usesFuel = b;
		}
	}
}
