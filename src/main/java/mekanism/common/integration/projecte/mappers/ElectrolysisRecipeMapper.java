package mekanism.common.integration.projecte.mappers;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.common.integration.projecte.NSSGas;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

@RecipeTypeMapper
public class ElectrolysisRecipeMapper implements IRecipeTypeMapper {

	@Override
	public String getName() {
		return "MekElectrolysis";
	}

	@Override
	public String getDescription() {
		return "Maps Mekanism electrolytic separator recipes.";
	}

	@Override
	public boolean canHandle(IRecipeType<?> recipeType) {
		return recipeType == MekanismRecipeType.SEPARATING;
	}

	@Override
	public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, IRecipe<?> iRecipe) {
		if (!(iRecipe instanceof ElectrolysisRecipe)) {
			//Double check that we have a type of recipe we know how to handle
			return false;
		}
		ElectrolysisRecipe recipe = (ElectrolysisRecipe) iRecipe;
		FluidStackIngredient input = recipe.getInput();
		for (FluidStack representation : input.getRepresentations()) {
			NormalizedSimpleStack nssInput = NSSFluid.createFluid(representation);
			Pair<@NonNull GasStack, @NonNull GasStack> output = recipe.getOutput(representation);
			GasStack leftOutput = output.getLeft();
			GasStack rightOutput = output.getRight();
			NormalizedSimpleStack nssLeftOutput = NSSGas.createGas(leftOutput);
			NormalizedSimpleStack nssRightOutput = NSSGas.createGas(rightOutput);
			//Add trying to calculate left output (using it as if we needed negative of right output)
			Map<NormalizedSimpleStack, Integer> ingredientMap = new HashMap<>();
			ingredientMap.put(nssInput, representation.getAmount());
			ingredientMap.put(nssRightOutput, -rightOutput.getAmount());
			mapper.addConversion(leftOutput.getAmount(), nssLeftOutput, ingredientMap);
			//Add trying to calculate right output (using it as if we needed negative of left output)
			ingredientMap = new HashMap<>();
			ingredientMap.put(nssInput, representation.getAmount());
			ingredientMap.put(nssLeftOutput, -leftOutput.getAmount());
			mapper.addConversion(rightOutput.getAmount(), nssRightOutput, ingredientMap);
		}
		return true;
	}
}