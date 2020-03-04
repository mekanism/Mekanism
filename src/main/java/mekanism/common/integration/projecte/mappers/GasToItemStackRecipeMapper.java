package mekanism.common.integration.projecte.mappers;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToItemStackRecipe;
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
public class GasToItemStackRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekGasToItemStack";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism crystallizing recipes.";
    }

    @Override
    public boolean canHandle(IRecipeType<?> recipeType) {
        return recipeType == MekanismRecipeType.CRYSTALLIZING;
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, IRecipe<?> iRecipe) {
        if (!(iRecipe instanceof GasToItemStackRecipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        boolean handled = false;
        GasToItemStackRecipe recipe = (GasToItemStackRecipe) iRecipe;
        for (GasStack representation : recipe.getInput().getRepresentations()) {
            Map<NormalizedSimpleStack, Integer> ingredientMap = new HashMap<>();
            ingredientMap.put(NSSGas.createGas(representation), representation.getAmount());
            ItemStack recipeOutput = recipe.getOutput(representation);
            if (!recipeOutput.isEmpty()) {
                mapper.addConversion(recipeOutput.getCount(), NSSItem.createItem(recipeOutput), ingredientMap);
                handled = true;
            }
        }
        return handled;
    }
}