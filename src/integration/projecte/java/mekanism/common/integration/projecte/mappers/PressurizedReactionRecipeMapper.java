package mekanism.common.integration.projecte.mappers;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe.PressurizedReactionRecipeOutput;
import mekanism.api.recipes.basic.BasicPressurizedReactionRecipe;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.integration.projecte.NSSChemical;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStackTemplate;

@RecipeTypeMapper
public class PressurizedReactionRecipeMapper extends TypedMekanismRecipeMapper<PressurizedReactionRecipe> {

    public PressurizedReactionRecipeMapper() {
        super(MekanismConfigTranslations.PE_MAPPER_PRESSURIZED_REACTION, PressurizedReactionRecipe.class, MekanismRecipeType.REACTION);
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, PressurizedReactionRecipe recipe, MekFakeGroupHelper fakeGroupHelper, ContextMap contextMap) {
        if (OPTIMIZE_BASIC && recipe instanceof BasicPressurizedReactionRecipe basicRecipe) {
            //This will be the case for the majority of our recipes
            ItemStackTemplate outputItem = basicRecipe.getOutputItem();
            ChemicalStackTemplate outputChemical = basicRecipe.getOutputChemical();
            if (outputItem == null && outputChemical == null) {
                return false;
            }
            return addConversions(mapper, new PressurizedReactionRecipeOutput(outputItem, outputChemical), fakeGroupHelper.forIngredients(
                  contextMap, recipe.getInputSolid(),
                  recipe.getInputFluid(),
                  recipe.getInputChemical()
            ));
        }
        return addConversions(mapper, recipe.getInputSolid(), recipe.getInputFluid(), recipe.getInputChemical(), recipe::getOutput, fakeGroupHelper::forItems,
              fakeGroupHelper::forFluids, fakeGroupHelper::forChemicals, PressurizedReactionRecipeMapper::addConversions, contextMap);
    }

    private static boolean addConversions(IMappingCollector<NormalizedSimpleStack, Long> mapper, PressurizedReactionRecipeOutput output,
          Object2IntMap<NormalizedSimpleStack> inputs) {
        if (inputs.isEmpty()) {
            return false;
        }
        ItemStackTemplate outputItem = output.item();
        ChemicalStackTemplate outputChemical = output.chemical();
        if (outputItem == null) {
            return addConversion(mapper, outputChemical, inputs);
        } else if (outputChemical == null) {
            return addConversion(mapper, outputItem, inputs);
        }
        //Use bitwise or as we want to try and add both of them
        return addConversion(mapper, outputItem, forIngredients(
              inputs,
              NSSChemical.createChemical(outputChemical), -outputChemical.amount()
        )) | addConversion(mapper, outputChemical, forIngredients(
              inputs,
              NSSItem.createItem(outputItem.item(), outputItem.components()), -outputItem.count()
        ));
    }
}