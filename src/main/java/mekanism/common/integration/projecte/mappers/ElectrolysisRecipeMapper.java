package mekanism.common.integration.projecte.mappers;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ElectrolysisRecipe.ElectrolysisRecipeOutput;
import mekanism.api.recipes.basic.BasicElectrolysisRecipe;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.integration.projecte.NSSChemical;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

@RecipeTypeMapper
public class ElectrolysisRecipeMapper extends TypedMekanismRecipeMapper<ElectrolysisRecipe> {

    public ElectrolysisRecipeMapper() {
        super(MekanismConfigTranslations.PE_MAPPER_ELECTROLYSIS, ElectrolysisRecipe.class, MekanismRecipeType.SEPARATING);
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ElectrolysisRecipe recipe, MekFakeGroupHelper fakeGroupHelper) {
        if (OPTIMIZE_BASIC && recipe instanceof BasicElectrolysisRecipe basicRecipe) {
            //This will be the case for the majority of our recipes
            ChemicalStack leftOutput = basicRecipe.getLeftChemicalOutput();
            ChemicalStack rightOutput = basicRecipe.getRightChemicalOutput();
            if (leftOutput.isEmpty() || rightOutput.isEmpty()) {//Shouldn't be the case, but validate it just in case
                return false;
            }
            return addConversions(mapper, new ElectrolysisRecipeOutput(leftOutput, rightOutput), fakeGroupHelper.forIngredient(recipe.getInput()));
        }
        return addConversions(mapper, recipe.getInput(), recipe::getOutput, output -> output.left().isEmpty() || output.right().isEmpty(),
              fakeGroupHelper::forFluids, null, ElectrolysisRecipeMapper::addConversions);
    }

    private static boolean addConversions(IMappingCollector<NormalizedSimpleStack, Long> mapper, ElectrolysisRecipeOutput output, Object2IntMap<NormalizedSimpleStack> inputs) {
        ChemicalStack leftOutput = output.left();
        ChemicalStack rightOutput = output.right();
        if (inputs.isEmpty() || leftOutput.getAmount() > Integer.MAX_VALUE || rightOutput.getAmount() > Integer.MAX_VALUE) {
            return false;
        }
        //Use bitwise or as we want to try and add both of them
        return addConversion(mapper, leftOutput, forIngredients(
              inputs,
              NSSChemical.createChemical(rightOutput), (int) -rightOutput.getAmount()
        )) | addConversion(mapper, rightOutput, forIngredients(
              inputs,
              NSSChemical.createChemical(leftOutput), (int) -leftOutput.getAmount()
        ));
    }
}