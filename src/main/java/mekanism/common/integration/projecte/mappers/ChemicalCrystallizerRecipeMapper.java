package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;

@RecipeTypeMapper
public class ChemicalCrystallizerRecipeMapper extends TypedMekanismRecipeMapper<ChemicalCrystallizerRecipe> {

    public ChemicalCrystallizerRecipeMapper() {
        super(ChemicalCrystallizerRecipe.class, MekanismRecipeType.CRYSTALLIZING);
    }

    @Override
    public String getName() {
        return "MekChemicalCrystallizer";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism crystallizing recipes.";
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ChemicalCrystallizerRecipe recipe) {
        boolean handled = false;
        for (ChemicalStack representation : recipe.getInput().getRepresentations()) {
            ItemStack output = recipe.getOutput(representation);
            if (!output.isEmpty()) {
                IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                ingredientHelper.put(representation);
                if (ingredientHelper.addAsConversion(output)) {
                    handled = true;
                }
            }
        }
        return handled;
    }
}