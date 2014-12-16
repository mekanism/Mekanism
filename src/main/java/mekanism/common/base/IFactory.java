package mekanism.common.base;

import java.util.Map;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.util.StackUtils;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.block.states.BlockStateMachine.MachineBlockType;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.recipe.machines.BasicMachineRecipe;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.tile.TileEntityAdvancedElectricMachine;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumFacing;

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
		SMELTING("smelting", "smelter", MachineBlockType.ENERGIZED_SMELTER.getStack(), false, Recipe.ENERGIZED_SMELTER),
		ENRICHING("enriching", "enrichment", MachineBlockType.ENRICHMENT_CHAMBER.getStack(), false, Recipe.ENRICHMENT_CHAMBER),
		CRUSHING("crushing", "crusher", MachineBlockType.CRUSHER.getStack(), false, Recipe.CRUSHER),
		COMPRESSING("compressing", "compressor", MachineBlockType.OSMIUM_COMPRESSOR.getStack(), true, Recipe.OSMIUM_COMPRESSOR),
		COMBINING("combining", "combiner", MachineBlockType.COMBINER.getStack(), true, Recipe.COMBINER),
		PURIFYING("purifying", "purifier", MachineBlockType.PURIFICATION_CHAMBER.getStack(), true, Recipe.PURIFICATION_CHAMBER),
		INJECTING("injecting", "injection", MachineBlockType.CHEMICAL_INJECTION_CHAMBER.getStack(), true, Recipe.CHEMICAL_INJECTION_CHAMBER);

		private String name;
		private ResourceLocation sound;
		private ItemStack stack;
		private boolean usesFuel;
		private Recipe recipe;
		private TileEntityAdvancedElectricMachine cacheTile;

		public BasicMachineRecipe getRecipe(ItemStackInput input)
		{
			return RecipeHandler.getRecipe(input, recipe.get());
		}

		public BasicMachineRecipe getRecipe(ItemStack input)
		{
			return getRecipe(new ItemStackInput(input));
		}

		public AdvancedMachineRecipe getRecipe(AdvancedMachineInput input)
		{
			return RecipeHandler.getRecipe(input, recipe.get());
		}

		public AdvancedMachineRecipe getRecipe(ItemStack input, Gas gas)
		{
			return getRecipe(new AdvancedMachineInput(input, gas));
		}

		public MachineRecipe getAnyRecipe(ItemStack slotStack, Gas gasType)
		{
			if(usesFuel())
			{
				return getRecipe(slotStack,gasType);
			}
			return getRecipe(slotStack);
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
				return getTile().BASE_SECONDARY_ENERGY_PER_TICK;
			}

			return 0;
		}

		public boolean canReceiveGas(EnumFacing side, Gas type)
		{
			if(usesFuel)
			{
				return getTile().canReceiveGas(side, type);
			}

			return false;
		}

		public boolean canTubeConnect(EnumFacing side)
		{
			if(usesFuel)
			{
				return getTile().canTubeConnect(side);
			}

			return false;
		}

		public boolean isValidGas(Gas gas)
		{
			if(usesFuel)
			{
				return getTile().isValidGas(gas);
			}

			return false;
		}

		public boolean hasRecipe(ItemStack itemStack)
		{
			if(itemStack == null)
			{
				return false;
			}

			for(Object obj : recipe.get().entrySet())
			{
				if(((Map.Entry)obj).getKey() instanceof AdvancedMachineInput)
				{
					Map.Entry entry = (Map.Entry)obj;

					ItemStack stack = ((AdvancedMachineInput)entry.getKey()).itemStack;

					if(StackUtils.equalsWildcard(stack, itemStack))
					{
						return true;
					}
				}
			}

			return false;
		}

		public TileEntityAdvancedElectricMachine getTile()
		{
			if(cacheTile == null)
			{
				MachineBlockType type = MachineBlockType.get(Block.getBlockFromItem(getStack().getItem()), getStack().getItemDamage());
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

		public ResourceLocation getSound()
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
			sound = new ResourceLocation("mekanism", "tile.machine." + s1);
			stack = is;
			usesFuel = b;
			recipe = r;
		}
	}
}
