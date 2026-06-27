package mekanism.common.integration.projecte.mappers;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ElectrolysisRecipe.ElectrolysisRecipeOutput;
import mekanism.api.recipes.basic.BasicElectrolysisRecipe;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.integration.projecte.NSSChemical;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.util.context.ContextMap;

@RecipeTypeMapper
public class ElectrolysisRecipeMapper extends TypedMekanismRecipeMapper<ElectrolysisRecipe> {

    public ElectrolysisRecipeMapper() {
        super(MekanismConfigTranslations.PE_MAPPER_ELECTROLYSIS, ElectrolysisRecipe.class, MekanismRecipeType.SEPARATING);
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ElectrolysisRecipe recipe, MekFakeGroupHelper fakeGroupHelper, ContextMap contextMap) {
        if (OPTIMIZE_BASIC && recipe instanceof BasicElectrolysisRecipe basicRecipe) {
            //This will be the case for the majority of our recipes
            ChemicalStackTemplate leftOutput = basicRecipe.getLeftChemicalOutput();
            ChemicalStackTemplate rightOutput = basicRecipe.getRightChemicalOutput();
            return addConversions(mapper, new ElectrolysisRecipeOutput(leftOutput, rightOutput), fakeGroupHelper.forIngredient(recipe.getInput(), contextMap));
        }
        return addConversions(mapper, recipe.getInput(), recipe::getOutput, fakeGroupHelper::forFluids, ElectrolysisRecipeMapper::addConversions);
    }

    private static boolean addConversions(IMappingCollector<NormalizedSimpleStack, Long> mapper, ElectrolysisRecipeOutput output, Object2IntMap<NormalizedSimpleStack> inputs) {
        ChemicalStackTemplate leftOutput = output.left();
        ChemicalStackTemplate rightOutput = output.right();
        if (inputs.isEmpty()) {
            return false;
        }
        //Use bitwise or as we want to try and add both of them
        return addConversion(mapper, leftOutput, forIngredients(
              inputs,
              NSSChemical.createChemical(rightOutput), -rightOutput.amount()
        )) | addConversion(mapper, rightOutput, forIngredients(
              inputs,
              NSSChemical.createChemical(leftOutput), -leftOutput.amount()
        ));
    }
}