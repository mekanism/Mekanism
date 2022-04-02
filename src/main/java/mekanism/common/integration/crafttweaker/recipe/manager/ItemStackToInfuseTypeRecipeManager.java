package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.InfusionConversionIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_ITEM_STACK_TO_INFUSE_TYPE)
public abstract class ItemStackToInfuseTypeRecipeManager extends MekanismRecipeManager<ItemStackToInfuseTypeRecipe> {

    protected ItemStackToInfuseTypeRecipeManager(IMekanismRecipeTypeProvider<ItemStackToInfuseTypeRecipe, ?> recipeType) {
        super(recipeType);
    }

    /**
     * Adds a recipe that an item into an infuse type.
     * <br>
     * If this is called from the infusion conversion recipe manager, this will be an infusion conversion recipe and be able to be used in any slots in Mekanism machines
     * that are able to convert items to infuse types, for example in the Metallurgic Infuser and in Infusing Factories.
     *
     * @param name   Name of the new recipe.
     * @param input  {@link ItemStackIngredient} representing the input of the recipe.
     * @param output {@link ICrTInfusionStack} representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient input, ICrTInfusionStack output) {
        addRecipe(makeRecipe(getAndValidateName(name), input, getAndValidateNotEmpty(output)));
    }

    protected abstract ItemStackToInfuseTypeRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, InfusionStack output);

    @Override
    protected ActionAddMekanismRecipe getAction(ItemStackToInfuseTypeRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(recipe.getOutputDefinition(), CrTInfusionStack::new);
            }
        };
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