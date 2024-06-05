package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.basic.BasicChemicalOxidizerRecipe;
import mekanism.api.recipes.basic.BasicGasConversionRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_ITEM_STACK_TO_GAS)
public abstract class ItemStackToGasRecipeManager extends ItemStackToChemicalRecipeManager<Gas, GasStack, ICrTGasStack, ItemStackToGasRecipe> {

    protected ItemStackToGasRecipeManager(IMekanismRecipeTypeProvider<ItemStackToGasRecipe, ?> recipeType) {
        super(recipeType);
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_GAS_CONVERSION)
    public static class GasConversionRecipeManager extends ItemStackToGasRecipeManager {

        public static final GasConversionRecipeManager INSTANCE = new GasConversionRecipeManager();

        private GasConversionRecipeManager() {
            super(MekanismRecipeType.GAS_CONVERSION);
        }

        @Override
        protected ItemStackToGasRecipe makeRecipe(IIngredientWithAmount input, GasStack output) {
            return new BasicGasConversionRecipe(CrTUtils.fromCrT(input), output);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_OXIDIZING)
    public static class ChemicalOxidizerRecipeManager extends ItemStackToGasRecipeManager {

        public static final ChemicalOxidizerRecipeManager INSTANCE = new ChemicalOxidizerRecipeManager();

        private ChemicalOxidizerRecipeManager() {
            super(MekanismRecipeType.OXIDIZING);
        }

        @Override
        protected ItemStackToGasRecipe makeRecipe(IIngredientWithAmount input, GasStack output) {
            return new BasicChemicalOxidizerRecipe(CrTUtils.fromCrT(input), output);
        }
    }
}