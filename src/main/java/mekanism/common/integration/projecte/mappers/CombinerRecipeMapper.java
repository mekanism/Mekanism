package mekanism.common.integration.projecte.mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;

@RecipeTypeMapper
public class CombinerRecipeMapper implements IRecipeTypeMapper {

	@Override
	public String getName() {
		return "MekCombiner";
	}

	@Override
	public String getDescription() {
		return "Maps Mekanism combiner recipes.";
	}

	@Override
	public boolean canHandle(IRecipeType<?> recipeType) {
		return recipeType == MekanismRecipeType.COMBINING;
	}

	@Override
	public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, IRecipe<?> iRecipe) {
		if (!(iRecipe instanceof CombinerRecipe)) {
			//Double check that we have a type of recipe we know how to handle
			return false;
		}
		CombinerRecipe recipe = (CombinerRecipe) iRecipe;
		List<@NonNull ItemStack> mainRepresentations = recipe.getMainInput().getRepresentations();
		List<@NonNull ItemStack> extraRepresentations = recipe.getExtraInput().getRepresentations();
		for (ItemStack mainRepresentation : mainRepresentations) {
			NormalizedSimpleStack nssMain = NSSItem.createItem(mainRepresentation);
			for (ItemStack extraRepresentation : extraRepresentations) {
				Map<NormalizedSimpleStack, Integer> ingredientMap = new HashMap<>();
				ingredientMap.put(nssMain, mainRepresentation.getCount());
				ingredientMap.put(NSSItem.createItem(extraRepresentation), extraRepresentation.getCount());
				ItemStack recipeOutput = recipe.getOutput(mainRepresentation, extraRepresentation);
				mapper.addConversion(recipeOutput.getCount(), NSSItem.createItem(recipeOutput), ingredientMap);
			}
		}
		return true;
	}
}