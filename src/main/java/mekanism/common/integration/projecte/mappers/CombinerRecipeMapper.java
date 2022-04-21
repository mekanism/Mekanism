package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

@RecipeTypeMapper
public class CombinerRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekCombiner";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism combiner recipes.";
    }

    @Override
    public boolean canHandle(RecipeType<?> recipeType) {
        return recipeType == MekanismRecipeType.COMBINING.get();
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, INSSFakeGroupManager groupManager) {
        if (!(iRecipe instanceof CombinerRecipe recipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        boolean handled = false;
        List<@NonNull ItemStack> mainRepresentations = recipe.getMainInput().getRepresentations();
        List<@NonNull ItemStack> extraRepresentations = recipe.getExtraInput().getRepresentations();
        for (ItemStack mainRepresentation : mainRepresentations) {
            NormalizedSimpleStack nssMain = NSSItem.createItem(mainRepresentation);
            for (ItemStack extraRepresentation : extraRepresentations) {
                ItemStack output = recipe.getOutput(mainRepresentation, extraRepresentation);
                if (!output.isEmpty()) {
                    IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                    ingredientHelper.put(nssMain, mainRepresentation.getCount());
                    ingredientHelper.put(extraRepresentation);
                    if (ingredientHelper.addAsConversion(output)) {
                        handled = true;
                    }
                }
            }
        }
        return handled;
    }
}