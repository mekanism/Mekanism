package mekanism.common.integration.projecte.mappers;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraftforge.fluids.FluidStack;

@RecipeTypeMapper
public class FluidToFluidRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekFluidToFluid";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism evaporating recipes.";
    }

    @Override
    public boolean canHandle(IRecipeType<?> recipeType) {
        return recipeType == MekanismRecipeType.EVAPORATING;
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, IRecipe<?> iRecipe) {
        if (!(iRecipe instanceof FluidToFluidRecipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        boolean handled = false;
        FluidToFluidRecipe recipe = (FluidToFluidRecipe) iRecipe;
        for (FluidStack representation : recipe.getInput().getRepresentations()) {
            Map<NormalizedSimpleStack, Integer> ingredientMap = new HashMap<>();
            ingredientMap.put(NSSFluid.createFluid(representation), representation.getAmount());
            FluidStack recipeOutput = recipe.getOutput(representation);
            if (!recipeOutput.isEmpty()) {
                mapper.addConversion(recipeOutput.getAmount(), NSSFluid.createFluid(recipeOutput), ingredientMap);
                handled = true;
            }
        }
        return handled;
    }
}