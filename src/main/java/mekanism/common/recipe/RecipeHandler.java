package mekanism.common.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.infuse.InfuseType;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.inputs.ChemicalPairInput;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.inputs.GasInput;
import mekanism.common.recipe.inputs.InfusionInput;
import mekanism.common.recipe.inputs.IntegerInput;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.inputs.PressurizedReactants;
import mekanism.common.recipe.outputs.ChanceOutput;
import mekanism.common.recipe.outputs.ChemicalPairOutput;
import mekanism.common.recipe.outputs.InfusionOutput;
import mekanism.common.recipe.outputs.MachineOutput;
import mekanism.api.util.StackUtils;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.recipe.machines.AmbientGasRecipe;
import mekanism.common.recipe.machines.BasicMachineRecipe;
import mekanism.common.recipe.machines.ChanceMachineRecipe;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mekanism.common.recipe.machines.CombinerRecipe;
import mekanism.common.recipe.machines.CrusherRecipe;
import mekanism.common.recipe.machines.CrystallizerRecipe;
import mekanism.common.recipe.machines.DissolutionRecipe;
import mekanism.common.recipe.machines.EnrichmentRecipe;
import mekanism.common.recipe.machines.InjectionRecipe;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mekanism.common.recipe.machines.OsmiumCompressorRecipe;
import mekanism.common.recipe.machines.OxidationRecipe;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mekanism.common.recipe.machines.PurificationRecipe;
import mekanism.common.recipe.machines.SawmillRecipe;
import mekanism.common.recipe.machines.SeparatorRecipe;
import mekanism.common.recipe.machines.WasherRecipe;

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
	public static void addRecipe(Recipe recipeMap, MachineRecipe recipe)
	{
		recipeMap.put(recipe);
	}

	/**
	 * Add an Enrichment Chamber recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addEnrichmentChamberRecipe(ItemStack input, ItemStack output)
	{
		addRecipe(Recipe.ENRICHMENT_CHAMBER, new EnrichmentRecipe(input, output));
	}

	/**
	 * Add an Osmium Compressor recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addOsmiumCompressorRecipe(ItemStack input, ItemStack output)
	{
		addRecipe(Recipe.OSMIUM_COMPRESSOR, new OsmiumCompressorRecipe(input, output));
	}

	/**
	 * Add a Combiner recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addCombinerRecipe(ItemStack input, ItemStack output)
	{
		addRecipe(Recipe.COMBINER, new CombinerRecipe(input, output));
	}

	/**
	 * Add a Crusher recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addCrusherRecipe(ItemStack input, ItemStack output)
	{
		addRecipe(Recipe.CRUSHER, new CrusherRecipe(input, output));
	}

	/**
	 * Add a Purification Chamber recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addPurificationChamberRecipe(ItemStack input, ItemStack output)
	{
		addRecipe(Recipe.PURIFICATION_CHAMBER, new PurificationRecipe(input, output));
	}

	/**
	 * Add a Metallurgic Infuser recipe.
	 * @param infuse - which Infuse to use
	 * @param amount - how much of the Infuse to use
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addMetallurgicInfuserRecipe(InfuseType infuse, int amount, ItemStack input, ItemStack output)
	{
		addRecipe(Recipe.METALLURGIC_INFUSER, new MetallurgicInfuserRecipe(new InfusionInput(infuse, amount, input), output));
	}

	/**
	 * Add a Chemical Infuser recipe.
	 * @param leftInput - left GasStack to input
	 * @param rightInput - right GasStack to input
	 * @param output - output GasStack
	 */
	public static void addChemicalInfuserRecipe(GasStack leftInput, GasStack rightInput, GasStack output)
	{
		addRecipe(Recipe.CHEMICAL_INFUSER, new ChemicalInfuserRecipe(leftInput, rightInput, output));
	}

	/**
	 * Add a Chemical Oxidizer recipe.
	 * @param input - input ItemStack
	 * @param output - output GasStack
	 */
	public static void addChemicalOxidizerRecipe(ItemStack input, GasStack output)
	{
		addRecipe(Recipe.CHEMICAL_OXIDIZER, new OxidationRecipe(input, output));
	}

	/**
	 * Add a Chemical Injection Chamber recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addChemicalInjectionChamberRecipe(ItemStack input, String gasName, ItemStack output)
	{
		addRecipe(Recipe.CHEMICAL_INJECTION_CHAMBER, new InjectionRecipe(input, gasName, output));
	}

	/**
	 * Add an Electrolytic Separator recipe.
	 * @param fluid - FluidStack to electrolyze
	 * @param leftOutput - left gas to produce when the fluid is electrolyzed
	 * @param rightOutput - right gas to produce when the fluid is electrolyzed
	 */
	public static void addElectrolyticSeparatorRecipe(FluidStack fluid, GasStack leftOutput, GasStack rightOutput)
	{
		addRecipe(Recipe.ELECTROLYTIC_SEPARATOR, new SeparatorRecipe(fluid, leftOutput, rightOutput));
	}

	/**
	 * Add a Precision Sawmill recipe.
	 * @param input - input ItemStack
	 * @param primaryOutput - guaranteed output
	 * @param secondaryOutput - possible extra output
	 * @param chance - probability of obtaining extra output
	 */
	public static void addPrecisionSawmillRecipe(ItemStack input, ItemStack primaryOutput, ItemStack secondaryOutput, double chance)
	{
		addRecipe(Recipe.PRECISION_SAWMILL, new SawmillRecipe(input, primaryOutput, secondaryOutput, chance));
	}

	/**
	 * Add a Precision Sawmill recipe with no chance output
	 * @param input - input ItemStack
	 * @param primaryOutput - guaranteed output
	 */
	public static void addPrecisionSawmillRecipe(ItemStack input, ItemStack primaryOutput)
	{
		addRecipe(Recipe.PRECISION_SAWMILL, new SawmillRecipe(input, primaryOutput));
	}

	/**
	 * Add a Chemical Dissolution Chamber recipe.
	 * @param input - input ItemStack
	 * @param output - output GasStack
	 */
	public static void addChemicalDissolutionChamberRecipe(ItemStack input, GasStack output)
	{
		addRecipe(Recipe.CHEMICAL_DISSOLUTION_CHAMBER, new DissolutionRecipe(input, output));
	}

	/**
	 * Add a Chemical Washer recipe.
	 * @param input - input GasStack
	 * @param output - output GasStack
	 */
	public static void addChemicalWasherRecipe(GasStack input, GasStack output)
	{
		addRecipe(Recipe.CHEMICAL_WASHER, new WasherRecipe(input, output));
	}

	/**
	 * Add a Chemical Crystallizer recipe.
	 * @param input - input GasStack
	 * @param output - output ItemStack
	 */
	public static void addChemicalCrystallizerRecipe(GasStack input, ItemStack output)
	{
		addRecipe(Recipe.CHEMICAL_CRYSTALLIZER, new CrystallizerRecipe(input, output));
	}

	/**
	 * Add a Pressurized Reaction Chamber recipe.
	 * @param inputSolid - input ItemStack
	 * @param inputFluid - input FluidStack
	 * @param inputGas - input GasStack
	 * @param outputSolid - output ItemStack
	 * @param outputGas - output GasStack
	 * @param extraEnergy - extra energy needed by the recipe
	 * @param ticks - amount of ticks it takes for this recipe to complete
	 */
	public static void addPRCRecipe(ItemStack inputSolid, FluidStack inputFluid, GasStack inputGas, ItemStack outputSolid, GasStack outputGas, double extraEnergy, int ticks)
	{
		addRecipe(Recipe.PRESSURIZED_REACTION_CHAMBER, new PressurizedRecipe(inputSolid, inputFluid, inputGas, outputSolid, outputGas, extraEnergy, ticks));
	}

	public static void addAmbientGas(int dimensionID, String ambientGasName)
	{
		addRecipe(Recipe.AMBIENT_ACCUMULATOR, new AmbientGasRecipe(dimensionID, ambientGasName));
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
			HashMap<InfusionInput, MetallurgicInfuserRecipe> recipes = Recipe.METALLURGIC_INFUSER.get();

			MetallurgicInfuserRecipe recipe = recipes.get(infusion);

			if(recipe != null)
			{
				if(StackUtils.equalsWildcard(recipe.getInput().inputStack, infusion.inputStack) && infusion.inputStack.stackSize >= recipe.getInput().inputStack.stackSize)
				{
					if(infusion.infusionType == recipe.getInput().infusionType)
					{
						if(stackDecrease)
						{
							infusion.inputStack.stackSize -= recipe.getInput().inputStack.stackSize;
						}

						return recipe.getOutput().copy();
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
		ChemicalPairInput input = new ChemicalPairInput(leftTank.getGas(), rightTank.getGas());

		if(input.isValid())
		{
			HashMap<ChemicalPairInput, ChemicalInfuserRecipe> recipes = Recipe.CHEMICAL_INFUSER.get();

			ChemicalInfuserRecipe recipe = recipes.get(input);

			if(recipe != null)
			{
				ChemicalPairInput required = recipe.getInput();

				if(required.meetsInput(input))
				{
					if(doRemove)
					{
						required.draw(leftTank, rightTank);
					}

					return recipe.getOutput().output.copy();
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
		GasInput input = new GasInput(gasTank.getGas());

		if(input.ingredient != null)
		{
			HashMap<GasInput, CrystallizerRecipe> recipes = Recipe.CHEMICAL_CRYSTALLIZER.get();

			CrystallizerRecipe recipe = recipes.get(input);

			if(recipe != null)
			{
				GasStack key = recipe.getInput().ingredient;

				if(key != null && key.getGas() == input.ingredient.getGas() && input.ingredient.amount >= key.amount)
				{
					gasTank.draw(key.amount, removeGas);

					return recipe.getOutput().output.copy();
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
		GasInput input = new GasInput(gasTank.getGas());

		if(input.ingredient != null)
		{
			HashMap<GasInput, WasherRecipe> recipes = Recipe.CHEMICAL_WASHER.get();

			WasherRecipe recipe = recipes.get(input);

			if(recipe != null)
			{
				GasStack key = recipe.getInput().ingredient;

				if(key != null && key.getGas() == input.ingredient.getGas() && input.ingredient.amount >= key.amount)
				{
					gasTank.draw(key.amount, removeGas);

					return recipe.getOutput().output.copy();
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
	public static GasStack getDissolutionOutput(ItemStack itemstack, boolean stackDecrease)
	{
		if(itemstack != null)
		{
			ItemStackInput input = new ItemStackInput(itemstack);

			HashMap<ItemStackInput, DissolutionRecipe> recipes = Recipe.CHEMICAL_DISSOLUTION_CHAMBER.get();

			DissolutionRecipe recipe = recipes.get(input);

			if(recipe != null)
			{
				ItemStack stack = recipe.getInput().ingredient;

				if(StackUtils.equalsWildcard(stack, itemstack) && itemstack.stackSize >= stack.stackSize)
				{
					if(stackDecrease)
					{
						itemstack.stackSize -= stack.stackSize;
					}

					return recipe.getOutput().output.copy();
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
	public static GasStack getOxidizerOutput(ItemStack itemstack, boolean stackDecrease)
	{
		if(itemstack != null)
		{
			ItemStackInput input = new ItemStackInput(itemstack);

			HashMap<ItemStackInput, OxidationRecipe> recipes = Recipe.CHEMICAL_OXIDIZER.get();

			OxidationRecipe recipe = recipes.get(input);

			if(recipe != null)
			{
				ItemStack stack = recipe.getInput().ingredient;

				if(StackUtils.equalsWildcard(stack, itemstack) && itemstack.stackSize >= stack.stackSize)
				{
					if(stackDecrease)
					{
						itemstack.stackSize -= stack.stackSize;
					}

					return recipe.getOutput().output.copy();
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
	public static ChanceOutput getChanceOutput(ItemStack itemstack, boolean stackDecrease, Map<ItemStackInput, ? extends ChanceMachineRecipe> recipes)
	{
		if(itemstack != null)
		{
			ItemStackInput input = new ItemStackInput(itemstack);

			ChanceMachineRecipe recipe = recipes.get(input);

			if(recipe != null)
			{
				ItemStack stack = recipe.getInput().ingredient;

				if(StackUtils.equalsWildcard(stack, itemstack) && itemstack.stackSize >= stack.stackSize)
				{
					if(stackDecrease)
					{
						itemstack.stackSize -= stack.stackSize;
					}

					return recipe.getOutput().copy();
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
	public static ItemStack getOutput(ItemStack itemstack, boolean stackDecrease, Map<ItemStackInput, ? extends BasicMachineRecipe> recipes)
	{
		if(itemstack != null)
		{
			ItemStackInput input = new ItemStackInput(itemstack);

			BasicMachineRecipe recipe = recipes.get(input);

			if(recipe != null)
			{
				ItemStack stack = recipe.getInput().ingredient;

				if(StackUtils.equalsWildcard(stack, itemstack) && itemstack.stackSize >= stack.stackSize)
				{
					if(stackDecrease)
					{
						itemstack.stackSize -= stack.stackSize;
					}

					return recipe.getOutput().output.copy();
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
	public static ItemStack getOutput(AdvancedMachineInput input, boolean stackDecrease, Map<AdvancedMachineInput, ? extends AdvancedMachineRecipe> recipes)
	{
		if(input != null && input.isValid())
		{
			AdvancedMachineRecipe recipe = recipes.get(input);

			if(recipe != null && recipe.getInput().matches(input))
			{
				if(stackDecrease)
				{
					input.itemStack.stackSize -= recipe.getInput().itemStack.stackSize;
				}

				return recipe.getOutput().output.copy();
			}
		}

		return null;
	}

	/**
	 * Get the result of electrolysing a given fluid
	 * @param fluidTank - the FluidTank to electrolyse fluid from
	 */
	public static ChemicalPairOutput getElectrolyticSeparatorOutput(FluidTank fluidTank, boolean doRemove)
	{
		FluidStack fluid = fluidTank.getFluid();

		if(fluid != null)
		{
			FluidInput input = new FluidInput(fluid);

			HashMap<FluidInput, SeparatorRecipe> recipes = Recipe.ELECTROLYTIC_SEPARATOR.get();

			SeparatorRecipe recipe = recipes.get(input);

			if(recipe != null)
			{
				FluidStack key = recipe.getInput().ingredient;

				if(fluid.containsFluid(key))
				{
					fluidTank.drain(key.amount, doRemove);

					return recipe.getOutput().copy();
				}
			}
		}

		return null;
	}

	public static PressurizedRecipe getPRCOutput(ItemStack inputItem, FluidTank inputFluidTank, GasTank inputGasTank)
	{
		FluidStack inputFluid = inputFluidTank.getFluid();
		GasStack inputGas = inputGasTank.getGas();

		if(inputItem != null && inputFluid != null && inputGas != null)
		{
			PressurizedReactants input = new PressurizedReactants(inputItem, inputFluid, inputGas);

			HashMap<PressurizedReactants, PressurizedRecipe> recipes = Recipe.PRESSURIZED_REACTION_CHAMBER.get();

			PressurizedRecipe recipe = recipes.get(input);

			if(recipe.getInput().meets(input))
			{
				return recipe.copy();
			}
		}

		return null;
	}

	public static GasStack getDimensionGas(Integer dimensionID)
	{
		HashMap<IntegerInput, AmbientGasRecipe> recipes = Recipe.AMBIENT_ACCUMULATOR.get();
		return recipes.get(new IntegerInput(dimensionID)).getOutput().output;
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
		ENRICHMENT_CHAMBER(new HashMap<ItemStackInput, EnrichmentRecipe>()),
		OSMIUM_COMPRESSOR(new HashMap<AdvancedMachineInput, OsmiumCompressorRecipe>()),
		COMBINER(new HashMap<AdvancedMachineInput, CombinerRecipe>()),
		CRUSHER(new HashMap<ItemStackInput, CrusherRecipe>()),
		PURIFICATION_CHAMBER(new HashMap<AdvancedMachineInput, PurificationRecipe>()),
		METALLURGIC_INFUSER(new HashMap<InfusionInput, MetallurgicInfuserRecipe>()),
		CHEMICAL_INFUSER(new HashMap<ChemicalPairInput, ChemicalInfuserRecipe>()),
		CHEMICAL_OXIDIZER(new HashMap<ItemStackInput, OxidationRecipe>()),
		CHEMICAL_INJECTION_CHAMBER(new HashMap<AdvancedMachineInput, InjectionRecipe>()),
		ELECTROLYTIC_SEPARATOR(new HashMap<FluidInput, SeparatorRecipe>()),
		PRECISION_SAWMILL(new HashMap<ItemStackInput, SawmillRecipe>()),
		CHEMICAL_DISSOLUTION_CHAMBER(new HashMap<ItemStackInput, DissolutionRecipe>()),
		CHEMICAL_WASHER(new HashMap<GasInput, WasherRecipe>()),
		CHEMICAL_CRYSTALLIZER(new HashMap<GasInput, CrystallizerRecipe>()),
		PRESSURIZED_REACTION_CHAMBER(new HashMap<PressurizedReactants, PressurizedRecipe>()),
		AMBIENT_ACCUMULATOR(new HashMap<IntegerInput, AmbientGasRecipe>());

		private HashMap recipes;

		private Recipe(HashMap<? extends MachineInput, ? extends MachineRecipe> map)
		{
			recipes = map;
		}

		public void put(MachineRecipe<? extends MachineInput, ? extends MachineOutput> recipe)
		{
			recipes.put(recipe.getInput(), recipe);
		}

		public boolean containsRecipe(ItemStack input)
		{
			for(Object obj : get().entrySet())
			{
				if(obj instanceof Map.Entry)
				{
					Map.Entry entry = (Map.Entry)obj;

					if(entry.getKey() instanceof ItemStackInput)
					{
						ItemStack stack = ((ItemStackInput)entry.getKey()).ingredient;

						if(StackUtils.equalsWildcard(stack, input))
						{
							return true;
						}
					}
					else if(entry.getKey() instanceof FluidInput)
					{
						if(((FluidInput)entry.getKey()).ingredient.isFluidEqual(input))
						{
							return true;
						}
					}
					else if(entry.getKey() instanceof AdvancedMachineInput)
					{
						ItemStack stack = ((AdvancedMachineInput)entry.getKey()).itemStack;

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
