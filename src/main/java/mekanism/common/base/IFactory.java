package mekanism.common.base;

import java.util.Map;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.InfuseStorage;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.inputs.InfusionInput;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.recipe.machines.BasicMachineRecipe;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.util.LangUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

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
    int getRecipeType(ItemStack itemStack);

	/**
	 * Sets the recipe type of this Smelting Factory to a new value.
	 * @param type - RecipeType ordinal
	 * @param itemStack - stack to set
	 */
    void setRecipeType(int type, ItemStack itemStack);

	public static enum RecipeType implements IStringSerializable
	{
		SMELTING("Smelting", "smelter", MachineType.ENERGIZED_SMELTER, false, false, Recipe.ENERGIZED_SMELTER),
		ENRICHING("Enriching", "enrichment", MachineType.ENRICHMENT_CHAMBER, false, false, Recipe.ENRICHMENT_CHAMBER),
		CRUSHING("Crushing", "crusher", MachineType.CRUSHER, false, false, Recipe.CRUSHER),
		COMPRESSING("Compressing", "compressor", MachineType.OSMIUM_COMPRESSOR, true, false, Recipe.OSMIUM_COMPRESSOR),
		COMBINING("Combining", "combiner", MachineType.COMBINER, true, false, Recipe.COMBINER),
		PURIFYING("Purifying", "purifier", MachineType.PURIFICATION_CHAMBER, true, true, Recipe.PURIFICATION_CHAMBER),
		INJECTING("Injecting", "injection", MachineType.CHEMICAL_INJECTION_CHAMBER, true, true, Recipe.CHEMICAL_INJECTION_CHAMBER),
		INFUSING("Infusing", "metalinfuser", MachineType.METALLURGIC_INFUSER, false, false, Recipe.METALLURGIC_INFUSER);

		private String name;
		private ResourceLocation sound;
		private MachineType type;
		private boolean usesFuel;
		private boolean fuelSpeed;
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
		
		public MetallurgicInfuserRecipe getRecipe(InfusionInput input)
		{
			return RecipeHandler.getMetallurgicInfuserRecipe(input);
		}
		
		public MetallurgicInfuserRecipe getRecipe(ItemStack input, InfuseStorage storage)
		{
			return getRecipe(new InfusionInput(storage, input));
		}

		public MachineRecipe getAnyRecipe(ItemStack slotStack, Gas gasType, InfuseStorage infuse)
		{
			if(usesFuel())
			{
				return getRecipe(slotStack, gasType);
			}
			else if(this == INFUSING)
			{
				if(infuse.type != null)
				{
					return RecipeHandler.getMetallurgicInfuserRecipe(new InfusionInput(infuse, slotStack));
				}
				else {
					for(Object obj : Recipe.METALLURGIC_INFUSER.get().entrySet())
					{
						Map.Entry entry = (Map.Entry)obj;
						InfusionInput input = (InfusionInput)entry.getKey();
						
						if(input.inputStack.isItemEqual(slotStack))
						{
							return (MetallurgicInfuserRecipe)entry.getValue();
						}
					}
				}
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
			if(itemStack.isEmpty())
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
				MachineType type = MachineType.get(getStack());
				cacheTile = (TileEntityAdvancedElectricMachine)type.create();
			}

			return cacheTile;
		}
		
		public double getEnergyUsage()
		{
			return type.getUsage();
		}

		public int getMaxSecondaryEnergy()
		{
			return 200;
		}

		public ItemStack getStack()
		{
			return type.getStack();
		}
		
		public String getUnlocalizedName()
		{
			return name;
		}

		public String getLocalizedName()
		{
			return LangUtils.localize("gui.factory." + name);
		}

		public ResourceLocation getSound()
		{
			return sound;
		}

		public boolean usesFuel()
		{
			return usesFuel;
		}
		
		public boolean fuelEnergyUpgrades()
		{
			return fuelSpeed;
		}
		
		public static RecipeType getFromMachine(Block block, int meta)
		{
			RecipeType type = null;
			
			for(RecipeType iterType : RecipeType.values())
			{
				ItemStack machineStack = iterType.getStack();
				
				if(Block.getBlockFromItem(machineStack.getItem()) == block && machineStack.getItemDamage() == meta)
				{
					type = iterType;
					break;
				}
			}
			
			return type;
		}

		RecipeType(String s, String s1, MachineType t, boolean b, boolean b1, Recipe r)
		{
			name = s;
			sound = new ResourceLocation("mekanism", "tile.machine." + s1);
			type = t;
			usesFuel = b;
			fuelSpeed = b1;
			recipe = r;
		}

		@Override
		public String getName() 
		{
			return name().toLowerCase();
		}
	}
}
