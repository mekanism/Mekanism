package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.integration.projecte.NSSGas;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;

@RecipeTypeMapper
public class ChemicalDissolutionRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekDissolution";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism dissolution recipes.";
    }

    @Override
    public boolean canHandle(IRecipeType<?> recipeType) {
        return recipeType == MekanismRecipeType.DISSOLUTION;
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, IRecipe<?> iRecipe, INSSFakeGroupManager groupManager) {
        if (!(iRecipe instanceof ChemicalDissolutionRecipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        boolean handled = false;
        ChemicalDissolutionRecipe recipe = (ChemicalDissolutionRecipe) iRecipe;
        List<@NonNull ItemStack> itemRepresentations = recipe.getItemInput().getRepresentations();
        List<@NonNull GasStack> gasRepresentations = recipe.getGasInput().getRepresentations();
        for (GasStack gasRepresentation : gasRepresentations) {
            NSSGas nssGas = NSSGas.createGas(gasRepresentation);
            long gasAmount = gasRepresentation.getAmount() * TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED;
            for (ItemStack itemRepresentation : itemRepresentations) {
                BoxedChemicalStack output = recipe.getOutput(itemRepresentation, gasRepresentation);
                if (!output.isEmpty()) {
                    IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                    ingredientHelper.put(itemRepresentation);
                    ingredientHelper.put(nssGas, gasAmount);
                    if (ingredientHelper.addAsConversion(output.getChemicalStack())) {
                        handled = true;
                    }
                }
            }
        }
        return handled;
    }
}