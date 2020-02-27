package mekanism.common.integration.projecte.mappers;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
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

@RecipeTypeMapper
public class RotaryRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekRotary";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism rotary condensentrator recipes.";
    }

    @Override
    public boolean canHandle(IRecipeType<?> recipeType) {
        return recipeType == MekanismRecipeType.ROTARY;
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, IRecipe<?> iRecipe) {
        if (!(iRecipe instanceof RotaryRecipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        RotaryRecipe recipe = (RotaryRecipe) iRecipe;
        boolean handled = false;
        if (recipe.hasFluidToGas()) {
            handled = true;
            for (FluidStack representation : recipe.getFluidInput().getRepresentations()) {
                Map<NormalizedSimpleStack, Integer> ingredientMap = new HashMap<>();
                ingredientMap.put(NSSFluid.createFluid(representation), representation.getAmount());
                GasStack recipeOutput = recipe.getGasOutput(representation);
                mapper.addConversion(recipeOutput.getAmount(), NSSGas.createGas(recipeOutput), ingredientMap);
            }
        }
        if (recipe.hasGasToFluid()) {
            handled = true;
            for (GasStack representation : recipe.getGasInput().getRepresentations()) {
                Map<NormalizedSimpleStack, Integer> ingredientMap = new HashMap<>();
                ingredientMap.put(NSSGas.createGas(representation), representation.getAmount());
                FluidStack recipeOutput = recipe.getFluidOutput(representation);
                mapper.addConversion(recipeOutput.getAmount(), NSSFluid.createFluid(recipeOutput), ingredientMap);
            }
        }
        return handled;
    }
}