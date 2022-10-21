package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.CombinerIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_COMBINING)
public class CombinerRecipeManager extends MekanismRecipeManager<CombinerRecipe> {

    public static final CombinerRecipeManager INSTANCE = new CombinerRecipeManager();

    private CombinerRecipeManager() {
        super(MekanismRecipeType.COMBINING);
    }

    /**
     * Adds a combining recipe that combines multiple items into a new item. Combiners and Combining Factories can process this recipe type.
     *
     * @param name       Name of the new recipe.
     * @param mainInput  {@link ItemStackIngredient} representing the main item input of the recipe.
     * @param extraInput {@link ItemStackIngredient} representing the secondary item input of the recipe.
     * @param output     {@link IItemStack} representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient mainInput, ItemStackIngredient extraInput, IItemStack output) {
        addRecipe(makeRecipe(getAndValidateName(name), mainInput, extraInput, output));
    }

    /**
     * Creates a combining recipe that combines multiple items into a new item.
     *
     * @param id         Name of the new recipe.
     * @param mainInput  {@link ItemStackIngredient} representing the main item input of the recipe.
     * @param extraInput {@link ItemStackIngredient} representing the secondary item input of the recipe.
     * @param output     {@link IItemStack} representing the output of the recipe. Will be validated as not empty.
     */
    public final CombinerRecipe makeRecipe(ResourceLocation id, ItemStackIngredient mainInput, ItemStackIngredient extraInput, IItemStack output) {
        return new CombinerIRecipe(id, mainInput, extraInput, getAndValidateNotEmpty(output));
    }

    @Override
    protected ActionAddMekanismRecipe getAction(CombinerRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(recipe.getOutputDefinition(), ItemStackUtil::getCommandString);
            }
        };
    }
}