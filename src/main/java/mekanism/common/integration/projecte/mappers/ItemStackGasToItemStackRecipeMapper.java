package mekanism.common.integration.projecte.mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.common.integration.projecte.NSSGas;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;

@RecipeTypeMapper
public class ItemStackGasToItemStackRecipeMapper implements IRecipeTypeMapper {

	@Override
	public String getName() {
		return "MekItemStackGasToItemStack";
	}

	@Override
	public String getDescription() {
		return "Maps Mekanism Machine recipes that go from item, gas to item. (Compressing, Purifying, Injecting)";
	}

	@Override
	public boolean canHandle(IRecipeType<?> recipeType) {
		return recipeType == MekanismRecipeType.COMPRESSING || recipeType == MekanismRecipeType.PURIFYING || recipeType == MekanismRecipeType.INJECTING;
	}

	@Override
	public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, IRecipe<?> iRecipe) {
		if (!(iRecipe instanceof ItemStackGasToItemStackRecipe)) {
			//Double check that we have a type of recipe we know how to handle
			return false;
		}
		ItemStackGasToItemStackRecipe recipe = (ItemStackGasToItemStackRecipe) iRecipe;
		List<@NonNull ItemStack> itemRepresentations = recipe.getItemInput().getRepresentations();
		List<@NonNull GasStack> gasRepresentations = recipe.getGasInput().getRepresentations();
		int gasMultiplier = TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED * TileEntityAdvancedElectricMachine.BASE_GAS_PER_TICK;
		for (ItemStack itemRepresentation : itemRepresentations) {
			NormalizedSimpleStack nssItem = NSSItem.createItem(itemRepresentation);
			for (GasStack gasRepresentation : gasRepresentations) {
				Map<NormalizedSimpleStack, Integer> ingredientMap = new HashMap<>();
				ingredientMap.put(nssItem, itemRepresentation.getCount());
				ingredientMap.put(NSSGas.createGas(gasRepresentation), gasRepresentation.getAmount() * gasMultiplier);
				ItemStack output = recipe.getOutput(itemRepresentation, gasRepresentation);
				mapper.addConversion(output.getCount(), NSSItem.createItem(output), ingredientMap);
			}
		}
		return true;
	}
}