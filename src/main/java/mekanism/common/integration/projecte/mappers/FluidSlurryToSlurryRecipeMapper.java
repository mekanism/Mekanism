/*package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
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
public class FluidSlurryToSlurryRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekFluidSlurryToSlurry";
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
        if (!(iRecipe instanceof FluidSlurryToSlurryRecipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        boolean handled = false;
        FluidSlurryToSlurryRecipe recipe = (FluidSlurryToSlurryRecipe) iRecipe;
        List<@NonNull FluidStack> fluidRepresentations = recipe.getFluidInput().getRepresentations();
        List<@NonNull SlurryStack> slurryRepresentations = recipe.getChemicalInput().getRepresentations();
        for (FluidStack fluidRepresentation : fluidRepresentations) {
            NormalizedSimpleStack nssFluid = NSSFluid.createFluid(fluidRepresentation);
            for (SlurryStack slurryRepresentation : slurryRepresentations) {
                SlurryStack output = recipe.getOutput(fluidRepresentation, slurryRepresentation);
                if (!output.isEmpty()) {
                    IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                    ingredientHelper.put(nssFluid, fluidRepresentation.getAmount());
                    ingredientHelper.put(slurryRepresentation);
                    if (ingredientHelper.addAsConversion(output)) {
                        handled = true;
                    }
                }
            }
        }
        return handled;
    }
}*/