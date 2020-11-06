package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker.impl.recipes.wrappers.WrapperRecipe;
import java.util.List;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER)
public abstract class MekanismRecipeManager implements IRecipeManager {

    protected MekanismRecipeManager() {
    }

    @Override
    public List<WrapperRecipe> getRecipesByOutput(IIngredient output) {
        throw new UnsupportedOperationException("Mekanism's recipe managers don't support reverse lookup by output, please lookup by recipe name.");
    }

    @Override
    public void removeRecipe(IItemStack output) {
        throw new UnsupportedOperationException("Mekanism's recipe managers don't support removal by output, please remove by recipe name.");
    }

    protected static abstract class ActionAddMekanismRecipe<RECIPE extends MekanismRecipe> extends ActionAddRecipe {

        protected ActionAddMekanismRecipe(MekanismRecipeManager recipeManager, RECIPE recipe) {
            super(recipeManager, recipe, "");
        }

        protected RECIPE getRecipe() {
            return (RECIPE) recipe;
        }

        @Override//Force implementers to describe the outputs
        protected abstract String describeOutputs();
    }
}