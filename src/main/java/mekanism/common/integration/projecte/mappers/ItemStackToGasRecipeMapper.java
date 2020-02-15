package mekanism.common.integration.projecte.mappers;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.common.integration.projecte.NSSGas;
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
public class ItemStackToGasRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekItemStackToGas";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism item stack to gas recipes. (Gas conversion, Oxidizing)";
    }

    @Override
    public boolean canHandle(IRecipeType<?> recipeType) {
        return recipeType == MekanismRecipeType.GAS_CONVERSION || recipeType == MekanismRecipeType.OXIDIZING;
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, IRecipe<?> iRecipe) {
        if (!(iRecipe instanceof ItemStackToGasRecipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        ItemStackToGasRecipe recipe = (ItemStackToGasRecipe) iRecipe;
        for (ItemStack representation : recipe.getInput().getRepresentations()) {
            Map<NormalizedSimpleStack, Integer> ingredientMap = new HashMap<>();
            ingredientMap.put(NSSItem.createItem(representation), representation.getCount());
            GasStack recipeOutput = recipe.getOutput(representation);
            mapper.addConversion(recipeOutput.getAmount(), NSSGas.createGas(recipeOutput), ingredientMap);
        }
        return true;
    }
}