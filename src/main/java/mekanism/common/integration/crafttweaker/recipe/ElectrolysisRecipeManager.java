package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTFloatingLong;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.ingredient.CrTFluidStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.ElectrolysisIRecipe;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_SEPARATING)
public class ElectrolysisRecipeManager extends MekanismRecipeManager<ElectrolysisRecipe> {

    public static final ElectrolysisRecipeManager INSTANCE = new ElectrolysisRecipeManager();

    private ElectrolysisRecipeManager() {
        super(MekanismRecipeType.SEPARATING);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTFluidStackIngredient input, ICrTGasStack leftGasOutput, ICrTGasStack rightGasOutput,
          @ZenCodeType.Optional("1 as " + CrTConstants.CLASS_FLOATING_LONG) CrTFloatingLong energyMultiplier) {
        FloatingLong multiplier = energyMultiplier.getInternalAsConst();
        if (multiplier.smallerThan(FloatingLong.ONE)) {
            throw new IllegalArgumentException("Energy multiplier must be at least one! Multiplier: " + multiplier);
        }
        addRecipe(new ElectrolysisIRecipe(getAndValidateName(name), input.getInternal(), multiplier, getAndValidateNotEmpty(leftGasOutput),
              getAndValidateNotEmpty(rightGasOutput)));
    }

    @Override
    protected ActionAddMekanismRecipe getAction(ElectrolysisRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return new CrTGasStack(getRecipe().getLeftGasOutputRepresentation()) + " and " + new CrTGasStack(getRecipe().getRightGasOutputRepresentation());
            }
        };
    }
}