package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.integration.projecte.NSSChemical;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@RecipeTypeMapper
public class MetallurgicInfuserRecipeMapper extends TypedMekanismRecipeMapper<MetallurgicInfuserRecipe> {

    public MetallurgicInfuserRecipeMapper() {
        super(MetallurgicInfuserRecipe.class, MekanismRecipeType.METALLURGIC_INFUSING);
    }

    @Override
    public String getName() {
        return "MekMetallurgicInfuser";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism metallurgic infuser recipes.";
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, MetallurgicInfuserRecipe recipe) {
        boolean handled = false;
        List<@NotNull ChemicalStack> infuseTypeRepresentations = recipe.getChemicalInput().getRepresentations();
        List<@NotNull ItemStack> itemRepresentations = recipe.getItemInput().getRepresentations();
        for (ChemicalStack infuseTypeRepresentation : infuseTypeRepresentations) {
            NormalizedSimpleStack nssInfuseType = NSSChemical.createChemical(infuseTypeRepresentation);
            for (ItemStack itemRepresentation : itemRepresentations) {
                ItemStack output = recipe.getOutput(itemRepresentation, infuseTypeRepresentation);
                if (!output.isEmpty()) {
                    IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                    ingredientHelper.put(nssInfuseType, infuseTypeRepresentation.getAmount());
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