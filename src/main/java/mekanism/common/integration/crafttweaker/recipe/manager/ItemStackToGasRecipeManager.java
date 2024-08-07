package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.basic.BasicChemicalOxidizerRecipe;
import mekanism.api.recipes.basic.BasicChemicalConversionRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_ITEM_STACK_TO_GAS)
public abstract class ItemStackToGasRecipeManager extends ItemStackToChemicalRecipeManager<ItemStackToChemicalRecipe> {

    protected ItemStackToGasRecipeManager(IMekanismRecipeTypeProvider<SingleRecipeInput, ItemStackToChemicalRecipe, ?> recipeType) {
        super(recipeType);
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_CHEMICAL_CONVERSION)
    public static class ChemicalConversionRecipeManager extends ItemStackToGasRecipeManager {

        public static final ChemicalConversionRecipeManager INSTANCE = new ChemicalConversionRecipeManager();

        private ChemicalConversionRecipeManager() {
            super(MekanismRecipeType.CHEMICAL_CONVERSION);
        }

        @Override
        protected ItemStackToChemicalRecipe makeRecipe(IIngredientWithAmount input, ChemicalStack output) {
            return new BasicChemicalConversionRecipe(CrTUtils.fromCrT(input), output);
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
        protected ItemStackToChemicalRecipe makeRecipe(IIngredientWithAmount input, ChemicalStack output) {
            return new BasicChemicalOxidizerRecipe(CrTUtils.fromCrT(input), output);
        }
    }
}