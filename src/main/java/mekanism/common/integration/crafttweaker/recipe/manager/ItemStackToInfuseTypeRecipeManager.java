package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.InfusionConversionIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_ITEM_STACK_TO_INFUSE_TYPE)
public abstract class ItemStackToInfuseTypeRecipeManager extends ItemStackToChemicalRecipeManager<InfuseType, InfusionStack, ICrTInfusionStack, ItemStackToInfuseTypeRecipe> {

    protected ItemStackToInfuseTypeRecipeManager(IMekanismRecipeTypeProvider<ItemStackToInfuseTypeRecipe, ?> recipeType) {
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
        protected ItemStackToInfuseTypeRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, InfusionStack output) {
            return new InfusionConversionIRecipe(id, input, output);
        }
    }
}