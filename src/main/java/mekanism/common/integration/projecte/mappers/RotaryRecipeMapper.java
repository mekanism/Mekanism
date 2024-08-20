package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.neoforged.neoforge.fluids.FluidStack;

@RecipeTypeMapper
public class RotaryRecipeMapper extends TypedMekanismRecipeMapper<RotaryRecipe> {

    public RotaryRecipeMapper() {
        super(RotaryRecipe.class, MekanismRecipeType.ROTARY);
    }

    @Override
    public String getName() {
        return "MekRotary";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism rotary condensentrator recipes.";
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, RotaryRecipe recipe) {
        boolean handled = false;
        if (recipe.hasFluidToChemical()) {
            for (FluidStack representation : recipe.getFluidInput().getRepresentations()) {
                ChemicalStack output = recipe.getChemicalOutput(representation);
                if (!output.isEmpty()) {
                    IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                    ingredientHelper.put(representation);
                    if (ingredientHelper.addAsConversion(output)) {
                        handled = true;
                    }
                }
            }
        }
        if (recipe.hasChemicalToFluid()) {
            for (ChemicalStack representation : recipe.getChemicalInput().getRepresentations()) {
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