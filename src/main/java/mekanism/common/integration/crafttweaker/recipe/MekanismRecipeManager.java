package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker.impl.recipes.wrappers.WrapperRecipe;
import java.util.List;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER)
public abstract class MekanismRecipeManager<RECIPE extends MekanismRecipe> implements IRecipeManager {

    private final MekanismRecipeType<RECIPE> recipeType;

    protected MekanismRecipeManager(MekanismRecipeType<RECIPE> recipeType) {
        this.recipeType = recipeType;
    }

    protected abstract ActionAddMekanismRecipe getAction(RECIPE recipe);

    protected void addRecipe(RECIPE recipe) {
        CraftTweakerAPI.apply(getAction(recipe));
    }

    @Override
    public IRecipeType<RECIPE> getRecipeType() {
        return recipeType;
    }

    @Override
    public ResourceLocation getBracketResourceLocation() {
        //Short circuit reverse lookup and just grab it from our recipe type
        return recipeType.getRegistryName();
    }

    @Override
    public List<WrapperRecipe> getRecipesByOutput(IIngredient output) {
        throw new UnsupportedOperationException("Mekanism's recipe managers don't support reverse lookup by output, please lookup by recipe name.");
    }

    @Override
    public void removeRecipe(IItemStack output) {
        throw new UnsupportedOperationException("Mekanism's recipe managers don't support removal by output, please remove by recipe name.");
    }

    protected abstract class ActionAddMekanismRecipe extends ActionAddRecipe {

        protected ActionAddMekanismRecipe(RECIPE recipe) {
            super(MekanismRecipeManager.this, recipe, "");
        }

        protected RECIPE getRecipe() {
            return (RECIPE) recipe;
        }

        @Override//Force implementers to describe the outputs
        protected abstract String describeOutputs();
    }
}