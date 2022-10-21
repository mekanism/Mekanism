package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.integration.projecte.NSSGas;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
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
public class ItemStackGasToItemStackRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekItemStackGasToItemStack";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism Machine recipes that go from item, gas to item. (Compressing, Purifying, Injecting)";
    }

    @Override
    public boolean canHandle(RecipeType<?> recipeType) {
        return recipeType == MekanismRecipeType.COMPRESSING.get() || recipeType == MekanismRecipeType.PURIFYING.get() || recipeType == MekanismRecipeType.INJECTING.get();
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, INSSFakeGroupManager groupManager) {
        if (!(iRecipe instanceof ItemStackGasToItemStackRecipe recipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        boolean handled = false;
        List<@NotNull ItemStack> itemRepresentations = recipe.getItemInput().getRepresentations();
        List<@NotNull GasStack> gasRepresentations = recipe.getChemicalInput().getRepresentations();
        for (GasStack gasRepresentation : gasRepresentations) {
            NSSGas nssGas = NSSGas.createGas(gasRepresentation);
            long gasAmount = gasRepresentation.getAmount() * TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED;
            for (ItemStack itemRepresentation : itemRepresentations) {
                ItemStack output = recipe.getOutput(itemRepresentation, gasRepresentation);
                if (!output.isEmpty()) {
                    IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                    ingredientHelper.put(itemRepresentation);
                    ingredientHelper.put(nssGas, gasAmount);
                    if (ingredientHelper.addAsConversion(output)) {
                        handled = true;
                    }
                }
            }
        }
        return handled;
    }
}