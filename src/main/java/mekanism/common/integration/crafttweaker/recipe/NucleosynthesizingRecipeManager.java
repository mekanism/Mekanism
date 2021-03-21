package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.NucleosynthesizingIRecipe;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_NUCLEOSYNTHESIZING)
public class NucleosynthesizingRecipeManager extends MekanismRecipeManager<NucleosynthesizingRecipe> {

    public static final NucleosynthesizingRecipeManager INSTANCE = new NucleosynthesizingRecipeManager();

    private NucleosynthesizingRecipeManager() {
        super(MekanismRecipeType.NUCLEOSYNTHESIZING);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient itemInput, GasStackIngredient gasInput, IItemStack output, int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be a number greater than zero! Duration: " + duration);
        }
        addRecipe(new NucleosynthesizingIRecipe(getAndValidateName(name), itemInput, gasInput, getAndValidateNotEmpty(output), duration));
    }

    @Override
    protected ActionAddMekanismRecipe getAction(NucleosynthesizingRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(getRecipe().getOutputDefinition(), MCItemStackMutable::new);
            }
        };
    }
}