package mekanism.common.integration.projecte.mappers;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.common.integration.projecte.NSSGas;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;

@RecipeTypeMapper
public class GasToGasRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekGasToGas";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism activating recipes.";
    }

    @Override
    public boolean canHandle(IRecipeType<?> recipeType) {
        return recipeType == MekanismRecipeType.ACTIVATING;
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, IRecipe<?> iRecipe) {
        if (!(iRecipe instanceof GasToGasRecipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        GasToGasRecipe recipe = (GasToGasRecipe) iRecipe;
        for (GasStack representation : recipe.getInput().getRepresentations()) {
            Map<NormalizedSimpleStack, Integer> ingredientMap = new HashMap<>();
            ingredientMap.put(NSSGas.createGas(representation), representation.getAmount());
            GasStack recipeOutput = recipe.getOutput(representation);
            mapper.addConversion(recipeOutput.getAmount(), NSSGas.createGas(recipeOutput), ingredientMap);
        }
        return true;
    }
}