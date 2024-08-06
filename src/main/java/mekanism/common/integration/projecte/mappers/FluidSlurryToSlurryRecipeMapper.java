package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

@RecipeTypeMapper
public class FluidSlurryToSlurryRecipeMapper extends TypedMekanismRecipeMapper<FluidSlurryToSlurryRecipe> {

    public FluidSlurryToSlurryRecipeMapper() {
        super(FluidSlurryToSlurryRecipe.class, MekanismRecipeType.WASHING);
    }

    @Override
    public String getName() {
        return "MekFluidSlurryToSlurry";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism washing recipes.";
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, FluidSlurryToSlurryRecipe recipe) {
        boolean handled = false;
        List<@NotNull FluidStack> fluidRepresentations = recipe.getFluidInput().getRepresentations();
        List<@NotNull ChemicalStack> slurryRepresentations = recipe.getChemicalInput().getRepresentations();
        for (FluidStack fluidRepresentation : fluidRepresentations) {
            NormalizedSimpleStack nssFluid = NSSFluid.createFluid(fluidRepresentation);
            for (ChemicalStack slurryRepresentation : slurryRepresentations) {
                ChemicalStack output = recipe.getOutput(fluidRepresentation, slurryRepresentation);
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
}