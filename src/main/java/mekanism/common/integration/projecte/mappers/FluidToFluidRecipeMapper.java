package mekanism.common.integration.projecte.mappers;

import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.neoforged.neoforge.fluids.FluidStack;

@RecipeTypeMapper
public class FluidToFluidRecipeMapper extends TypedMekanismRecipeMapper<FluidToFluidRecipe> {

    public FluidToFluidRecipeMapper() {
        super(FluidToFluidRecipe.class, MekanismRecipeType.EVAPORATING);
    }

    @Override
    public String getName() {
        return "MekFluidToFluid";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism evaporating recipes.";
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, FluidToFluidRecipe recipe) {
        boolean handled = false;
        for (FluidStack representation : recipe.getInput().getRepresentations()) {
            FluidStack output = recipe.getOutput(representation);
            if (!output.isEmpty()) {
                IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                ingredientHelper.put(representation);
                if (ingredientHelper.addAsConversion(output)) {
                    handled = true;
                }
            }
        }
        return handled;
    }
}