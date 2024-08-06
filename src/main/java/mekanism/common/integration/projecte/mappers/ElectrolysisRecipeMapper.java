package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ElectrolysisRecipe.ElectrolysisRecipeOutput;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.integration.projecte.NSSChemical;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.neoforged.neoforge.fluids.FluidStack;

@RecipeTypeMapper
public class ElectrolysisRecipeMapper extends TypedMekanismRecipeMapper<ElectrolysisRecipe> {

    public ElectrolysisRecipeMapper() {
        super(ElectrolysisRecipe.class, MekanismRecipeType.SEPARATING);
    }

    @Override
    public String getName() {
        return "MekElectrolysis";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism electrolytic separator recipes.";
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ElectrolysisRecipe recipe) {
        boolean handled = false;
        FluidStackIngredient input = recipe.getInput();
        for (FluidStack representation : input.getRepresentations()) {
            ElectrolysisRecipeOutput output = recipe.getOutput(representation);
            ChemicalStack leftOutput = output.left();
            ChemicalStack rightOutput = output.right();
            if (!leftOutput.isEmpty() && !rightOutput.isEmpty()) {
                NormalizedSimpleStack nssInput = NSSFluid.createFluid(representation);
                NormalizedSimpleStack nssLeftOutput = NSSChemical.createChemical(leftOutput);
                NormalizedSimpleStack nssRightOutput = NSSChemical.createChemical(rightOutput);
                //Add trying to calculate left output (using it as if we needed negative of right output)
                IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                ingredientHelper.put(nssInput, representation.getAmount());
                ingredientHelper.put(nssRightOutput, -rightOutput.getAmount());
                if (ingredientHelper.addAsConversion(nssLeftOutput, leftOutput.getAmount())) {
                    handled = true;
                }
                //Add trying to calculate right output (using it as if we needed negative of left output)
                ingredientHelper.resetHelper();
                ingredientHelper.put(nssInput, representation.getAmount());
                ingredientHelper.put(nssLeftOutput, -leftOutput.getAmount());
                if (ingredientHelper.addAsConversion(nssRightOutput, rightOutput.getAmount())) {
                    handled = true;
                }
            }
        }
        return handled;
    }
}