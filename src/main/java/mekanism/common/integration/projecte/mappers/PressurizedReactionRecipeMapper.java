/*package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.integration.projecte.NSSGas;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

@RecipeTypeMapper
public class PressurizedReactionRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekPressurizedReaction";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism pressurized reaction recipes.";
    }

    @Override
    public boolean canHandle(IRecipeType<?> recipeType) {
        return recipeType == MekanismRecipeType.REACTION;
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, IRecipe<?> iRecipe) {
        if (!(iRecipe instanceof PressurizedReactionRecipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        boolean handled = false;
        PressurizedReactionRecipe recipe = (PressurizedReactionRecipe) iRecipe;
        List<@NonNull ItemStack> itemRepresentations = recipe.getInputSolid().getRepresentations();
        List<@NonNull FluidStack> fluidRepresentations = recipe.getInputFluid().getRepresentations();
        List<@NonNull GasStack> gasRepresentations = recipe.getInputGas().getRepresentations();
        for (ItemStack itemRepresentation : itemRepresentations) {
            NormalizedSimpleStack nssItem = NSSItem.createItem(itemRepresentation);
            for (FluidStack fluidRepresentation : fluidRepresentations) {
                NormalizedSimpleStack nssFluid = NSSFluid.createFluid(fluidRepresentation);
                for (GasStack gasRepresentation : gasRepresentations) {
                    NormalizedSimpleStack nssGas = NSSGas.createGas(gasRepresentation);
                    Pair<@NonNull ItemStack, @NonNull GasStack> output = recipe.getOutput(itemRepresentation, fluidRepresentation, gasRepresentation);
                    ItemStack itemOutput = output.getLeft();
                    GasStack gasOutput = output.getRight();
                    IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                    ingredientHelper.put(nssItem, itemRepresentation.getCount());
                    ingredientHelper.put(nssFluid, fluidRepresentation.getAmount());
                    ingredientHelper.put(nssGas, gasRepresentation.getAmount());
                    if (itemOutput.isEmpty()) {
                        //We only have a gas output
                        if (!gasOutput.isEmpty() && ingredientHelper.addAsConversion(gasOutput)) {
                            handled = true;
                        }
                    } else if (gasOutput.isEmpty()) {
                        //We only have an item output
                        if (ingredientHelper.addAsConversion(itemOutput)) {
                            handled = true;
                        }
                    } else {
                        NormalizedSimpleStack nssItemOutput = NSSItem.createItem(itemOutput);
                        NormalizedSimpleStack nssGasOutput = NSSGas.createGas(gasOutput);
                        //We have both so do our best guess
                        //Add trying to calculate the item output (using it as if we needed negative of gas output)
                        ingredientHelper.put(nssGasOutput, -gasOutput.getAmount());
                        if (ingredientHelper.addAsConversion(nssItemOutput, itemOutput.getCount())) {
                            handled = true;
                        }
                        //Add trying to calculate gas output (using it as if we needed negative of item output)
                        ingredientHelper.resetHelper();
                        ingredientHelper.put(nssItem, itemRepresentation.getCount());
                        ingredientHelper.put(nssFluid, fluidRepresentation.getAmount());
                        ingredientHelper.put(nssGas, gasRepresentation.getAmount());
                        ingredientHelper.put(nssItemOutput, -itemOutput.getCount());
                        if (ingredientHelper.addAsConversion(nssGasOutput, gasOutput.getAmount())) {
                            handled = true;
                        }
                    }
                }
            }
        }
        return handled;
    }
}*/