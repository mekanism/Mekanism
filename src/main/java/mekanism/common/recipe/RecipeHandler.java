package mekanism.common.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import mekanism.api.AdvancedInput;
import mekanism.api.ChanceOutput;
import mekanism.api.ChemicalPair;
import mekanism.api.PressurizedProducts;
import mekanism.api.PressurizedReactants;
import mekanism.api.PressurizedRecipe;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.infuse.InfusionInput;
import mekanism.api.infuse.InfusionOutput;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/**
 * Class used to handle machine recipes. This is used for both adding recipes and checking outputs.
 * @author AidanBrady
 *
 */
public final class RecipeHandler
{
	public static void addRecipe(Recipe recipe, Object input, Object output)
	{
		recipe.put(input, output);
	}

	/**
	 * Add an Enrichment Chamber recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addEnrichmentChamberRecipe(ItemStack input, ItemStack output)
	{
		Recipe.ENRICHMENT_CHAMBER.put(input, output);
	}

	/**
	 * Add an Osmium Compressor recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addOsmiumCompressorRecipe(ItemStack input, ItemStack output)
	{
		Recipe.OSMIUM_COMPRESSOR.put(new AdvancedInput(input, GasRegistry.getGas("liquidOsmium")), output);
	}

	/**
	 * Add a Combiner recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addCombinerRecipe(ItemStack input, ItemStack output)
	{
		Recipe.COMBINER.put(new AdvancedInput(input, GasRegistry.getGas("liquidStone")), output);
	}

	/**
	 * Add a Crusher recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addCrusherRecipe(ItemStack input, ItemStack output)
	{
		Recipe.CRUSHER.put(input, output);
	}

	/**
	 * Add a Purification Chamber recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addPurificationChamberRecipe(ItemStack input, ItemStack output)
	{
		Recipe.PURIFICATION_CHAMBER.put(new AdvancedInput(input, GasRegistry.getGas("oxygen")), output);
	}

	/**
	 * Add a Metallurgic Infuser recipe.
	 * @param input - input Infusion
	 * @param output - output ItemStack
	 */
	public static void addMetallurgicInfuserRecipe(InfusionInput input, ItemStack output)
	{
		Recipe.METALLURGIC_INFUSER.put(input, InfusionOutput.getInfusion(input, output));
	}

	/**
	 * Add a Chemical Infuser recipe.
	 * @param input - input ChemicalPair
	 * @param output - output GasStack
	 */
	public static void addChemicalInfuserRecipe(ChemicalPair input, GasStack output)
	{
		Recipe.CHEMICAL_INFUSER.put(input, output);
	}

	/**
	 * Add a Chemical Oxidizer recipe.
	 * @param input - input ItemStack
	 * @param output - output GasStack
	 */
	public static void addChemicalOxidizerRecipe(ItemStack input, GasStack output)
	{
		Recipe.CHEMICAL_OXIDIZER.put(input, output);
	}

	/**
	 * Add a Chemical Injection Chamber recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addChemicalInjectionChamberRecipe(AdvancedInput input, ItemStack output)
	{
		Recipe.CHEMICAL_INJECTION_CHAMBER.put(input, output);
	}

	/**
	 * Add an Electrolytic Separator recipe.
	 * @param fluid - FluidStack to electrolyze
	 * @param products - Pair of gases to produce when the fluid is electrolyzed
	 */
	public static void addElectrolyticSeparatorRecipe(FluidStack fluid, ChemicalPair products)
	{
		Recipe.ELECTROLYTIC_SEPARATOR.put(fluid, products);
	}

	/**
	 * Add an Precision Sawmill recipe.
	 * @param input - input ItemStack
	 * @param output - output ChanceOutput
	 */
	public static void addPrecisionSawmillRecipe(ItemStack input, ChanceOutput output)
	{
		Recipe.PRECISION_SAWMILL.put(input, output);
	}

	/**
	 * Add a Chemical Dissolution Chamber recipe.
	 * @param input - input ItemStack
	 * @param output - output GasStack
	 */
	public static void addChemicalDissolutionChamberRecipe(ItemStack input, GasStack output)
	{
		Recipe.CHEMICAL_DISSOLUTION_CHAMBER.put(input, output);
	}

	/**
	 * Add a Chemical Washer recipe.
	 * @param input - input GasStack
	 * @param output - output GasStack
	 */
	public static void addChemicalWasherRecipe(GasStack input, GasStack output)
	{
		Recipe.CHEMICAL_WASHER.put(input, output);
	}

	/**
	 * Add a Chemical Crystallizer recipe.
	 * @param input - input GasStack
	 * @param output - output ItemStack
	 */
	public static void addChemicalCrystallizerRecipe(GasStack input, ItemStack output)
	{
		Recipe.CHEMICAL_CRYSTALLIZER.put(input, output);
	}

	/**
	 * Add a Pressurized Reaction Chamber recipe.
	 * @param input - input PressurizedReactants
	 * @param output - output PressurizedProducts
	 * @param extraEnergy - extra energy needed by the recipe
	 * @param ticks - amount of ticks it takes for this recipe to complete
	 */
	public static void addPRCRecipe(PressurizedReactants input, PressurizedProducts output, double extraEnergy, int ticks)
	{
		PressurizedRecipe recipe = new PressurizedRecipe(input, extraEnergy, output, ticks);
		Recipe.PRESSURIZED_REACTION_CHAMBER.put(input, recipe);
	}

	/**
	 * Gets the InfusionOutput of the InfusionInput in the parameters.
	 * @param infusion - input Infusion
	 * @param stackDecrease - whether or not to decrease the input slot's stack size AND the infuse amount
	 * @return InfusionOutput
	 */
	public static InfusionOutput getMetallurgicInfuserOutput(InfusionInput infusion, boolean stackDecrease)
	{
		if(infusion != null && infusion.inputStack != null)
		{
			HashMap<InfusionInput, InfusionOutput> recipes = Recipe.METALLURGIC_INFUSER.get();

			for(Map.Entry<InfusionInput, InfusionOutput> entry : recipes.entrySet())
			{
				InfusionInput input = (InfusionInput)entry.getKey();

				if(StackUtils.equalsWildcard(input.inputStack, infusion.inputStack) && infusion.inputStack.stackSize >= input.inputStack.stackSize)
				{
					if(infusion.infusionType == input.infusionType)
					{
						if(stackDecrease)
						{
							infusion.inputStack.stackSize -= ((InfusionInput)entry.getKey()).inputStack.stackSize;
						}

						return ((InfusionOutput)entry.getValue()).copy();
					}
				}
			}
		}

		return null;
	}

	/**
	 * Gets the GasStack of the ChemicalInput in the parameters.
	 * @param leftTank - first GasTank
	 * @param rightTank - second GasTank
	 * @param doRemove - actually remove the gases
	 * @return output GasStack
	 */
	public static GasStack getChemicalInfuserOutput(GasTank leftTank, GasTank rightTank, boolean doRemove)
	{
		ChemicalPair input = new ChemicalPair(leftTank.getGas(), rightTank.getGas());

		if(input.isValid())
		{
			HashMap<ChemicalPair, GasStack> recipes = Recipe.CHEMICAL_INFUSER.get();

			for(Map.Entry<ChemicalPair, GasStack> entry : recipes.entrySet())
			{
				ChemicalPair key = (ChemicalPair)entry.getKey();

				if(key.meetsInput(input))
				{
					if(doRemove)
					{
						key.draw(leftTank, rightTank);
					}

					return entry.getValue().copy();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the Chemical Crystallizer ItemStack output of the defined GasTank input.
	 * @param gasTank - input GasTank
	 * @param removeGas - whether or not to use gas in the gas tank
	 * @return output ItemStack
	 */
	public static ItemStack getChemicalCrystallizerOutput(GasTank gasTank, boolean removeGas)
	{
		GasStack gas = gasTank.getGas();

		if(gas != null)
		{
			HashMap<GasStack, ItemStack> recipes = Recipe.CHEMICAL_CRYSTALLIZER.get();

			for(Map.Entry<GasStack, ItemStack> entry : recipes.entrySet())
			{
				GasStack key = (GasStack)entry.getKey();

				if(key != null && key.getGas() == gas.getGas() && gas.amount >= key.amount)
				{
					gasTank.draw(key.amount, removeGas);

					return entry.getValue().copy();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the Chemical Washer GasStack output of the defined GasTank input.
	 * @param gasTank - input GasTank
	 * @param removeGas - whether or not to use gas in the gas tank
	 * @return output GasStack
	 */
	public static GasStack getChemicalWasherOutput(GasTank gasTank, boolean removeGas)
	{
		GasStack gas = gasTank.getGas();

		if(gas != null)
		{
			HashMap<GasStack, GasStack> recipes = Recipe.CHEMICAL_WASHER.get();

			for(Map.Entry<GasStack, GasStack> entry : recipes.entrySet())
			{
				GasStack key = (GasStack)entry.getKey();

				if(key != null && key.getGas() == gas.getGas() && gas.amount >= key.amount)
				{
					gasTank.draw(key.amount, removeGas);

					return entry.getValue().copy();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the GasStack of the ItemStack in the parameters using a defined map.
	 * @param itemstack - input ItemStack
	 * @param stackDecrease - whether or not to decrease the input slot's stack size
	 * @return output GasStack
	 */
	public static GasStack getItemToGasOutput(ItemStack itemstack, boolean stackDecrease, HashMap<ItemStack, GasStack> recipes)
	{
		if(itemstack != null)
		{
			for(Map.Entry<ItemStack, GasStack> entry : recipes.entrySet())
			{
				ItemStack stack = (ItemStack)entry.getKey();

				if(StackUtils.equalsWildcard(stack, itemstack) && itemstack.stackSize >= stack.stackSize)
				{
					if(stackDecrease)
					{
						itemstack.stackSize -= stack.stackSize;
					}

					return entry.getValue().copy();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the output ChanceOutput of the ItemStack in the parameters.
	 * @param itemstack - input ItemStack
	 * @param stackDecrease - whether or not to decrease the input slot's stack size
	 * @param recipes - Map of recipes
	 * @return output ChanceOutput
	 */
	public static ChanceOutput getChanceOutput(ItemStack itemstack, boolean stackDecrease, Map<ItemStack, ChanceOutput> recipes)
	{
		if(itemstack != null)
		{
			for(Map.Entry entry : recipes.entrySet())
			{
				ItemStack stack = (ItemStack)entry.getKey();

				if(StackUtils.equalsWildcard(stack, itemstack) && itemstack.stackSize >= stack.stackSize)
				{
					if(stackDecrease)
					{
						itemstack.stackSize -= stack.stackSize;
					}

					return ((ChanceOutput)entry.getValue()).copy();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the output ItemStack of the ItemStack in the parameters.
	 * @param itemstack - input ItemStack
	 * @param stackDecrease - whether or not to decrease the input slot's stack size
	 * @param recipes - Map of recipes
	 * @return output ItemStack
	 */
	public static ItemStack getOutput(ItemStack itemstack, boolean stackDecrease, Map<ItemStack, ItemStack> recipes)
	{
		if(itemstack != null)
		{
			for(Map.Entry entry : recipes.entrySet())
			{
				ItemStack stack = (ItemStack)entry.getKey();

				if(StackUtils.equalsWildcard(stack, itemstack) && itemstack.stackSize >= stack.stackSize)
				{
					if(stackDecrease)
					{
						itemstack.stackSize -= stack.stackSize;
					}

					return ((ItemStack)entry.getValue()).copy();
				}
			}
		}

		return null;
	}
	/**
	 * Gets the output ItemStack of the AdvancedInput in the parameters.
	 * @param input - input AdvancedInput
	 * @param stackDecrease - whether or not to decrease the input slot's stack size
	 * @param recipes - Map of recipes
	 * @return output ItemStack
	 */
	public static ItemStack getOutput(AdvancedInput input, boolean stackDecrease, Map<AdvancedInput, ItemStack> recipes)
	{
		if(input != null && input.isValid())
		{
			for(Map.Entry<AdvancedInput, ItemStack> entry : recipes.entrySet())
			{
				if(entry.getKey().matches(input))
				{
					if(stackDecrease)
					{
						input.itemStack.stackSize -= entry.getKey().itemStack.stackSize;
					}

					return entry.getValue().copy();
				}
			}
		}

		return null;
	}

	/**
	 * Get the result of electrolysing a given fluid
	 * @param fluidTank - the FluidTank to electrolyse fluid from
	 */
	public static ChemicalPair getElectrolyticSeparatorOutput(FluidTank fluidTank, boolean doRemove)
	{
		FluidStack fluid = fluidTank.getFluid();

		if(fluid != null)
		{
			HashMap<FluidStack, ChemicalPair> recipes = Recipe.ELECTROLYTIC_SEPARATOR.get();

			for(Map.Entry<FluidStack, ChemicalPair> entry : recipes.entrySet())
			{
				FluidStack key = (FluidStack)entry.getKey();

				if(fluid.containsFluid(key))
				{
					fluidTank.drain(key.amount, doRemove);

					return entry.getValue().copy();
				}
			}
		}

		return null;
	}

	public static PressurizedRecipe getPRCOutput(ItemStack inputItem, FluidTank inputFluidTank, GasTank inputGasTank)
	{
		FluidStack inputFluid = inputFluidTank.getFluid();
		GasStack inputGas = inputGasTank.getGas();

		if(inputFluid != null && inputGas != null)
		{
			HashMap<PressurizedReactants, PressurizedRecipe> recipes = Recipe.PRESSURIZED_REACTION_CHAMBER.get();

			for(PressurizedRecipe recipe : recipes.values())
			{
				PressurizedReactants reactants = recipe.reactants;

				if(reactants.meetsInput(inputItem, inputFluid, inputGas))
				{
					return recipe.copy();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the output ItemStack of the ItemStack in the parameters.
	 * @param itemstack - input ItemStack
	 * @param recipes - Map of recipes
	 * @return whether the item can be used in a recipe
	 */
	public static boolean isInRecipe(ItemStack itemstack, Map<ItemStack, ItemStack> recipes)
	{
		if(itemstack != null)
		{
			for(Map.Entry entry : recipes.entrySet())
			{
				ItemStack stack = (ItemStack)entry.getKey();

				if(StackUtils.equalsWildcard(stack, itemstack))
				{
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isInPressurizedRecipe(ItemStack stack)
	{
		if(stack != null)
		{
			for(PressurizedReactants key : (Set<PressurizedReactants>)Recipe.PRESSURIZED_REACTION_CHAMBER.get().keySet())
			{
				if(key.containsType(stack))
				{
					return true;
				}
			}
		}

		return false;
	}

	public static enum Recipe
	{
		ENRICHMENT_CHAMBER(new HashMap<ItemStack, ItemStack>()),
		OSMIUM_COMPRESSOR(new HashMap<AdvancedInput, ItemStack>()),
		COMBINER(new HashMap<AdvancedInput, ItemStack>()),
		CRUSHER(new HashMap<ItemStack, ItemStack>()),
		PURIFICATION_CHAMBER(new HashMap<AdvancedInput, ItemStack>()),
		METALLURGIC_INFUSER(new HashMap<InfusionInput, InfusionOutput>()),
		CHEMICAL_INFUSER(new HashMap<ChemicalPair, GasStack>()),
		CHEMICAL_OXIDIZER(new HashMap<ItemStack, GasStack>()),
		CHEMICAL_INJECTION_CHAMBER(new HashMap<AdvancedInput, ItemStack>()),
		ELECTROLYTIC_SEPARATOR(new HashMap<FluidStack, ChemicalPair>()),
		PRECISION_SAWMILL(new HashMap<ItemStack, ChanceOutput>()),
		CHEMICAL_DISSOLUTION_CHAMBER(new HashMap<ItemStack, FluidStack>()),
		CHEMICAL_WASHER(new HashMap<GasStack, GasStack>()),
		CHEMICAL_CRYSTALLIZER(new HashMap<GasStack, ItemStack>()),
		PRESSURIZED_REACTION_CHAMBER(new HashMap<PressurizedReactants, PressurizedRecipe>());

		private HashMap recipes;

		private Recipe(HashMap map)
		{
			recipes = map;
		}

		public void put(Object input, Object output)
		{
			recipes.put(input, output);
		}

		public boolean containsRecipe(ItemStack input)
		{
			for(Object obj : get().entrySet())
			{
				if(obj instanceof Map.Entry)
				{
					Map.Entry entry = (Map.Entry)obj;

					if(entry.getKey() instanceof ItemStack)
					{
						ItemStack stack = (ItemStack)entry.getKey();

						if(StackUtils.equalsWildcard(stack, input))
						{
							return true;
						}
					}
					else if(entry.getKey() instanceof FluidStack)
					{
						if(((FluidStack)entry.getKey()).isFluidEqual(input))
						{
							return true;
						}
					}
					else if(entry.getKey() instanceof AdvancedInput)
					{
						ItemStack stack = ((AdvancedInput)entry.getKey()).itemStack;

						if(StackUtils.equalsWildcard(stack, input))
						{
							return true;
						}
					}
				}
			}

			return false;
		}

		public boolean containsRecipe(Fluid input)
		{
			for(Object obj : get().entrySet())
			{
				if(obj instanceof Map.Entry)
				{
					Map.Entry entry = (Map.Entry)obj;

					if(entry.getKey() instanceof FluidStack)
					{
						if(((FluidStack)entry.getKey()).getFluid() == input)
						{
							return true;
						}
					}
				}
			}

			return false;
		}

		public HashMap get()
		{
			return recipes;
		}
	}
}
