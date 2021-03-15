package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.MetallurgicInfuserIRecipe;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_METALLURGIC_INFUSING)
public class MetallurgicInfuserRecipeManager extends MekanismRecipeManager<MetallurgicInfuserRecipe> {

    public static final MetallurgicInfuserRecipeManager INSTANCE = new MetallurgicInfuserRecipeManager();

    private MetallurgicInfuserRecipeManager() {
        super(MekanismRecipeType.METALLURGIC_INFUSING);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient itemInput, InfusionStackIngredient infusionInput, IItemStack output) {
        addRecipe(new MetallurgicInfuserIRecipe(getAndValidateName(name), itemInput, infusionInput, getAndValidateNotEmpty(output)));
    }

    @Override
    protected ActionAddMekanismRecipe getAction(MetallurgicInfuserRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(getRecipe().getOutputDefinition(), MCItemStackMutable::new);
            }
        };
    }
}