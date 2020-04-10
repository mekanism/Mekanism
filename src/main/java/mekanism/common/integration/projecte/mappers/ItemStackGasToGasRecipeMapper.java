package mekanism.common.integration.projecte.mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.common.integration.projecte.NSSGas;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;

@RecipeTypeMapper
public class ItemStackGasToGasRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekItemStackGasToGas";
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
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, IRecipe<?> iRecipe) {
        if (!(iRecipe instanceof ItemStackGasToGasRecipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        boolean handled = false;
        ItemStackGasToGasRecipe recipe = (ItemStackGasToGasRecipe) iRecipe;
        List<@NonNull ItemStack> itemRepresentations = recipe.getItemInput().getRepresentations();
        List<@NonNull GasStack> gasRepresentations = recipe.getGasInput().getRepresentations();
        int gasMultiplier = TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED * TileEntityAdvancedElectricMachine.BASE_GAS_PER_TICK;
        for (ItemStack itemRepresentation : itemRepresentations) {
            NormalizedSimpleStack nssItem = NSSItem.createItem(itemRepresentation);
            for (GasStack gasRepresentation : gasRepresentations) {
                Map<NormalizedSimpleStack, Integer> ingredientMap = new HashMap<>();
                ingredientMap.put(nssItem, itemRepresentation.getCount());
                ingredientMap.put(NSSGas.createGas(gasRepresentation), gasRepresentation.getAmount() * gasMultiplier);
                GasStack output = recipe.getOutput(itemRepresentation, gasRepresentation);
                if (!output.isEmpty()) {
                    mapper.addConversion(output.getAmount(), NSSGas.createGas(output), ingredientMap);
                    handled = true;
                }
            }
        }
        return handled;
    }
}