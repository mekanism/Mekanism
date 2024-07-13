package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.basic.BasicCombinerRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.world.item.crafting.RecipeInput;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_COMBINING)
public class CombinerRecipeManager extends MekanismRecipeManager<RecipeInput, CombinerRecipe> {

    public static final CombinerRecipeManager INSTANCE = new CombinerRecipeManager();

    private CombinerRecipeManager() {
        super(MekanismRecipeType.COMBINING);
    }

    /**
     * Adds a combining recipe that combines multiple items into a new item. Combiners and Combining Factories can process this recipe type.
     *
     * @param name       Name of the new recipe.
     * @param mainInput  {@link IIngredientWithAmount} representing the main item input of the recipe.
     * @param extraInput {@link IIngredientWithAmount} representing the secondary item input of the recipe.
     * @param output     {@link IItemStack} representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount mainInput, IIngredientWithAmount extraInput, IItemStack output) {
        addRecipe(name, makeRecipe(mainInput, extraInput, output));
    }

    /**
     * Creates a combining recipe that combines multiple items into a new item.
     *
     * @param mainInput  {@link IIngredientWithAmount} representing the main item input of the recipe.
     * @param extraInput {@link IIngredientWithAmount} representing the secondary item input of the recipe.
     * @param output     {@link IItemStack} representing the output of the recipe. Will be validated as not empty.
     */
    public final BasicCombinerRecipe makeRecipe(IIngredientWithAmount mainInput, IIngredientWithAmount extraInput, IItemStack output) {
        return new BasicCombinerRecipe(CrTUtils.fromCrT(mainInput), CrTUtils.fromCrT(extraInput), getAndValidateNotEmpty(output));
    }

    @Override
    protected String describeOutputs(CombinerRecipe recipe) {
        return CrTUtils.describeOutputs(recipe.getOutputDefinition(), ItemStackUtil::getCommandString);
    }
}