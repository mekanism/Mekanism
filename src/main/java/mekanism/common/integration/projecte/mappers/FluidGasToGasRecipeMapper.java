package mekanism.common.integration.projecte.mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.common.integration.projecte.NSSGas;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraftforge.fluids.FluidStack;

@RecipeTypeMapper
public class FluidGasToGasRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekFluidGasToGas";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism washing recipes.";
    }

    @Override
    public boolean canHandle(IRecipeType<?> recipeType) {
        return recipeType == MekanismRecipeType.WASHING;
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, IRecipe<?> iRecipe) {
        if (!(iRecipe instanceof FluidGasToGasRecipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        boolean handled = false;
        FluidGasToGasRecipe recipe = (FluidGasToGasRecipe) iRecipe;
        List<@NonNull FluidStack> fluidRepresentations = recipe.getFluidInput().getRepresentations();
        List<@NonNull GasStack> gasRepresentations = recipe.getGasInput().getRepresentations();
        for (FluidStack fluidRepresentation : fluidRepresentations) {
            NormalizedSimpleStack nssFluid = NSSFluid.createFluid(fluidRepresentation);
            for (GasStack gasRepresentation : gasRepresentations) {
                Map<NormalizedSimpleStack, Integer> ingredientMap = new HashMap<>();
                ingredientMap.put(nssFluid, fluidRepresentation.getAmount());
                ingredientMap.put(NSSGas.createGas(gasRepresentation), gasRepresentation.getAmount());
                GasStack output = recipe.getOutput(fluidRepresentation, gasRepresentation);
                if (!output.isEmpty()) {
                    mapper.addConversion(output.getAmount(), NSSGas.createGas(output), ingredientMap);
                    handled = true;
                }
            }
        }
        return handled;
    }
}