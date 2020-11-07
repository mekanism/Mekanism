package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.fluid.MCFluidStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.recipe.MekanismRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ROTARY)
public class RotaryRecipeManager extends MekanismRecipeManager<RotaryRecipe> {

    public static final RotaryRecipeManager INSTANCE = new RotaryRecipeManager();

    private RotaryRecipeManager() {
        super(MekanismRecipeType.ROTARY);
    }

    @Override
    protected ActionAddMekanismRecipe getAction(RotaryRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                RotaryRecipe recipe = getRecipe();
                StringBuilder builder = new StringBuilder();
                if (recipe.hasFluidToGas()) {
                    builder.append(new MCFluidStack(recipe.getFluidOutputRepresentation())).append(" for fluid to gas");
                }
                if (recipe.hasGasToFluid()) {
                    if (recipe.hasFluidToGas()) {
                        builder.append(" and ");
                    }
                    builder.append(new CrTGasStack(recipe.getGasOutputRepresentation())).append(" for gas to fluid");
                }
                return builder.toString();
            }
        };
    }
}