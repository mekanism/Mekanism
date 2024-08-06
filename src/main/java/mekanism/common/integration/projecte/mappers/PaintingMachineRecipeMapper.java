package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.integration.projecte.NSSChemical;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@RecipeTypeMapper
public class PaintingMachineRecipeMapper extends TypedMekanismRecipeMapper<PaintingRecipe> {

    public PaintingMachineRecipeMapper() {
        super(PaintingRecipe.class, MekanismRecipeType.PAINTING);
    }

    @Override
    public String getName() {
        return "MekPaintingMachine";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism painting machine recipes.";
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, PaintingRecipe recipe) {
        boolean handled = false;
        List<@NotNull ChemicalStack> pigmentRepresentations = recipe.getChemicalInput().getRepresentations();
        List<@NotNull ItemStack> itemRepresentations = recipe.getItemInput().getRepresentations();
        for (ChemicalStack pigmentRepresentation : pigmentRepresentations) {
            NormalizedSimpleStack nssPigment = NSSChemical.createChemical(pigmentRepresentation);
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