package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.PigmentExtractingIRecipe;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ITEM_STACK_TO_PIGMENT)
public abstract class ItemStackToPigmentRecipeManager extends MekanismRecipeManager<ItemStackToPigmentRecipe> {

    protected ItemStackToPigmentRecipeManager(MekanismRecipeType<ItemStackToPigmentRecipe, ?> recipeType) {
        super(recipeType);
    }

    /**
     * Adds a recipe that an item into a pigment.
     * <br>
     * If this is called from the pigment extracting recipe manager, this will be a pigment extracting recipe. Pigment Extractors can process this recipe type.
     *
     * @param name   Name of the new recipe.
     * @param input  {@link ItemStackIngredient} representing the input of the recipe.
     * @param output {@link ICrTPigmentStack} representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient input, ICrTPigmentStack output) {
        addRecipe(makeRecipe(getAndValidateName(name), input, getAndValidateNotEmpty(output)));
    }

    protected abstract ItemStackToPigmentRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, PigmentStack output);

    @Override
    protected ActionAddMekanismRecipe getAction(ItemStackToPigmentRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(getRecipe().getOutputDefinitionNew(), CrTPigmentStack::new);
            }
        };
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_PIGMENT_EXTRACTING)
    public static class PigmentExtractingRecipeManager extends ItemStackToPigmentRecipeManager {

        public static final PigmentExtractingRecipeManager INSTANCE = new PigmentExtractingRecipeManager();

        private PigmentExtractingRecipeManager() {
            super(MekanismRecipeType.PIGMENT_EXTRACTING);
        }

        @Override
        protected ItemStackToPigmentRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, PigmentStack output) {
            return new PigmentExtractingIRecipe(id, input, output);
        }
    }
}