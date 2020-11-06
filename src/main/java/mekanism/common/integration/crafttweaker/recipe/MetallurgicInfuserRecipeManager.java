package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_METALLURGIC_INFUSING)
public class MetallurgicInfuserRecipeManager extends MekanismRecipeManager {

    public static final MetallurgicInfuserRecipeManager INSTANCE = new MetallurgicInfuserRecipeManager();

    private MetallurgicInfuserRecipeManager() {
    }

    @Override
    public IRecipeType<MetallurgicInfuserRecipe> getRecipeType() {
        return MekanismRecipeType.METALLURGIC_INFUSING;
    }

    private static class ActionAddMetallurgicInfuserRecipe extends ActionAddMekanismRecipe<MetallurgicInfuserRecipe> {

        protected ActionAddMetallurgicInfuserRecipe(MekanismRecipeManager recipeManager, MetallurgicInfuserRecipe recipe) {
            super(recipeManager, recipe);
        }

        @Override
        protected String describeOutputs() {
            return CrTUtils.describeOutputs(getRecipe().getOutputDefinition(), MCItemStackMutable::new);
        }
    }
}