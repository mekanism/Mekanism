package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.impl.fluid.MCFluidStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.ingredient.CrTFluidStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTGasStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.RotaryIRecipe;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ROTARY)
public class RotaryRecipeManager extends MekanismRecipeManager<RotaryRecipe> {

    public static final RotaryRecipeManager INSTANCE = new RotaryRecipeManager();

    private RotaryRecipeManager() {
        super(MekanismRecipeType.ROTARY);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTFluidStackIngredient fluidInput, ICrTGasStack gasOutput) {
        addRecipe(new RotaryIRecipe(getAndValidateName(name), fluidInput.getInternal(), getAndValidateNotEmpty(gasOutput)));
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTGasStackIngredient gasInput, IFluidStack fluidOutput) {
        addRecipe(new RotaryIRecipe(getAndValidateName(name), gasInput.getInternal(), getAndValidateNotEmpty(fluidOutput)));
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTFluidStackIngredient fluidInput, CrTGasStackIngredient gasInput, ICrTGasStack gasOutput, IFluidStack fluidOutput) {
        addRecipe(new RotaryIRecipe(getAndValidateName(name), fluidInput.getInternal(), gasInput.getInternal(), getAndValidateNotEmpty(gasOutput),
              getAndValidateNotEmpty(fluidOutput)));
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