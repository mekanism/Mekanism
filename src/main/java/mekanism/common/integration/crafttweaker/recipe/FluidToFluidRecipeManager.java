package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.fluid.MCFluidStack;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_FLUID_TO_FLUID)
public abstract class FluidToFluidRecipeManager extends MekanismRecipeManager {

    protected FluidToFluidRecipeManager() {
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_EVAPORATING)
    public static class EvaporatingRecipeManager extends FluidToFluidRecipeManager {

        public static final EvaporatingRecipeManager INSTANCE = new EvaporatingRecipeManager();

        private EvaporatingRecipeManager() {
        }

        @Override
        public IRecipeType<FluidToFluidRecipe> getRecipeType() {
            return MekanismRecipeType.EVAPORATING;
        }
    }

    private static class ActionAddFluidToFluidRecipe extends ActionAddMekanismRecipe<FluidToFluidRecipe> {

        protected ActionAddFluidToFluidRecipe(MekanismRecipeManager recipeManager, FluidToFluidRecipe recipe) {
            super(recipeManager, recipe);
        }

        @Override
        protected String describeOutputs() {
            return new MCFluidStack(getRecipe().getOutputRepresentation()).toString();
        }
    }
}