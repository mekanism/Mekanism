package mekanism.common.integration.projecte.mappers;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.common.integration.projecte.NSSInfuseType;
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
public class ItemStackToInfuseTypeRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekItemStackToInfuseType";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism item stack to infuse type conversion recipes.";
    }

    @Override
    public boolean canHandle(IRecipeType<?> recipeType) {
        return recipeType == MekanismRecipeType.INFUSION_CONVERSION;
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, IRecipe<?> iRecipe) {
        if (!(iRecipe instanceof ItemStackToInfuseTypeRecipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        ItemStackToInfuseTypeRecipe recipe = (ItemStackToInfuseTypeRecipe) iRecipe;
        for (ItemStack representation : recipe.getInput().getRepresentations()) {
            Map<NormalizedSimpleStack, Integer> ingredientMap = new HashMap<>();
            ingredientMap.put(NSSItem.createItem(representation), representation.getCount());
            InfusionStack recipeOutput = recipe.getOutput(representation);
            mapper.addConversion(recipeOutput.getAmount(), NSSInfuseType.createInfuseType(recipeOutput), ingredientMap);
        }
        return true;
    }
}