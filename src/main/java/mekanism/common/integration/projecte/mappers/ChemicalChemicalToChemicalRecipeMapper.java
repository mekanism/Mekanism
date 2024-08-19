package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.integration.projecte.NSSChemical;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import org.jetbrains.annotations.NotNull;

@RecipeTypeMapper
public class ChemicalChemicalToChemicalRecipeMapper extends TypedMekanismRecipeMapper<ChemicalChemicalToChemicalRecipe> {

    public ChemicalChemicalToChemicalRecipeMapper() {
        super(ChemicalChemicalToChemicalRecipe.class, MekanismRecipeType.CHEMICAL_INFUSING, MekanismRecipeType.PIGMENT_MIXING);
    }

    @Override
    public String getName() {
        return "MekChemicalChemicalToChemical";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism chemical infuser and pigment mixer recipes.";
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ChemicalChemicalToChemicalRecipe recipe) {
        boolean handled = false;
        List<@NotNull ChemicalStack> leftInputRepresentations = recipe.getLeftInput().getRepresentations();
        List<@NotNull ChemicalStack> rightInputRepresentations = recipe.getRightInput().getRepresentations();
        for (ChemicalStack leftRepresentation : leftInputRepresentations) {
            NormalizedSimpleStack nssLeft = NSSChemical.createChemical(leftRepresentation);
            for (ChemicalStack rightRepresentation : rightInputRepresentations) {
                ChemicalStack output = recipe.getOutput(leftRepresentation, rightRepresentation);
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