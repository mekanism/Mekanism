package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_COMBINING)
public class CombinerRecipeManager extends MekanismRecipeManager {

    public static final CombinerRecipeManager INSTANCE = new CombinerRecipeManager();

    private CombinerRecipeManager() {
    }

    @Override
    public IRecipeType<CombinerRecipe> getRecipeType() {
        return MekanismRecipeType.COMBINING;
    }

    private static class ActionAddCombinerRecipe extends ActionAddMekanismRecipe<CombinerRecipe> {

        protected ActionAddCombinerRecipe(MekanismRecipeManager recipeManager, CombinerRecipe recipe) {
            super(recipeManager, recipe);
        }

        @Override
        protected String describeOutputs() {
            return CrTUtils.describeOutputs(getRecipe().getOutputDefinition(), MCItemStackMutable::new);
        }
    }
}