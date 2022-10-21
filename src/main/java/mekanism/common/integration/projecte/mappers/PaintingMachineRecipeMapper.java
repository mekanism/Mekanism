package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.integration.projecte.NSSPigment;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

@RecipeTypeMapper
public class PaintingMachineRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekPaintingMachine";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism painting machine recipes.";
    }

    @Override
    public boolean canHandle(RecipeType<?> recipeType) {
        return recipeType == MekanismRecipeType.PAINTING.get();
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, INSSFakeGroupManager groupManager) {
        if (!(iRecipe instanceof PaintingRecipe recipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        boolean handled = false;
        List<@NotNull PigmentStack> pigmentRepresentations = recipe.getChemicalInput().getRepresentations();
        List<@NotNull ItemStack> itemRepresentations = recipe.getItemInput().getRepresentations();
        for (PigmentStack pigmentRepresentation : pigmentRepresentations) {
            NormalizedSimpleStack nssPigment = NSSPigment.createPigment(pigmentRepresentation);
            for (ItemStack itemRepresentation : itemRepresentations) {
                ItemStack output = recipe.getOutput(itemRepresentation, pigmentRepresentation);
                if (!output.isEmpty()) {
                    IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                    ingredientHelper.put(nssPigment, pigmentRepresentation.getAmount());
                    ingredientHelper.put(itemRepresentation);
                    if (ingredientHelper.addAsConversion(output)) {
                        handled = true;
                    }
                }
            }
        }
        return handled;
    }
}