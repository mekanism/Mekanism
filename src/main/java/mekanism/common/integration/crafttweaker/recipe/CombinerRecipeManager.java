package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.ingredient.CrTItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.CombinerIRecipe;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_COMBINING)
public class CombinerRecipeManager extends MekanismRecipeManager<CombinerRecipe> {

    public static final CombinerRecipeManager INSTANCE = new CombinerRecipeManager();

    private CombinerRecipeManager() {
        super(MekanismRecipeType.COMBINING);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTItemStackIngredient mainInput, CrTItemStackIngredient extraInput, IItemStack output) {
        addRecipe(new CombinerIRecipe(getAndValidateName(name), mainInput.getInternal(), extraInput.getInternal(), getAndValidateNotEmpty(output)));
    }

    @Override
    protected ActionAddMekanismRecipe getAction(CombinerRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(getRecipe().getOutputDefinition(), MCItemStackMutable::new);
            }
        };
    }
}