package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTSlurryStack;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_FLUID_SLURRY_TO_SLURRY)
public abstract class FluidSlurryToSlurryRecipeManager extends MekanismRecipeManager {

    protected FluidSlurryToSlurryRecipeManager() {
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_WASHING)
    public static class ChemicalWasherRecipeManager extends FluidSlurryToSlurryRecipeManager {

        public static final ChemicalWasherRecipeManager INSTANCE = new ChemicalWasherRecipeManager();

        private ChemicalWasherRecipeManager() {
        }

        @Override
        public IRecipeType<FluidSlurryToSlurryRecipe> getRecipeType() {
            return MekanismRecipeType.WASHING;
        }
    }

    private static class ActionAddFluidSlurryToSlurryRecipe extends ActionAddMekanismRecipe<FluidSlurryToSlurryRecipe> {

        protected ActionAddFluidSlurryToSlurryRecipe(MekanismRecipeManager recipeManager, FluidSlurryToSlurryRecipe recipe) {
            super(recipeManager, recipe);
        }

        @Override
        protected String describeOutputs() {
            return new CrTSlurryStack(getRecipe().getOutputRepresentation()).toString();
        }
    }
}