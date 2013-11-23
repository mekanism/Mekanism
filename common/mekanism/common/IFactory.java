package mekanism.common;

import mekanism.common.RecipeHandler.Recipe;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.tileentity.TileEntityAdvancedElectricMachine;
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
	
	/**
	 * Whether or not this item is a Smelting Factory.
	 * @param itemStack - stack to check
	 * @return if the item is a smelting factory
	 */
	public boolean isFactory(ItemStack itemStack);
	
	public static enum RecipeType
	{
		SMELTING("Smelting", "Smelter.ogg", new ItemStack(Mekanism.MachineBlock, 1, MachineType.ENERGIZED_SMELTER.meta), false),
		ENRICHING("Enriching", "Chamber.ogg", new ItemStack(Mekanism.MachineBlock, 1, MachineType.ENRICHMENT_CHAMBER.meta), false),
		CRUSHING("Crushing", "Crusher.ogg", new ItemStack(Mekanism.MachineBlock, 1, MachineType.CRUSHER.meta), false),
		COMPRESSING("Compressing", "Compressor.ogg", new ItemStack(Mekanism.MachineBlock, 1, MachineType.OSMIUM_COMPRESSOR.meta), true),
		COMBINING("Combining", "Combiner.ogg", new ItemStack(Mekanism.MachineBlock, 1, MachineType.COMBINER.meta), true),
		PURIFYING("Purifying", "PurificationChamber.ogg", new ItemStack(Mekanism.MachineBlock, 1, MachineType.PURIFICATION_CHAMBER.meta), true);
		
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
			return name;
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
