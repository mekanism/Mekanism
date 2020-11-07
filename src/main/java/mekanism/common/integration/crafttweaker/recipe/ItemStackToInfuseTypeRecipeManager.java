package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.ingredient.CrTItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.InfusionConversionIRecipe;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ITEM_STACK_TO_INFUSE_TYPE)
public abstract class ItemStackToInfuseTypeRecipeManager extends MekanismRecipeManager<ItemStackToInfuseTypeRecipe> {

    protected ItemStackToInfuseTypeRecipeManager(MekanismRecipeType<ItemStackToInfuseTypeRecipe> recipeType) {
        super(recipeType);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTItemStackIngredient input, ICrTInfusionStack output) {
        addRecipe(makeRecipe(getAndValidateName(name), input.getInternal(), getAndValidateNotEmpty(output)));
    }

    protected abstract ItemStackToInfuseTypeRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, InfusionStack output);

    @Override
    protected ActionAddMekanismRecipe getAction(ItemStackToInfuseTypeRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return new CrTInfusionStack(getRecipe().getOutputDefinition()).toString();
            }
        };
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_INFUSION_CONVERSION)
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