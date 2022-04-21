package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.integration.projecte.NSSGas;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

@RecipeTypeMapper
public class ChemicalInfuserRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekChemicalInfuser";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism chemical infuser recipes.";
    }

    @Override
    public boolean canHandle(RecipeType<?> recipeType) {
        return recipeType == MekanismRecipeType.CHEMICAL_INFUSING.get();
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, INSSFakeGroupManager groupManager) {
        if (!(iRecipe instanceof ChemicalInfuserRecipe recipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        boolean handled = false;
        List<@NonNull GasStack> leftInputRepresentations = recipe.getLeftInput().getRepresentations();
        List<@NonNull GasStack> rightInputRepresentations = recipe.getRightInput().getRepresentations();
        for (GasStack leftRepresentation : leftInputRepresentations) {
            NormalizedSimpleStack nssLeft = NSSGas.createGas(leftRepresentation);
            for (GasStack rightRepresentation : rightInputRepresentations) {
                GasStack output = recipe.getOutput(leftRepresentation, rightRepresentation);
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