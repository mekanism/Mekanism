package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.fluid.MCFluidStack;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_FLUID_TO_FLUID)
public abstract class FluidToFluidRecipeManager extends MekanismRecipeManager<FluidToFluidRecipe> {

    protected FluidToFluidRecipeManager(MekanismRecipeType<FluidToFluidRecipe> recipeType) {
        super(recipeType);
    }

    @Override
    protected ActionAddMekanismRecipe getAction(FluidToFluidRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return new MCFluidStack(getRecipe().getOutputRepresentation()).toString();
            }
        };
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_EVAPORATING)
    public static class EvaporatingRecipeManager extends FluidToFluidRecipeManager {

        public static final EvaporatingRecipeManager INSTANCE = new EvaporatingRecipeManager();

        private EvaporatingRecipeManager() {
            super(MekanismRecipeType.EVAPORATING);
        }
    }
}