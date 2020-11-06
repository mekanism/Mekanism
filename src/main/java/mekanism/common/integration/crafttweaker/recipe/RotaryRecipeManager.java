package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.fluid.MCFluidStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ROTARY)
public class RotaryRecipeManager extends MekanismRecipeManager {

    public static final RotaryRecipeManager INSTANCE = new RotaryRecipeManager();

    private RotaryRecipeManager() {
    }

    @Override
    public IRecipeType<RotaryRecipe> getRecipeType() {
        return MekanismRecipeType.ROTARY;
    }

    private static class ActionAddRotaryRecipe extends ActionAddMekanismRecipe<RotaryRecipe> {

        protected ActionAddRotaryRecipe(MekanismRecipeManager recipeManager, RotaryRecipe recipe) {
            super(recipeManager, recipe);
        }

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
    }
}