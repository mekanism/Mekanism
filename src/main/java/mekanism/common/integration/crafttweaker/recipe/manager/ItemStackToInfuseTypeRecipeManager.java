package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.basic.BasicItemStackToInfuseTypeRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_ITEM_STACK_TO_INFUSE_TYPE)
public abstract class ItemStackToInfuseTypeRecipeManager extends ItemStackToChemicalRecipeManager<ItemStackToInfuseTypeRecipe> {

    protected ItemStackToInfuseTypeRecipeManager(IMekanismRecipeTypeProvider<SingleRecipeInput, ItemStackToInfuseTypeRecipe, ?> recipeType) {
        super(recipeType);
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_INFUSION_CONVERSION)
    public static class InfusionConversionRecipeManager extends ItemStackToInfuseTypeRecipeManager {

        public static final InfusionConversionRecipeManager INSTANCE = new InfusionConversionRecipeManager();

        private InfusionConversionRecipeManager() {
            super(MekanismRecipeType.INFUSION_CONVERSION);
        }

        @Override
        protected ItemStackToInfuseTypeRecipe makeRecipe(IIngredientWithAmount input, ChemicalStack output) {
            return new BasicItemStackToInfuseTypeRecipe(CrTUtils.fromCrT(input), output);
        }
    }
}