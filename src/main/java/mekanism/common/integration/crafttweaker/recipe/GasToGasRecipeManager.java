package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.ingredient.CrTGasStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.ActivatingIRecipe;
import mekanism.common.recipe.impl.CentrifugingIRecipe;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_GAS_TO_GAS)
public abstract class GasToGasRecipeManager extends MekanismRecipeManager<GasToGasRecipe> {

    protected GasToGasRecipeManager(MekanismRecipeType<GasToGasRecipe> recipeType) {
        super(recipeType);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTGasStackIngredient input, ICrTGasStack output) {
        addRecipe(makeRecipe(getAndValidateName(name), input.getInternal(), getAndValidateNotEmpty(output)));
    }

    protected abstract GasToGasRecipe makeRecipe(ResourceLocation id, GasStackIngredient ingredient, GasStack output);

    @Override
    protected ActionAddMekanismRecipe getAction(GasToGasRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return new CrTGasStack(getRecipe().getOutputRepresentation()).toString();
            }
        };
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ACTIVATING)
    public static class SolarNeutronActivatorRecipeManager extends GasToGasRecipeManager {

        public static final SolarNeutronActivatorRecipeManager INSTANCE = new SolarNeutronActivatorRecipeManager();

        private SolarNeutronActivatorRecipeManager() {
            super(MekanismRecipeType.ACTIVATING);
        }

        @Override
        protected GasToGasRecipe makeRecipe(ResourceLocation id, GasStackIngredient ingredient, GasStack output) {
            return new ActivatingIRecipe(id, ingredient, output);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_CENTRIFUGING)
    public static class IsotopicCentrifugeRecipeManager extends GasToGasRecipeManager {

        public static final IsotopicCentrifugeRecipeManager INSTANCE = new IsotopicCentrifugeRecipeManager();

        private IsotopicCentrifugeRecipeManager() {
            super(MekanismRecipeType.CENTRIFUGING);
        }

        @Override
        protected GasToGasRecipe makeRecipe(ResourceLocation id, GasStackIngredient ingredient, GasStack output) {
            return new CentrifugingIRecipe(id, ingredient, output);
        }
    }
}