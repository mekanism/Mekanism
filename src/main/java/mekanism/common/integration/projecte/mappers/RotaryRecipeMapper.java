package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
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
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, IRecipe<?> iRecipe, INSSFakeGroupManager groupManager) {
        if (!(iRecipe instanceof RotaryRecipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        RotaryRecipe recipe = (RotaryRecipe) iRecipe;
        boolean handled = false;
        if (recipe.hasFluidToGas()) {
            for (FluidStack representation : recipe.getFluidInput().getRepresentations()) {
                GasStack output = recipe.getGasOutput(representation);
                if (!output.isEmpty()) {
                    IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                    ingredientHelper.put(representation);
                    if (ingredientHelper.addAsConversion(output)) {
                        handled = true;
                    }
                }
            }
        }
        if (recipe.hasGasToFluid()) {
            for (GasStack representation : recipe.getGasInput().getRepresentations()) {
                FluidStack output = recipe.getFluidOutput(representation);
                if (!output.isEmpty()) {
                    IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                    ingredientHelper.put(representation);
                    if (ingredientHelper.addAsConversion(output)) {
                        handled = true;
                    }
                }
            }
        }
        return handled;
    }
}