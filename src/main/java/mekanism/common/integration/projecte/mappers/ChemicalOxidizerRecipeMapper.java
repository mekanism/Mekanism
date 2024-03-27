package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalOxidizerRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@RecipeTypeMapper
public class ChemicalOxidizerRecipeMapper extends TypedMekanismRecipeMapper<ChemicalOxidizerRecipe> {

    public ChemicalOxidizerRecipeMapper() {
        super(ChemicalOxidizerRecipe.class, MekanismRecipeType.OXIDIZING);
    }

    @Override
    public String getName() {
        return "MekChemicalOxidizer";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism oxidizing recipes.";
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ChemicalOxidizerRecipe recipe) {
        boolean handled = false;
        List<@NotNull ItemStack> representations = recipe.getInput().getRepresentations();
        for (ItemStack representation : representations) {
            BoxedChemicalStack output = recipe.getOutput(representation);
            if (!output.isEmpty()) {
                IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                ingredientHelper.put(representation);
                if (ingredientHelper.addAsConversion(output.getChemicalStack())) {
                    handled = true;
                }
            }
        }
        return handled;
    }
}