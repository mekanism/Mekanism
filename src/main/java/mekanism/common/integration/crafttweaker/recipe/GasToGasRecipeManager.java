package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_GAS_TO_GAS)
public abstract class GasToGasRecipeManager extends MekanismRecipeManager {

    protected GasToGasRecipeManager() {
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ACTIVATING)
    public static class SolarNeutronActivatorRecipeManager extends GasToGasRecipeManager {

        public static final SolarNeutronActivatorRecipeManager INSTANCE = new SolarNeutronActivatorRecipeManager();

        private SolarNeutronActivatorRecipeManager() {
        }

        @Override
        public IRecipeType<GasToGasRecipe> getRecipeType() {
            return MekanismRecipeType.ACTIVATING;
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_CENTRIFUGING)
    public static class IsotopicCentrifugeRecipeManager extends GasToGasRecipeManager {

        public static final IsotopicCentrifugeRecipeManager INSTANCE = new IsotopicCentrifugeRecipeManager();

        private IsotopicCentrifugeRecipeManager() {
        }

        @Override
        public IRecipeType<GasToGasRecipe> getRecipeType() {
            return MekanismRecipeType.CENTRIFUGING;
        }
    }

    private static class ActionAddGasToGasRecipe extends ActionAddMekanismRecipe<GasToGasRecipe> {

        protected ActionAddGasToGasRecipe(MekanismRecipeManager recipeManager, GasToGasRecipe recipe) {
            super(recipeManager, recipe);
        }

        @Override
        protected String describeOutputs() {
            return new CrTGasStack(getRecipe().getOutputRepresentation()).toString();
        }
    }
}