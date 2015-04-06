package mekanism.api.recipe;

import java.lang.reflect.Method;

import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfuseType;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * SET TO BE REMOVED NEXT MINOR MEKANISM VERSION, PLEASE USE IMC INSTEAD.
 * Use this handy class to add recipes to Mekanism machinery.
 * @author AidanBrady
 *
 */
@Deprecated
public final class RecipeHelper
{
	/**
	 * Add an Enrichment Chamber recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addEnrichmentChamberRecipe(ItemStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addEnrichmentChamberRecipe", ItemStack.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add an Osmium Compressor recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addOsmiumCompressorRecipe(ItemStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addOsmiumCompressorRecipe", ItemStack.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Combiner recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addCombinerRecipe(ItemStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addCombinerRecipe", ItemStack.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Crusher recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addCrusherRecipe(ItemStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addCrusherRecipe", ItemStack.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Purification Chamber recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addPurificationChamberRecipe(ItemStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addPurificationChamberRecipe", ItemStack.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Chemical Oxidizer recipe.
	 * @param input - input ItemStack
	 * @param output - output GasStack
	 */
	public static void addChemicalOxidizerRecipe(ItemStack input, GasStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addChemicalOxidizerRecipe", ItemStack.class, GasStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Chemical Infuser recipe.
	 * @param leftInput - left input GasStack
	 * @param rightInput - right input GasStack
	 * @param output - output GasStack
	 */
	public static void addChemicalInfuserRecipe(GasStack leftInput, GasStack rightInput, GasStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addChemicalInfuserRecipe", GasStack.class, GasStack.class, GasStack.class);
			m.invoke(null, leftInput, rightInput, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
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
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addPrecisionSawmillRecipe", ItemStack.class, ItemStack.class, ItemStack.class, Double.TYPE);
			m.invoke(null, input, primaryOutput, secondaryOutput, chance);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Precision Sawmill recipe with no chance output
	 * @param input - input ItemStack
	 * @param primaryOutput - guaranteed output
	 */
	public static void addPrecisionSawmillRecipe(ItemStack input, ItemStack primaryOutput)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addPrecisionSawmillRecipe", ItemStack.class, ItemStack.class);
			m.invoke(null, input, primaryOutput);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Chemical Injection Chamber recipe.
	 * @param input - input AdvancedInput
	 * @param output - output ItemStack
	 */
	public static void addChemicalInjectionChamberRecipe(ItemStack input, String gasName, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addChemicalInjectionChamberRecipe", ItemStack.class, String.class, ItemStack.class);
			m.invoke(null, input, gasName, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add an Electrolytic Separator recipe.
	 * @param input - input FluidStack
	 * @param energy - required energy
	 * @param leftOutput - left output GasStack
	 * @param rightOutput - right output GasStack
	 */
	public static void addElectrolyticSeparatorRecipe(FluidStack input, double energy, GasStack leftOutput, GasStack rightOutput)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addElectrolyticSeparatorRecipe", FluidStack.class, Double.TYPE, GasStack.class, GasStack.class);
			m.invoke(null, input, energy, leftOutput, rightOutput);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Chemical Dissolution Chamber recipe.
	 * @param input - input ItemStack
	 * @param output - output GasStack
	 */
	public static void addChemicalDissolutionChamberRecipe(ItemStack input, GasStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addChemicalDissolutionChamberRecipe", ItemStack.class, GasStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Chemical Washer recipe.
	 * @param input - input GasStack
	 * @param output - output GasStack
	 */
	public static void addChemicalWasherRecipe(GasStack input, GasStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addChemicalWasherRecipe", GasStack.class, GasStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Chemical Crystallizer recipe.
	 * @param input - input GasStack
	 * @param output - output ItemStack
	 */
	public static void addChemicalCrystallizerRecipe(GasStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addChemicalCrystallizerRecipe", GasStack.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Metallurgic Infuser recipe.
	 * @param infuse - which Infuse to use
	 * @param amount - how much Infuse to use
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addMetallurgicInfuserRecipe(InfuseType infuse, int amount, ItemStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addMetallurgicInfuserRecipe", InfuseType.class, Integer.TYPE, ItemStack.class, ItemStack.class);
			m.invoke(null, infuse, amount, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
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
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addPRCRecipe", ItemStack.class, FluidStack.class, GasStack.class, ItemStack.class, GasStack.class, Double.TYPE, Integer.TYPE);
			m.invoke(null, inputSolid, inputFluid, inputGas, outputSolid, outputGas, extraEnergy, ticks);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}
	
	/**
	 * Add a Solar Evaporation Plant recipe.
	 * @param input - input GasStack
	 * @param output - output GasStack
	 */
	public static void addSolarEvaporationRecipe(FluidStack input, FluidStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addSolarEvaporationRecipe", FluidStack.class, FluidStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}
	
	/**
	 * Add a Solar Neutron Activator recipe.
	 * @param input - input GasStack
	 * @param output - output GasStack
	 */
	public static void addSolarNeutronRecipe(GasStack input, GasStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addSolarEvaporationRecipe", GasStack.class, GasStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}
}
