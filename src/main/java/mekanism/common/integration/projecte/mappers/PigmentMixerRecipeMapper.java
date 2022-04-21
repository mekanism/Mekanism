package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.integration.projecte.NSSPigment;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

@RecipeTypeMapper
public class PigmentMixerRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekPigmentMixer";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism pigment mixer recipes.";
    }

    @Override
    public boolean canHandle(RecipeType<?> recipeType) {
        return recipeType == MekanismRecipeType.PIGMENT_MIXING.get();
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, INSSFakeGroupManager groupManager) {
        if (!(iRecipe instanceof PigmentMixingRecipe recipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        boolean handled = false;
        List<@NonNull PigmentStack> leftInputRepresentations = recipe.getLeftInput().getRepresentations();
        List<@NonNull PigmentStack> rightInputRepresentations = recipe.getRightInput().getRepresentations();
        for (PigmentStack leftRepresentation : leftInputRepresentations) {
            NormalizedSimpleStack nssLeft = NSSPigment.createPigment(leftRepresentation);
            for (PigmentStack rightRepresentation : rightInputRepresentations) {
                PigmentStack output = recipe.getOutput(leftRepresentation, rightRepresentation);
                if (!output.isEmpty()) {
                    IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                    ingredientHelper.put(nssLeft, leftRepresentation.getAmount());
                    ingredientHelper.put(rightRepresentation);
                    if (ingredientHelper.addAsConversion(output)) {
                        handled = true;
                    }
                }
            }
        }
        return handled;
    }
}