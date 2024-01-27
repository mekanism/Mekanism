package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@RecipeTypeMapper
public class CombinerRecipeMapper extends TypedMekanismRecipeMapper<CombinerRecipe> {

    public CombinerRecipeMapper() {
        super(CombinerRecipe.class, MekanismRecipeType.COMBINING);
    }

    @Override
    public String getName() {
        return "MekCombiner";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism combiner recipes.";
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, CombinerRecipe recipe) {
        boolean handled = false;
        List<@NotNull ItemStack> mainRepresentations = recipe.getMainInput().getRepresentations();
        List<@NotNull ItemStack> extraRepresentations = recipe.getExtraInput().getRepresentations();
        for (ItemStack mainRepresentation : mainRepresentations) {
            NormalizedSimpleStack nssMain = NSSItem.createItem(mainRepresentation);
            for (ItemStack extraRepresentation : extraRepresentations) {
                ItemStack output = recipe.getOutput(mainRepresentation, extraRepresentation);
                if (!output.isEmpty()) {
                    IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                    ingredientHelper.put(nssMain, mainRepresentation.getCount());
                    ingredientHelper.put(extraRepresentation);
                    if (ingredientHelper.addAsConversion(output)) {
                        handled = true;
                    }
                }
            }
        }
        return handled;
    }
}